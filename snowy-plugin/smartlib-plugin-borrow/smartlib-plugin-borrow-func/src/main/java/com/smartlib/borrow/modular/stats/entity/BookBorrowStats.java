package com.smartlib.borrow.modular.stats.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

/** 按出版版本累计的成功借阅次数。 */
@Getter
@Setter
@TableName("book_borrow_stats")
public class BookBorrowStats extends CommonEntity {
    private static final long serialVersionUID = 1L;

    @TableId(value = "edition_key", type = IdType.INPUT)
    private String editionKey;

    private Long borrowCount;
}
