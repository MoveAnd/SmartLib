/*
 Navicat Premium Dump SQL

 Source Server         : MySQL8
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : 127.0.0.1:3306
 Source Schema         : smartlib_book

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 29/10/2026 18:31:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for author_works
-- ----------------------------
DROP TABLE IF EXISTS `author_works`;
CREATE TABLE `author_works`  (
  `author_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '作者标识，逻辑关联 authors.key',
  `work_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '作品标识，逻辑关联 works.key',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_DELETE' COMMENT '删除标志',
  PRIMARY KEY (`author_key`, `work_key`) USING BTREE,
  INDEX `idx_author_works_work_key`(`work_key` ASC) USING BTREE,
  CONSTRAINT `ck_author_works_delete_flag` CHECK (`delete_flag` in (_utf8mb4'NOT_DELETE',_utf8mb4'DELETED'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作品与作者关系' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for authors
-- ----------------------------
DROP TABLE IF EXISTS `authors`;
CREATE TABLE `authors`  (
  `type` varchar(32) CHARACTER SET ascii COLLATE ascii_bin NULL DEFAULT NULL COMMENT '来源类型，如 /type/author',
  `key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '作者完整 OL 标识；主键',
  `revision` int UNSIGNED NULL DEFAULT NULL COMMENT '来源记录修订号',
  `last_modified` datetime(6) NULL DEFAULT NULL COMMENT '来源最后修改时间，保留微秒；导入时统一时间语义',
  `data` json NULL COMMENT '完整来源作者对象，包含姓名、别名、简介等',
  `name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '从 data.name 提取的姓名，供展示和搜索',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_DELETE' COMMENT '删除标志',
  PRIMARY KEY (`key`) USING BTREE,
  INDEX `idx_authors_delete_flag`(`delete_flag` ASC) USING BTREE,
  FULLTEXT INDEX `ft_authors_name`(`name`) WITH PARSER `ngram`,
  CONSTRAINT `ck_authors_data_object` CHECK ((`data` is null) or (json_type(`data`) = _utf8mb4'OBJECT')),
  CONSTRAINT `ck_authors_delete_flag` CHECK (`delete_flag` in (_utf8mb4'NOT_DELETE',_utf8mb4'DELETED'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Open Library 作者' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for book_inventory
-- ----------------------------
DROP TABLE IF EXISTS `book_inventory`;
CREATE TABLE `book_inventory`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `edition_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '出版版本标识，逻辑关联 editions.key',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `available_stock` int NOT NULL DEFAULT 0 COMMENT '可借库存',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OFF_SHELF' COMMENT '上架状态：ON_SHELF上架，OFF_SHELF下架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_DELETE' COMMENT '删除标志',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_book_inventory_edition_key`(`edition_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '馆藏版本库存' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for edition_isbns
-- ----------------------------
DROP TABLE IF EXISTS `edition_isbns`;
CREATE TABLE `edition_isbns`  (
  `edition_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '版本标识，逻辑关联 editions.key',
  `isbn` varchar(13) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '清洗并通过校验的 ISBN-10 或 ISBN-13；ISBN-10 的 X 使用大写',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_DELETE' COMMENT '删除标志',
  PRIMARY KEY (`edition_key`, `isbn`) USING BTREE,
  INDEX `idx_edition_isbns_isbn`(`isbn` ASC) USING BTREE,
  CONSTRAINT `ck_edition_isbns_delete_flag` CHECK (`delete_flag` in (_utf8mb4'NOT_DELETE',_utf8mb4'DELETED')),
  CONSTRAINT `ck_edition_isbns_length` CHECK (char_length(`isbn`) in (10,13))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '版本与 ISBN 别名关系' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for editions
-- ----------------------------
DROP TABLE IF EXISTS `editions`;
CREATE TABLE `editions`  (
  `type` varchar(32) CHARACTER SET ascii COLLATE ascii_bin NULL DEFAULT NULL COMMENT '来源类型，如 /type/edition',
  `key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '版本完整 OL 标识；主键',
  `revision` int UNSIGNED NULL DEFAULT NULL COMMENT '来源记录修订号',
  `last_modified` datetime(6) NULL DEFAULT NULL COMMENT '来源最后修改时间，保留微秒；导入时统一时间语义',
  `data` json NULL COMMENT '完整来源版本对象，包含出版社、ISBN、封面、语言等',
  `work_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NULL DEFAULT NULL COMMENT '从 data.works[0].key 提取的作品标识，逻辑关联 works.key',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '从 data.title 提取的版本书名，供展示和搜索',
  `ocaid` varchar(255) CHARACTER SET ascii COLLATE ascii_bin NULL DEFAULT NULL COMMENT '从 data.ocaid 提取的 Internet Archive 条目标识',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_DELETE' COMMENT '删除标志',
  PRIMARY KEY (`key`) USING BTREE,
  INDEX `idx_editions_work_key`(`work_key` ASC) USING BTREE,
  INDEX `idx_editions_ocaid`(`ocaid` ASC) USING BTREE,
  FULLTEXT INDEX `ft_editions_title`(`title`) WITH PARSER `ngram`,
  CONSTRAINT `ck_editions_data_object` CHECK ((`data` is null) or (json_type(`data`) = _utf8mb4'OBJECT')),
  CONSTRAINT `ck_editions_delete_flag` CHECK (`delete_flag` in (_utf8mb4'NOT_DELETE',_utf8mb4'DELETED'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Open Library 出版版本' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for idempotent_record
-- ----------------------------
DROP TABLE IF EXISTS `idempotent_record`;
CREATE TABLE `idempotent_record`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `operation` varchar(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '操作类型',
  `caller_scope` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '调用方范围：C:用户ID、B:用户ID、SERVICE:服务标识',
  `idempotency_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '幂等键：请求号或内部业务标识',
  `request_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '规范化业务参数的SHA-256摘要',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态：PROCESSING处理中，EXECUTED本地已执行，SUCCESS成功，ROLLED_BACK已回滚',
  `result_json` json NULL COMMENT '响应结果：code、msg、data',
  `global_xid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前执行的Seata全局事务ID',
  `processing_deadline` datetime NOT NULL COMMENT '处理超时核查时间，不代表可直接重试',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '删除标志',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_idempotent_record_scope_operation_key`(`caller_scope` ASC, `operation` ASC, `idempotency_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通用幂等记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `branch_id` bigint NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'undo_log context',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE INDEX `ux_undo_log`(`xid` ASC, `branch_id` ASC) USING BTREE,
  INDEX `ix_log_created`(`log_created` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AT transaction mode undo table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for works
-- ----------------------------
DROP TABLE IF EXISTS `works`;
CREATE TABLE `works`  (
  `type` varchar(32) CHARACTER SET ascii COLLATE ascii_bin NULL DEFAULT NULL COMMENT '来源类型，如 /type/work',
  `key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '作品完整 OL 标识；主键',
  `revision` int UNSIGNED NULL DEFAULT NULL COMMENT '来源记录修订号',
  `last_modified` datetime(6) NULL DEFAULT NULL COMMENT '来源最后修改时间，保留微秒；导入时统一时间语义',
  `data` json NULL COMMENT '完整来源作品对象，包含标题、简介、主题及作者引用等',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '从 data.title 提取的作品标题，供展示和搜索',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_DELETE' COMMENT '删除标志',
  PRIMARY KEY (`key`) USING BTREE,
  INDEX `idx_works_delete_flag`(`delete_flag` ASC) USING BTREE,
  FULLTEXT INDEX `ft_works_title`(`title`) WITH PARSER `ngram`,
  CONSTRAINT `ck_works_data_object` CHECK ((`data` is null) or (json_type(`data`) = _utf8mb4'OBJECT')),
  CONSTRAINT `ck_works_delete_flag` CHECK (`delete_flag` in (_utf8mb4'NOT_DELETE',_utf8mb4'DELETED'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Open Library 作品' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
