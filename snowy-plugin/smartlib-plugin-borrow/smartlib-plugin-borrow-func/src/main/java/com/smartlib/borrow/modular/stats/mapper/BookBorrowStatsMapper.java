package com.smartlib.borrow.modular.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.smartlib.borrow.modular.stats.entity.BookBorrowStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 出版版本借阅统计持久化接口。
 */
public interface BookBorrowStatsMapper extends BaseMapper<BookBorrowStats> {

    /**
     * 原子累加借阅次数，实体参数用于填充公共审计字段。
     */
    int addSuccessfulBorrows(@Param(Constants.ENTITY) BookBorrowStats stats, @Param("count") int count);

    /**
     * 查询榜单需要的出版版本标识和累计借阅次数。
     */
    List<BookBorrowStats> selectBorrowTop10();
}
