package com.smartlib.borrow.modular.borrow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.borrow.modular.borrow.convert.BookBorrowConvert;
import com.smartlib.borrow.modular.borrow.entity.BookBorrow;
import com.smartlib.borrow.modular.borrow.enums.BookBorrowStatusEnum;
import com.smartlib.borrow.modular.borrow.mapper.BookBorrowMapper;
import com.smartlib.borrow.modular.borrow.param.BookBorrowManagePageParam;
import com.smartlib.borrow.modular.borrow.param.BookBorrowPageParam;
import com.smartlib.borrow.modular.borrow.result.BookBorrowResult;
import com.smartlib.borrow.modular.borrow.service.BookBorrowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;

@Service
public class BookBorrowServiceImpl extends ServiceImpl<BookBorrowMapper, BookBorrow>
        implements BookBorrowService {

    @Resource
    private BookBorrowConvert bookBorrowConvert;

    @Override
    public Page<BookBorrowResult> page(BookBorrowManagePageParam param) {
        LambdaQueryWrapper<BookBorrow> query = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(param.getUserId())) {
            query.eq(BookBorrow::getUserId, param.getUserId().trim());
        }
        return queryPage(param, query);
    }

    @Override
    public BookBorrowResult detail(String id) {
        if (StrUtil.isBlank(id)) {
            throw new CommonException("id不能为空");
        }
        BookBorrow record = this.getById(id.trim());
        if (record == null) {
            throw new CommonException("借阅记录不存在");
        }
        return bookBorrowConvert.toResult(record);
    }

    @Override
    public Page<BookBorrowResult> clientPage(BookBorrowPageParam param, String userId) {
        if (StrUtil.isBlank(userId)) {
            throw new CommonException("读者身份不能为空");
        }
        return queryPage(param, new LambdaQueryWrapper<BookBorrow>()
                .eq(BookBorrow::getUserId, userId));
    }

    @Override
    public BookBorrowResult clientDetail(String id, String userId) {
        if (StrUtil.isBlank(userId)) {
            throw new CommonException("读者身份不能为空");
        }
        if (StrUtil.isBlank(id)) {
            throw new CommonException("id不能为空");
        }
        BookBorrow record = this.getOne(new LambdaQueryWrapper<BookBorrow>()
                .eq(BookBorrow::getId, id.trim())
                .eq(BookBorrow::getUserId, userId));
        if (record == null) {
            throw new CommonException("借阅记录不存在");
        }
        return bookBorrowConvert.toResult(record);
    }

    private Page<BookBorrowResult> queryPage(BookBorrowPageParam param,
            LambdaQueryWrapper<BookBorrow> query) {
        Page<BookBorrow> page = CommonPageRequest.defaultPage();
        if (page.getCurrent() <= 0 || page.getSize() <= 0) {
            throw new CommonException("页码和每页条数必须大于0");
        }
        if (StrUtil.isNotBlank(param.getInventoryId())) {
            query.eq(BookBorrow::getInventoryId, param.getInventoryId().trim());
        }
        if (StrUtil.isNotBlank(param.getStatus())) {
            String status = param.getStatus().trim();
            BookBorrowStatusEnum.validate(status);
            query.eq(BookBorrow::getStatus, status);
        }
        boolean hasField = StrUtil.isNotBlank(param.getSortField());
        boolean hasOrder = StrUtil.isNotBlank(param.getSortOrder());
        if (hasField != hasOrder) {
            throw new CommonException("排序字段和排序方式必须同时传入");
        }
        if (!hasField) {
            query.orderByDesc(BookBorrow::getBorrowTime).orderByDesc(BookBorrow::getId);
        } else {
            CommonSortOrderEnum.validate(param.getSortOrder());
            SFunction<BookBorrow, ?> column = switch (param.getSortField()) {
                case "id" -> BookBorrow::getId;
                case "borrowTime" -> BookBorrow::getBorrowTime;
                case "dueTime" -> BookBorrow::getDueTime;
                case "returnTime" -> BookBorrow::getReturnTime;
                case "createTime" -> BookBorrow::getCreateTime;
                default -> throw new CommonException("不支持的排序字段：{}", param.getSortField());
            };
            query.orderBy(true, CommonSortOrderEnum.ASC.getValue().equals(param.getSortOrder()), column)
                    .orderByAsc(!"id".equals(param.getSortField()), BookBorrow::getId);
        }
        page = this.page(page, query);
        Page<BookBorrowResult> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(bookBorrowConvert.toResults(page.getRecords()));
        return result;
    }
}
