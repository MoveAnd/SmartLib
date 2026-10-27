package com.smartlib.borrow.modular.borrow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.borrow.modular.borrow.param.BookBorrowManagePageParam;
import com.smartlib.borrow.modular.borrow.param.BookBorrowPageParam;
import com.smartlib.borrow.modular.borrow.result.BookBorrowResult;

public interface BookBorrowService {

    /** 管理端按读者、馆藏及状态查询借阅记录。 */
    Page<BookBorrowResult> page(BookBorrowManagePageParam param);

    /** 管理端根据记录ID查询详情。 */
    BookBorrowResult detail(String id);

    /** 查询当前读者本人的记录，userId必须取自C端登录身份。 */
    Page<BookBorrowResult> clientPage(BookBorrowPageParam param, String userId);

    /** 在数据库查询中限定读者归属，其他读者的记录按不存在处理。 */
    BookBorrowResult clientDetail(String id, String userId);
}
