package com.smartlib.book.modular.author.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.book.modular.author.convert.BookAuthorConvert;
import com.smartlib.book.modular.author.entity.BookAuthor;
import com.smartlib.book.modular.author.mapper.BookAuthorMapper;
import com.smartlib.book.modular.author.param.BookAuthorPageParam;
import com.smartlib.book.modular.author.result.BookAuthorDetailResult;
import com.smartlib.book.modular.author.result.BookAuthorSummaryResult;
import com.smartlib.book.modular.author.service.BookAuthorService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;

import java.util.ArrayList;
import java.util.List;

/** 作者查询服务实现。 */
@Service
public class BookAuthorServiceImpl extends ServiceImpl<BookAuthorMapper, BookAuthor> implements BookAuthorService {

    @Resource
    private BookAuthorConvert bookAuthorConvert;

    @Override
    public Page<BookAuthorSummaryResult> page(BookAuthorPageParam param) {
        Page<BookAuthor> bookAuthorPage = CommonPageRequest.defaultPage();
        // 负数页大小会使分页插件跳过LIMIT，必须在查询前拒绝。
        if (bookAuthorPage.getSize() <= 0) {
            throw new CommonException("每页条数必须大于0");
        }
        QueryWrapper<BookAuthor> queryWrapper = new QueryWrapper<BookAuthor>().checkSqlInjection();
        // 摘要只返回主键与姓名，不读取 data 大字段
        queryWrapper.lambda().select(BookAuthor::getKey, BookAuthor::getName);
        String searchKey = StrUtil.trim(param.getSearchKey());
        if (ObjectUtil.isNotEmpty(searchKey)) {
            queryWrapper.lambda().like(BookAuthor::getName, searchKey);
        }
        // 排序字段白名单：不接受客户端传入的 SQL 列名，key 为保留字，交给实体列名加反引号
        if (ObjectUtil.isAllNotEmpty(param.getSortField(), param.getSortOrder())) {
            CommonSortOrderEnum.validate(param.getSortOrder());
            boolean isAsc = CommonSortOrderEnum.ASC.getValue().equalsIgnoreCase(param.getSortOrder());
            if ("name".equals(param.getSortField())) {
                // 同名作者按唯一主键排序，避免跨页重复或遗漏。
                queryWrapper.lambda().orderBy(true, isAsc, BookAuthor::getName).orderByAsc(BookAuthor::getKey);
            } else if ("key".equals(param.getSortField())) {
                queryWrapper.lambda().orderBy(true, isAsc, BookAuthor::getKey);
            } else {
                throw new CommonException("不支持的排序字段：{}", param.getSortField());
            }
        } else {
            // 默认按主键升序：命中聚簇索引，分页结果稳定
            queryWrapper.lambda().orderByAsc(BookAuthor::getKey);
        }
        bookAuthorPage = this.page(bookAuthorPage, queryWrapper);
        Page<BookAuthorSummaryResult> resultPage = new Page<>(bookAuthorPage.getCurrent(), bookAuthorPage.getSize(),
                bookAuthorPage.getTotal());
        resultPage.setRecords(bookAuthorConvert.toSummaries(bookAuthorPage.getRecords()));
        return resultPage;
    }

    @Override
    public BookAuthorDetailResult detail(String key) {
        QueryWrapper<BookAuthor> queryWrapper = new QueryWrapper<BookAuthor>().checkSqlInjection();
        // data 在实体上为 select=false，默认查询不加载，详情必须显式指定该列
        queryWrapper.lambda().select(BookAuthor::getKey, BookAuthor::getName, BookAuthor::getData)
                .eq(BookAuthor::getKey, key);
        BookAuthor bookAuthor = this.getOne(queryWrapper);
        if (ObjectUtil.isEmpty(bookAuthor)) {
            throw new CommonException("作者不存在，key值为：{}", key);
        }
        BookAuthorDetailResult result = bookAuthorConvert.toDetail(bookAuthor);
        JsonNode data = bookAuthor.getData();
        if (data == null || !data.isObject()) {
            return result;
        }
        result.setBio(textValue(data.path("bio")));
        result.setBirthDate(textValue(data.path("birth_date")));
        result.setDeathDate(textValue(data.path("death_date")));
        result.setWikipedia(textValue(data.path("wikipedia")));
        JsonNode alternateNames = data.path("alternate_names");
        if (alternateNames.isArray()) {
            List<String> names = new ArrayList<>();
            for (JsonNode element : alternateNames) {
                String name = textValue(element);
                if (ObjectUtil.isNotEmpty(name)) {
                    names.add(name);
                }
            }
            result.setAlternateNames(names);
        }
        JsonNode photos = data.path("photos");
        if (photos.isArray()) {
            List<Long> photoIds = new ArrayList<>();
            // 来源以-1表示无照片，仅保留正整数ID及原始顺序。
            for (JsonNode element : photos) {
                if (element.isIntegralNumber() && element.asLong() > 0) {
                    photoIds.add(element.asLong());
                }
            }
            result.setPhotos(photoIds);
        }
        return result;
    }

    /** 取文本字段：兼容字符串与来源的 {"type":"/type/text","value":"..."} 结构。 */
    private String textValue(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual()) {
            return StrUtil.emptyToNull(StrUtil.trim(node.asText()));
        }
        return textValue(node.path("value"));
    }
}
