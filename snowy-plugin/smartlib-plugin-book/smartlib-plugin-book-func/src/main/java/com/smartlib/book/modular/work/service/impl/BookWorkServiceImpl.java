package com.smartlib.book.modular.work.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.smartlib.book.modular.author.entity.BookAuthor;
import com.smartlib.book.modular.author.mapper.BookAuthorMapper;
import com.smartlib.book.modular.work.convert.BookWorkConvert;
import com.smartlib.book.modular.work.entity.BookWork;
import com.smartlib.book.modular.work.mapper.BookWorkMapper;
import com.smartlib.book.modular.work.param.BookWorkPageParam;
import com.smartlib.book.modular.work.result.BookWorkDetailResult;
import com.smartlib.book.modular.work.result.BookWorkSummaryResult;
import com.smartlib.book.modular.work.service.BookWorkService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookWorkServiceImpl extends ServiceImpl<BookWorkMapper, BookWork> implements BookWorkService {

    @Resource
    private BookAuthorMapper bookAuthorMapper;

    @Resource
    private BookWorkConvert bookWorkConvert;

    @Override
    public Page<BookWorkSummaryResult> page(BookWorkPageParam param) {
        Page<BookWork> page = CommonPageRequest.defaultPage();
        if (page.getSize() <= 0) {
            throw new CommonException("每页条数必须大于0");
        }
        QueryWrapper<BookWork> query = new QueryWrapper<BookWork>().checkSqlInjection();
        query.lambda().select(BookWork::getKey, BookWork::getTitle);
        String searchKey = StrUtil.trim(param.getSearchKey());
        query.lambda().like(StrUtil.isNotBlank(searchKey), BookWork::getTitle, searchKey);
        if (StrUtil.isNotBlank(param.getAuthorKey())) {
            query.apply("`key` IN (SELECT work_key FROM author_works WHERE author_key = {0}"
                    + " AND delete_flag = 'NOT_DELETE')", param.getAuthorKey().trim());
        }
        if (ObjectUtil.isAllNotEmpty(param.getSortField(), param.getSortOrder())) {
            CommonSortOrderEnum.validate(param.getSortOrder());
            boolean asc = CommonSortOrderEnum.ASC.getValue().equals(param.getSortOrder());
            if ("title".equals(param.getSortField())) {
                query.lambda().orderBy(true, asc, BookWork::getTitle).orderByAsc(BookWork::getKey);
            } else if ("key".equals(param.getSortField())) {
                query.lambda().orderBy(true, asc, BookWork::getKey);
            } else {
                throw new CommonException("不支持的排序字段：{}", param.getSortField());
            }
        } else {
            query.lambda().orderByAsc(BookWork::getKey);
        }
        page = this.page(page, query);
        Page<BookWorkSummaryResult> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(bookWorkConvert.toSummaries(page.getRecords()));
        return result;
    }

    @Override
    public BookWorkDetailResult detail(String key) {
        // data 默认不加载，详情显式查询。
        BookWork work = this.getOne(new LambdaQueryWrapper<BookWork>()
                .select(BookWork::getKey, BookWork::getTitle, BookWork::getData).eq(BookWork::getKey, key));
        if (work == null) {
            throw new CommonException("作品不存在，key值为：{}", key);
        }
        BookWorkDetailResult result = bookWorkConvert.toDetail(work);
        List<BookAuthor> authors = bookAuthorMapper.selectList(new LambdaQueryWrapper<BookAuthor>()
                .select(BookAuthor::getKey, BookAuthor::getName)
                .apply("`key` IN (SELECT author_key FROM author_works WHERE work_key = {0}"
                        + " AND delete_flag = 'NOT_DELETE')", key)
                .orderByAsc(BookAuthor::getKey));
        result.setAuthors(bookWorkConvert.toAuthorSummaries(authors));
        JsonNode data = work.getData();
        if (data == null || !data.isObject()) {
            return result;
        }
        JsonNode description = data.path("description");
        result.setDescription(description.isTextual() ? description.asText() : description.path("value").asText(null));
        result.setFirstPublishDate(data.path("first_publish_date").asText(null));
        List<Long> covers = new ArrayList<>();
        if (data.path("covers").isArray()) {
            for (JsonNode cover : data.path("covers")) {
                if (cover.isIntegralNumber() && cover.asLong() > 0) {
                    covers.add(cover.asLong());
                }
            }
        }
        result.setCovers(covers);
        List<String> subjects = new ArrayList<>();
        if (data.path("subjects").isArray()) {
            for (JsonNode subject : data.path("subjects")) {
                if (subject.isTextual() && StrUtil.isNotBlank(subject.asText())) {
                    subjects.add(subject.asText());
                }
            }
        }
        result.setSubjects(subjects);
        return result;
    }
}
