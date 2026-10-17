package com.smartlib.book.modular.edition.convert;

import com.smartlib.book.modular.edition.entity.BookEdition;
import com.smartlib.book.modular.edition.result.BookEditionDetailResult;
import com.smartlib.book.modular.edition.result.BookEditionSummaryResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookEditionConvert {

    BookEditionSummaryResult toSummary(BookEdition edition);

    List<BookEditionSummaryResult> toSummaries(List<BookEdition> editions);

    // 仅转换基础字段，JSON 字段及有效 ISBN 由服务组装。
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "key", source = "key")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "workKey", source = "workKey")
    @Mapping(target = "ocaid", source = "ocaid")
    BookEditionDetailResult toDetail(BookEdition edition);
}
