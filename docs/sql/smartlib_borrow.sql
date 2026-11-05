/*
 Navicat Premium Dump SQL

 Source Server         : MySQL8
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : 127.0.0.1:3306
 Source Schema         : smartlib_borrow

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 04/11/2026 22:28:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for book_borrow
-- ----------------------------
DROP TABLE IF EXISTS `book_borrow`;
CREATE TABLE `book_borrow`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `inventory_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '馆藏ID',
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'C端借阅人ID',
  `borrow_time` datetime NOT NULL COMMENT '借阅成功时间',
  `due_time` datetime NOT NULL COMMENT '应还时间',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '借阅状态：BORROWING未还，RETURNED已还',
  `return_time` datetime NULL DEFAULT NULL COMMENT '实际确认归还时间',
  `return_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实际确认人的B端用户ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '删除标志',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '借阅记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for book_borrow_stats
-- ----------------------------
DROP TABLE IF EXISTS `book_borrow_stats`;
CREATE TABLE `book_borrow_stats`  (
  `edition_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '出版版本标识，逻辑关联editions.key',
  `borrow_count` bigint NOT NULL DEFAULT 0 COMMENT '累计成功借阅次数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_DELETE' COMMENT '删除标志',
  PRIMARY KEY (`edition_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '出版版本累计借阅统计' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for book_return_apply
-- ----------------------------
DROP TABLE IF EXISTS `book_return_apply`;
CREATE TABLE `book_return_apply`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `borrow_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '借阅记录ID',
  `designated_return_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '指定的B端经办人ID',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请状态：PENDING待确认，CONFIRMED已确认',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '删除标志',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_book_return_apply_borrow_id`(`borrow_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '归还申请' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for borrow_user_quota
-- ----------------------------
DROP TABLE IF EXISTS `borrow_user_quota`;
CREATE TABLE `borrow_user_quota`  (
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'C端读者ID',
  `borrow_limit` int NOT NULL DEFAULT 0 COMMENT '个人借阅上限',
  `borrowed_count` int NOT NULL DEFAULT 0 COMMENT '未归还数量',
  `row_version` bigint NOT NULL DEFAULT 0 COMMENT '数据版本',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建用户',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `update_user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '修改用户',
  `delete_flag` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '删除标志',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '个人借阅名额' ROW_FORMAT = DYNAMIC;

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

SET FOREIGN_KEY_CHECKS = 1;
