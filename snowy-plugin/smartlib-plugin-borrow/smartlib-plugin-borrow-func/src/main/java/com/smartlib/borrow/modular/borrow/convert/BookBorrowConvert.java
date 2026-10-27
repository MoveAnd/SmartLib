package com.smartlib.borrow.modular.borrow.convert;

import com.smartlib.borrow.modular.borrow.entity.BookBorrow;
import com.smartlib.borrow.modular.borrow.result.BookBorrowResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BookBorrowConvert {

    BookBorrowResult toResult(BookBorrow record);

    List<BookBorrowResult> toResults(List<BookBorrow> records);
}
