package com.smartlib.book.modular.inventory.convert;

import com.smartlib.book.modular.inventory.entity.BookInventory;
import com.smartlib.book.modular.inventory.param.BookInventoryAddParam;
import com.smartlib.book.modular.inventory.result.BookInventoryResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookInventoryConvert {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "editionKey", source = "editionKey")
    @Mapping(target = "totalStock", source = "totalStock")
    BookInventory toEntity(BookInventoryAddParam param);

    BookInventoryResult toResult(BookInventory inventory);

    List<BookInventoryResult> toResults(List<BookInventory> inventories);
}
