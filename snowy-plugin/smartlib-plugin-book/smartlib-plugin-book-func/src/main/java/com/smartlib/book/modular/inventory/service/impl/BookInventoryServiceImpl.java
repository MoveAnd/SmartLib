package com.smartlib.book.modular.inventory.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartlib.book.modular.inventory.convert.BookInventoryConvert;
import com.smartlib.book.modular.inventory.entity.BookInventory;
import com.smartlib.book.modular.inventory.mapper.BookInventoryMapper;
import com.smartlib.book.modular.inventory.param.BookInventoryAddParam;
import com.smartlib.book.modular.inventory.param.BookInventoryAdjustStockParam;
import com.smartlib.book.modular.inventory.param.BookInventoryPageParam;
import com.smartlib.book.modular.inventory.result.BookInventoryResult;
import com.smartlib.book.modular.inventory.service.BookInventoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;

@Service
public class BookInventoryServiceImpl extends ServiceImpl<BookInventoryMapper, BookInventory>
        implements BookInventoryService {

    @Resource
    private BookInventoryConvert bookInventoryConvert;

    @Override
    public Page<BookInventoryResult> page(BookInventoryPageParam param) {
        Page<BookInventory> page = CommonPageRequest.defaultPage();
        if (page.getSize() <= 0) {
            throw new CommonException("每页条数必须大于0");
        }
        LambdaQueryWrapper<BookInventory> query = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(param.getEditionKey())) {
            query.eq(BookInventory::getEditionKey, param.getEditionKey().trim());
        }
        if (StrUtil.isNotBlank(param.getStatus())) {
            String status = param.getStatus().trim();
            if (!"ON_SHELF".equals(status) && !"OFF_SHELF".equals(status)) {
                throw new CommonException("不支持的上架状态：{}", status);
            }
            query.eq(BookInventory::getStatus, status);
        }
        String searchKey = StrUtil.trim(param.getSearchKey());
        if (StrUtil.isNotBlank(searchKey)) {
            query.apply("edition_key IN (SELECT `key` FROM editions WHERE title LIKE {0}"
                    + " AND delete_flag = 'NOT_DELETE')", "%" + searchKey + "%");
        }
        if (ObjectUtil.isAllNotEmpty(param.getSortField(), param.getSortOrder())) {
            CommonSortOrderEnum.validate(param.getSortOrder());
            SFunction<BookInventory, ?> column = switch (param.getSortField()) {
                case "id" -> BookInventory::getId;
                case "editionKey" -> BookInventory::getEditionKey;
                case "createTime" -> BookInventory::getCreateTime;
                case "totalStock" -> BookInventory::getTotalStock;
                case "availableStock" -> BookInventory::getAvailableStock;
                default -> throw new CommonException("不支持的排序字段：{}", param.getSortField());
            };
            query.orderBy(true, CommonSortOrderEnum.ASC.getValue().equals(param.getSortOrder()), column)
                    .orderByAsc(!"id".equals(param.getSortField()), BookInventory::getId);
        } else {
            query.orderByDesc(BookInventory::getCreateTime).orderByDesc(BookInventory::getId);
        }
        page = this.page(page, query);
        Page<BookInventoryResult> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(bookInventoryConvert.toResults(page.getRecords()));
        return result;
    }

    @Override
    public BookInventoryResult detail(String id) {
        BookInventory inventory = this.getById(StrUtil.trim(id));
        if (inventory == null) {
            throw new CommonException("馆藏不存在，id值为：{}", id);
        }
        return bookInventoryConvert.toResult(inventory);
    }

    @Override
    public void add(BookInventoryAddParam param) {
        throw new CommonException(501, "接口暂未实现");
    }

    @Override
    public void adjustStock(BookInventoryAdjustStockParam param) {
        throw new CommonException(501, "接口暂未实现");
    }

    @Override
    public void onShelf(String id) {
        throw new CommonException(501, "接口暂未实现");
    }

    @Override
    public void offShelf(String id) {
        throw new CommonException(501, "接口暂未实现");
    }
}
