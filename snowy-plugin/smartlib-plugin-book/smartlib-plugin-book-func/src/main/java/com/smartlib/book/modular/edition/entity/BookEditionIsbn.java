package com.smartlib.book.modular.edition.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

/**
 * 版本与 ISBN 别名关系，联合主键为 (edition_key, isbn)。
 * MyBatis-Plus 不支持联合主键：查询/更新/删除使用 Wrapper，不使用 ById 方法。
 * CHECK 仅校验长度；完整字符、前缀与校验位验证由导入器负责。
 */
@Getter
@Setter
@TableName("edition_isbns")
public class BookEditionIsbn extends CommonEntity {

    /** 序列化版本号 */
    private static final long serialVersionUID = 1L;

    /** 版本标识，逻辑关联 editions.key；联合主键之一 */
    @TableField(value = "edition_key")
    private String editionKey;

    /** 清洗并通过校验的 ISBN-10 或 ISBN-13；ISBN-10 的 X 使用大写；联合主键之一 */
    @TableField(value = "isbn")
    private String isbn;
}
