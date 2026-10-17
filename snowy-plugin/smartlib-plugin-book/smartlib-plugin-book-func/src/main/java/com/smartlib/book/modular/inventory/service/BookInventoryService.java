package com.smartlib.book.modular.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.inventory.entity.BookInventory;
import com.smartlib.book.modular.inventory.param.BookInventoryAddParam;
import com.smartlib.book.modular.inventory.param.BookInventoryAdjustStockParam;
import com.smartlib.book.modular.inventory.param.BookInventoryPageParam;

public interface BookInventoryService {

    /** 馆藏库存分页查询。 */
    Page<BookInventory> page(BookInventoryPageParam param);

    /** 根据馆藏ID查询详情。 */
    BookInventory detail(String id);

    /** 新增馆藏，可借库存初始化为总库存，默认下架。 */
    void add(BookInventoryAddParam param);

    /** 调整总库存，保留已借出数量，总库存不得低于已借出数量。 */
    void adjustStock(BookInventoryAdjustStockParam param);

    /** 馆藏上架。 */
    void onShelf(String id);

    /** 馆藏下架，不影响已借出图书归还。 */
    void offShelf(String id);
}
