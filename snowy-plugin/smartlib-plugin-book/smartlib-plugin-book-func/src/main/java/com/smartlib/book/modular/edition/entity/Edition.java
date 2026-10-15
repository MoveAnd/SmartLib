package com.smartlib.book.modular.edition.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.common.pojo.CommonEntity;

import java.time.LocalDateTime;

/**
 * Open Library 出版版本。
 */
@Getter
@Setter
@TableName(value = "editions", autoResultMap = true)
public class Edition extends CommonEntity {

    private static final long serialVersionUID = 1L;

    /** 来源类型，如 /type/edition */
    @TableField(value = "type")
    private String type;

    /** 版本完整 OL 标识；key 是 MySQL 保留字，需反引号转义 */
    @TableId(value = "`key`", type = IdType.INPUT)
    private String key;

    /** 来源修订号；Long 覆盖 INT UNSIGNED 全范围 */
    @TableField(value = "revision")
    private Long revision;

    /** 来源修改时间，保留微秒；导入时已转为北京时间 */
    @TableField(value = "last_modified")
    private LocalDateTime lastModified;

    /** 完整来源版本对象；默认不加载，按需显式查询 */
    @TableField(value = "data", typeHandler = JacksonTypeHandler.class, select = false)
    private JsonNode data;

    /** 从 data.works[0].key 提取的作品标识，逻辑关联 works.key */
    @TableField(value = "work_key")
    private String workKey;

    /** 从 data.title 提取的版本书名，供展示和搜索 */
    @TableField(value = "title")
    private String title;

    /** 从 data.ocaid 提取的 Internet Archive 条目标识 */
    @TableField(value = "ocaid")
    private String ocaid;
}
