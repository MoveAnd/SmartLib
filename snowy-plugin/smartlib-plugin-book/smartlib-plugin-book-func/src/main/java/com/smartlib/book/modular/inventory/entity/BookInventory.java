package com.smartlib.book.modular.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

/**
 * 按出版版本管理的馆藏库存。
 */
@Getter
@Setter
@TableName("book_inventory")
public class BookInventory extends CommonEntity {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 馆藏ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /** 出版版本标识，逻辑关联 editions.key */
    @TableField(value = "edition_key")
    private String editionKey;

    /** 总库存 */
    @TableField(value = "total_stock")
    private Integer totalStock;

    /** 可借库存 */
    @TableField(value = "available_stock")
    private Integer availableStock;

    /** 上架状态：ON_SHELF上架，OFF_SHELF下架 */
    @TableField(value = "status")
    private String status;
}
