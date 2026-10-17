package com.smartlib.book.modular.author.convert;

import com.smartlib.book.modular.author.entity.BookAuthor;
import com.smartlib.book.modular.author.result.BookAuthorDetailResult;
import com.smartlib.book.modular.author.result.BookAuthorSummaryResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookAuthorConvert {

    BookAuthorSummaryResult toSummary(BookAuthor author);

    List<BookAuthorSummaryResult> toSummaries(List<BookAuthor> authors);

    // 仅转换基础字段，JSON 字段由服务组装。
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "key", source = "key")
    @Mapping(target = "name", source = "name")
    BookAuthorDetailResult toDetail(BookAuthor author);
}
