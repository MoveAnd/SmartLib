package com.smartlib.book.modular.work.convert;

import com.smartlib.book.modular.author.entity.BookAuthor;
import com.smartlib.book.modular.author.result.BookAuthorSummaryResult;
import com.smartlib.book.modular.work.entity.BookWork;
import com.smartlib.book.modular.work.result.BookWorkDetailResult;
import com.smartlib.book.modular.work.result.BookWorkSummaryResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookWorkConvert {

    BookWorkSummaryResult toSummary(BookWork work);

    List<BookWorkSummaryResult> toSummaries(List<BookWork> works);

    // 仅转换基础字段，JSON 字段与作者列表由服务组装。
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "key", source = "key")
    @Mapping(target = "title", source = "title")
    BookWorkDetailResult toDetail(BookWork work);

    BookAuthorSummaryResult toAuthorSummary(BookAuthor author);

    List<BookAuthorSummaryResult> toAuthorSummaries(List<BookAuthor> authors);
}
