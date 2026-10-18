package com.smartlib.book.modular.inventory.convert;

import com.smartlib.book.modular.inventory.entity.BookInventory;
import com.smartlib.book.modular.inventory.result.BookInventoryResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookInventoryConvert {

    BookInventoryResult toResult(BookInventory inventory);

    List<BookInventoryResult> toResults(List<BookInventory> inventories);
}
