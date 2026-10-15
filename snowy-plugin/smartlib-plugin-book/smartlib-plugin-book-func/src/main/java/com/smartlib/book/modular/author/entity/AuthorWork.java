package com.smartlib.book.modular.author.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

/**
 * 作品与作者关系，联合主键为 (author_key, work_key)。
 * MyBatis-Plus 不支持联合主键：查询/更新/删除使用 Wrapper，不使用 ById 方法。
 * 逻辑删除记录仍占用该联合主键，delete_flag 不参与主键。
 */
@Getter
@Setter
@TableName("author_works")
public class AuthorWork extends CommonEntity {

    private static final long serialVersionUID = 1L;

    /** 作者标识，逻辑关联 authors.key；联合主键之一 */
    @TableField(value = "author_key")
    private String authorKey;

    /** 作品标识，逻辑关联 works.key；联合主键之一 */
    @TableField(value = "work_key")
    private String workKey;
}
