package com.smartlib.borrow.modular.quota.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.smartlib.borrow.modular.quota.entity.BorrowUserQuota;
import org.apache.ibatis.annotations.Param;

/** 个人借阅名额持久化接口。 */
public interface BorrowUserQuotaMapper extends BaseMapper<BorrowUserQuota> {

    /** 在剩余额度足够时原子占用名额，实体参数用于填充公共审计字段。 */
    int occupyQuota(@Param(Constants.ENTITY) BorrowUserQuota quota, @Param("count") int count);
}
