package com.smartlib.book.modular.edition.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartlib.book.modular.edition.convert.BookEditionConvert;
import com.smartlib.book.modular.edition.entity.BookEdition;
import com.smartlib.book.modular.edition.entity.BookEditionIsbn;
import com.smartlib.book.modular.edition.mapper.BookEditionIsbnMapper;
import com.smartlib.book.modular.edition.mapper.BookEditionMapper;
import com.smartlib.book.modular.edition.param.BookEditionDeleteParam;
import com.smartlib.book.modular.edition.param.BookEditionEditParam;
import com.smartlib.book.modular.edition.param.BookEditionPageParam;
import com.smartlib.book.modular.edition.result.BookEditionDetailResult;
import com.smartlib.book.modular.edition.result.BookEditionSummaryResult;
import com.smartlib.book.modular.edition.service.BookEditionService;
import com.smartlib.book.modular.favorite.entity.BookFavorite;
import com.smartlib.book.modular.favorite.enums.BookFavoriteStatusEnum;
import com.smartlib.book.modular.favorite.mapper.BookFavoriteMapper;
import com.smartlib.book.modular.inventory.entity.BookInventory;
import com.smartlib.book.modular.inventory.mapper.BookInventoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vip.xiaonuo.common.cache.CommonCacheOperator;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BookEditionServiceImpl extends ServiceImpl<BookEditionMapper, BookEdition> implements BookEditionService {

    /**
     * 出版版本详情缓存前缀
     */
    private static final String EDITION_DETAIL_CACHE_KEY = "book:edition:detail:";

    /**
     * 正常详情最长缓存 30 分钟，最多随机提前 5 分钟过期（秒）
     */
    private static final long EDITION_DETAIL_CACHE_TTL_SECONDS = 1800;

    /**
     * 随机提前过期的最大秒数，使有效期分散在 25～30 分钟
     */
    private static final long EDITION_DETAIL_CACHE_TTL_JITTER_SECONDS = 300;

    /**
     * 编辑参数字段名与来源 JSON 属性名的对应关系；书名不允许清空。
     */
    private static final Map<String, String> CLEARABLE_JSON_FIELDS = Map.of(
            "subtitle", "subtitle",
            "description", "description",
            "publishers", "publishers",
            "publishDate", "publish_date",
            "languages", "languages",
            "numberOfPages", "number_of_pages",
            "covers", "covers");

    @Resource
    private CommonCacheOperator commonCacheOperator;

    @Resource
    private BookEditionIsbnMapper bookEditionIsbnMapper;

    @Resource
    private BookEditionConvert bookEditionConvert;

    @Resource
    private BookInventoryMapper bookInventoryMapper;

    @Resource
    private BookFavoriteMapper bookFavoriteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(BookEditionEditParam param) {
        String key = param.getKey().trim();
        BookEdition edition = baseMapper.selectForEdit(key);
        if (edition == null) {
            throw new CommonException("出版版本不存在，key值为：{}", key);
        }
        if (param.getTitle() == null && param.getSubtitle() == null && param.getDescription() == null
                && param.getPublishers() == null && param.getPublishDate() == null && param.getLanguages() == null
                && param.getNumberOfPages() == null && param.getCovers() == null
                && (param.getClearFields() == null || param.getClearFields().isEmpty())) {
            return;
        }
        if (param.getTitle() != null && StrUtil.isBlank(param.getTitle())) {
            throw new CommonException("书名不能为空");
        }
        JsonNode source = edition.getData();
        if (source != null && !source.isNull() && !source.isObject()) {
            throw new CommonException("版本来源数据不是JSON对象，无法编辑");
        }
        ObjectNode data = source == null || source.isNull()
                ? JsonNodeFactory.instance.objectNode() : (ObjectNode) source;
        BookEdition update = new BookEdition();
        update.setKey(key);
        if (param.getTitle() != null) {
            String title = param.getTitle().trim();
            data.put("title", title);
            update.setTitle(title);
        }
        applyOptionalFields(data, param);
        clearOptionalFields(data, param);
        update.setData(data);
        this.updateById(update);
        invalidateDetailCacheAfterCommit(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(BookEditionDeleteParam param) {
        String key = param.getKey().trim();
        // 与编辑使用同一版本行锁，已删除或不存在的版本跳过关联校验。
        BookEdition edition = baseMapper.selectForEdit(key);
        if (edition != null) {
            if (bookInventoryMapper.exists(new LambdaQueryWrapper<BookInventory>()
                    .eq(BookInventory::getEditionKey, key))) {
                throw new CommonException("出版版本存在馆藏记录，不能删除");
            }
            if (bookFavoriteMapper.exists(new LambdaQueryWrapper<BookFavorite>()
                    .eq(BookFavorite::getEditionKey, key)
                    .eq(BookFavorite::getStatus, BookFavoriteStatusEnum.FAVORITED.getValue()))) {
                throw new CommonException("出版版本存在有效收藏，不能删除");
            }
            this.removeById(key);
        }
        // 重试同样清理缓存，修复先前提交成功但缓存删除失败的遗留数据。
        invalidateDetailCacheAfterCommit(key);
    }

    /** 事务回滚时保留旧缓存，提交成功后才使该版本详情失效。 */
    private void invalidateDetailCacheAfterCommit(String key) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                commonCacheOperator.remove(EDITION_DETAIL_CACHE_KEY + key);
            }
        });
    }

    private void applyOptionalFields(ObjectNode data, BookEditionEditParam param) {
        if (param.getSubtitle() != null) {
            data.put("subtitle", param.getSubtitle());
        }
        if (param.getDescription() != null) {
            data.put("description", param.getDescription());
        }
        if (param.getPublishDate() != null) {
            data.put("publish_date", param.getPublishDate());
        }
        if (param.getNumberOfPages() != null) {
            data.put("number_of_pages", param.getNumberOfPages());
        }
        if (param.getPublishers() != null) {
            if (param.getPublishers().isEmpty()) {
                data.remove("publishers");
            } else {
                ArrayNode publishers = data.putArray("publishers");
                for (String publisher : param.getPublishers()) {
                    publishers.add(publisher);
                }
            }
        }
        if (param.getLanguages() != null) {
            if (param.getLanguages().isEmpty()) {
                data.remove("languages");
            } else {
                ArrayNode languages = data.putArray("languages");
                for (String language : param.getLanguages()) {
                    languages.addObject().put("key", language);
                }
            }
        }
        if (param.getCovers() != null) {
            if (param.getCovers().isEmpty()) {
                data.remove("covers");
            } else {
                ArrayNode covers = data.putArray("covers");
                for (Long cover : param.getCovers()) {
                    covers.add(cover);
                }
            }
        }
    }

    private void clearOptionalFields(ObjectNode data, BookEditionEditParam param) {
        if (param.getClearFields() != null) {
            for (String field : param.getClearFields()) {
                String jsonField = CLEARABLE_JSON_FIELDS.get(field);
                if (jsonField == null) {
                    throw new CommonException("不支持清空字段：{}", field);
                }
                boolean supplied = switch (field) {
                    case "subtitle" -> param.getSubtitle() != null;
                    case "description" -> param.getDescription() != null;
                    case "publishers" -> param.getPublishers() != null;
                    case "publishDate" -> param.getPublishDate() != null;
                    case "languages" -> param.getLanguages() != null;
                    case "numberOfPages" -> param.getNumberOfPages() != null;
                    case "covers" -> param.getCovers() != null;
                    default -> false;
                };
                if (supplied) {
                    throw new CommonException("字段不能同时赋值和清空：{}", field);
                }
                data.remove(jsonField);
            }
        }
    }

    @Override
    public Page<BookEditionSummaryResult> page(BookEditionPageParam param) {
        Page<BookEdition> page = CommonPageRequest.defaultPage();
        if (page.getSize() <= 0) {
            throw new CommonException("每页条数必须大于0");
        }
        LambdaQueryWrapper<BookEdition> query = new LambdaQueryWrapper<BookEdition>()
                .select(BookEdition::getKey, BookEdition::getTitle, BookEdition::getWorkKey, BookEdition::getOcaid)
                .eq(BookEdition::getWorkKey, param.getWorkKey().trim());
        String searchKey = StrUtil.trim(param.getSearchKey());
        query.like(StrUtil.isNotBlank(searchKey), BookEdition::getTitle, searchKey);
        if (ObjectUtil.isAllNotEmpty(param.getSortField(), param.getSortOrder())) {
            CommonSortOrderEnum.validate(param.getSortOrder());
            boolean asc = CommonSortOrderEnum.ASC.getValue().equals(param.getSortOrder());
            if ("title".equals(param.getSortField())) {
                query.orderBy(true, asc, BookEdition::getTitle).orderByAsc(BookEdition::getKey);
            } else if ("key".equals(param.getSortField())) {
                query.orderBy(true, asc, BookEdition::getKey);
            } else {
                throw new CommonException("不支持的排序字段：{}", param.getSortField());
            }
        } else {
            query.orderByAsc(BookEdition::getKey);
        }
        page = this.page(page, query);
        Page<BookEditionSummaryResult> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(bookEditionConvert.toSummaries(page.getRecords()));
        return result;
    }

    @Override
    public BookEditionDetailResult detail(String key) {
        // 查Redis缓存
        String cacheKey = EDITION_DETAIL_CACHE_KEY + key;
        BookEditionDetailResult cached = (BookEditionDetailResult) commonCacheOperator.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // data 默认不加载，详情显式查询。
        BookEdition edition = this.getOne(new LambdaQueryWrapper<BookEdition>()
                .select(BookEdition::getKey, BookEdition::getTitle, BookEdition::getWorkKey,
                        BookEdition::getOcaid, BookEdition::getData).eq(BookEdition::getKey, key));
        if (edition == null) {
            throw new CommonException("出版版本不存在，key值为：{}", key);
        }
        BookEditionDetailResult result = bookEditionConvert.toDetail(edition);
        result.setIsbns(bookEditionIsbnMapper.selectList(new LambdaQueryWrapper<BookEditionIsbn>()
                .select(BookEditionIsbn::getIsbn).eq(BookEditionIsbn::getEditionKey, key)
                .orderByAsc(BookEditionIsbn::getIsbn)).stream().map(BookEditionIsbn::getIsbn).toList());

        // 解析data
        JsonNode data = edition.getData();
        if (data != null && data.isObject()) {
            result.setSubtitle(data.path("subtitle").asText(null));
            JsonNode description = data.path("description");
            result.setDescription(description.isTextual() ? description.asText() : description.path("value").asText(null));
            result.setPublishDate(data.path("publish_date").asText(null));
            JsonNode pages = data.path("number_of_pages");
            if (pages.isIntegralNumber() && pages.canConvertToInt() && pages.asInt() > 0) {
                result.setNumberOfPages(pages.asInt());
            }
            List<String> publishers = new ArrayList<>();
            if (data.path("publishers").isArray()) {
                for (JsonNode publisher : data.path("publishers")) {
                    if (publisher.isTextual() && StrUtil.isNotBlank(publisher.asText())) {
                        publishers.add(publisher.asText());
                    }
                }
            }
            result.setPublishers(publishers);
            List<String> languages = new ArrayList<>();
            if (data.path("languages").isArray()) {
                for (JsonNode language : data.path("languages")) {
                    JsonNode languageKey = language.path("key");
                    if (languageKey.isTextual() && StrUtil.isNotBlank(languageKey.asText())) {
                        languages.add(languageKey.asText());
                    }
                }
            }
            result.setLanguages(languages);
            Set<Long> covers = new LinkedHashSet<>();
            if (data.path("covers").isArray()) {
                for (JsonNode cover : data.path("covers")) {
                    if (cover.isIntegralNumber() && cover.canConvertToLong() && cover.asLong() > 0) {
                        covers.add(cover.asLong());
                    }
                }
            }
            result.setCovers(new ArrayList<>(covers));
        }

        // 写入Redis缓存
        long ttlSeconds = EDITION_DETAIL_CACHE_TTL_SECONDS
                - ThreadLocalRandom.current().nextLong(EDITION_DETAIL_CACHE_TTL_JITTER_SECONDS + 1);
        commonCacheOperator.put(cacheKey, result, ttlSeconds);

        return result;
    }

    @Override
    public List<BookEditionSummaryResult> byIsbn(String isbn) {
        String normalized = StrUtil.nullToEmpty(isbn).replaceAll("[\\s\\p{Z}-]", "").toUpperCase(Locale.ROOT);
        boolean isbn10 = normalized.matches("[0-9]{9}[0-9X]");
        if (!isbn10 && !normalized.matches("97[89][0-9]{10}")) {
            throw new CommonException("ISBN格式不正确");
        }
        int checksum = 0;
        for (int i = 0; i < normalized.length(); i++) {
            int digit = normalized.charAt(i) == 'X' ? 10 : normalized.charAt(i) - '0';
            checksum += digit * (isbn10 ? 10 - i : (i % 2 == 0 ? 1 : 3));
        }
        if (checksum % (isbn10 ? 11 : 10) != 0) {
            throw new CommonException("ISBN校验位不正确");
        }
        // 导入器已保存 ISBN-10/13 等价别名；IN 子查询保证同一版本只返回一次。
        return bookEditionConvert.toSummaries(this.list(new LambdaQueryWrapper<BookEdition>()
                .select(BookEdition::getKey, BookEdition::getTitle, BookEdition::getWorkKey, BookEdition::getOcaid)
                .apply("`key` IN (SELECT edition_key FROM edition_isbns WHERE isbn = {0}"
                        + " AND delete_flag = 'NOT_DELETE')", normalized)
                .orderByAsc(BookEdition::getKey)));
    }
}
