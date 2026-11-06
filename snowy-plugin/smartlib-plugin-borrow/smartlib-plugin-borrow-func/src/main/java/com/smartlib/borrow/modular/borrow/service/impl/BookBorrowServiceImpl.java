package com.smartlib.borrow.modular.borrow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.borrow.modular.borrow.convert.BookBorrowConvert;
import com.smartlib.borrow.modular.borrow.entity.BookBorrow;
import com.smartlib.borrow.modular.borrow.enums.BookBorrowStatusEnum;
import com.smartlib.borrow.modular.borrow.mapper.BookBorrowMapper;
import com.smartlib.borrow.modular.borrow.param.BookBorrowManagePageParam;
import com.smartlib.borrow.modular.borrow.param.BookBorrowPageParam;
import com.smartlib.borrow.modular.borrow.param.BookBorrowParam;
import com.smartlib.borrow.modular.borrow.result.BookBorrowResult;
import com.smartlib.borrow.modular.borrow.service.BookBorrowService;
import com.smartlib.borrow.modular.quota.service.BorrowUserQuotaService;
import com.smartlib.borrow.modular.stats.service.BookBorrowStatsService;
import com.smartlib.book.feign.BookInventoryFeign;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;
import vip.xiaonuo.common.pojo.CommonResult;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.time.temporal.ChronoUnit;

@Service
public class BookBorrowServiceImpl extends ServiceImpl<BookBorrowMapper, BookBorrow>
        implements BookBorrowService {

    private static final int DEFAULT_BORROW_DAYS = 14;

    @Resource
    private BookBorrowConvert bookBorrowConvert;

    @Resource
    private BorrowUserQuotaService borrowUserQuotaService;

    @Resource
    private BookInventoryFeign bookInventoryFeign;

    @Resource
    private BookBorrowStatsService bookBorrowStatsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BookBorrowResult> borrow(BookBorrowParam param, String userId, String idempotencyKey) {
        // 固定馆藏加锁顺序，减少交叉批量借阅时的死锁。
        List<String> inventoryIds = param.getInventoryIds().stream().map(String::trim).sorted().toList();
        if (new HashSet<>(inventoryIds).size() != inventoryIds.size()) {
            throw new CommonException("同一批次不能重复借阅同一馆藏");
        }
        borrowUserQuotaService.occupyQuota(userId, inventoryIds.size());
        // 额度行已锁定，同一读者的并发请求在此串行检查。
        // FOR UPDATE 使用当前读，避免 MySQL REPEATABLE READ 读取到旧快照。
        if (!this.list(new LambdaQueryWrapper<BookBorrow>()
                .select(BookBorrow::getId)
                .eq(BookBorrow::getUserId, userId)
                .in(BookBorrow::getInventoryId, inventoryIds)
                .ne(BookBorrow::getStatus, BookBorrowStatusEnum.RETURNED.getValue())
                .last("FOR UPDATE")).isEmpty()) {
            throw new CommonException("存在尚未归还的同一馆藏，不能重复借阅");
        }
        Date borrowedAt = new Date();
        Date dueAt = Date.from(borrowedAt.toInstant().plus(DEFAULT_BORROW_DAYS, ChronoUnit.DAYS));
        List<BookBorrow> records = new ArrayList<>();
        for (String inventoryId : inventoryIds) {
            BookBorrow record = new BookBorrow();
            record.setUserId(userId);
            record.setInventoryId(inventoryId);
            record.setStatus(BookBorrowStatusEnum.BORROWING.getValue());
            record.setBorrowTime(borrowedAt);
            record.setDueTime(dueAt);
            if (!this.save(record)) {
                throw new CommonException("借阅记录保存失败");
            }
            records.add(record);
        }
        CommonResult<String> result = bookInventoryFeign.borrowStock(inventoryIds, idempotencyKey);
        if (result.getCode() != CommonResult.CODE_SUCCESS) {
            throw new CommonException(result.getMsg());
        }
        CommonResult<Map<String, String>> editions = bookInventoryFeign.editionKeys(inventoryIds);
        if (editions.getCode() != CommonResult.CODE_SUCCESS) {
            throw new CommonException(editions.getMsg());
        }
        Map<String, Integer> counts = new TreeMap<>();
        for (String inventoryId : inventoryIds) {
            String editionKey = editions.getData() == null ? null : editions.getData().get(inventoryId);
            if (StrUtil.isBlank(editionKey)) {
                throw new CommonException("馆藏缺少出版版本关联，inventoryId：{}", inventoryId);
            }
            counts.merge(editionKey, 1, Integer::sum);
        }
        // 按出版版本固定更新顺序，减少不同批次交叉更新时的死锁。
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String editionKey = entry.getKey();
            int borrowCount = entry.getValue();
            bookBorrowStatsService.addSuccessfulBorrows(editionKey, borrowCount);
        }
        return bookBorrowConvert.toResults(records);
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyReturn(String id, String userId) {
        BookBorrow update = new BookBorrow();
        update.setStatus(BookBorrowStatusEnum.RETURNING.getValue());
        if (!this.update(update, new LambdaUpdateWrapper<BookBorrow>()
                .eq(BookBorrow::getId, id.trim())
                .eq(BookBorrow::getUserId, userId)
                .eq(BookBorrow::getStatus, BookBorrowStatusEnum.BORROWING.getValue()))) {
            throw new CommonException("借阅记录不存在或当前状态不允许申请归还");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturn(String id, String operatorId, String idempotencyKey) {
        BookBorrow record = this.getById(id.trim());
        if (record == null) {
            throw new CommonException("借阅记录不存在");
        }
        BookBorrow update = new BookBorrow();
        update.setStatus(BookBorrowStatusEnum.RETURNED.getValue());
        update.setReturnTime(new Date());
        update.setReturnUser(operatorId);
        // 按原状态更新，同一记录即使使用不同请求号也只能确认一次。
        if (!this.update(update, new LambdaUpdateWrapper<BookBorrow>()
                .eq(BookBorrow::getId, record.getId())
                .eq(BookBorrow::getStatus, BookBorrowStatusEnum.RETURNING.getValue()))) {
            throw new CommonException("该记录尚未申请归还或已确认归还");
        }
        borrowUserQuotaService.releaseQuota(record.getUserId());
        CommonResult<String> result = bookInventoryFeign.returnStock(
                record.getInventoryId(), record.getId(), idempotencyKey);
        if (result.getCode() != CommonResult.CODE_SUCCESS) {
            throw new CommonException(result.getMsg());
        }
    }

    private Page<BookBorrowResult> queryPage(BookBorrowPageParam param,
            LambdaQueryWrapper<BookBorrow> query) {
        Page<BookBorrow> page = CommonPageRequest.defaultPage();
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
