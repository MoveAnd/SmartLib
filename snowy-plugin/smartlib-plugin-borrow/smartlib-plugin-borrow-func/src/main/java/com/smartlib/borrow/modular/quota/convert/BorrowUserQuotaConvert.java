package com.smartlib.borrow.modular.quota.convert;

import com.smartlib.borrow.modular.quota.entity.BorrowUserQuota;
import com.smartlib.borrow.modular.quota.param.BorrowUserQuotaAdjustLimitParam;
import com.smartlib.borrow.modular.quota.result.BorrowUserQuotaDetailResult;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface BorrowUserQuotaConvert {

    // 仅映射允许调整的字段；预期版本由服务设置。
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "borrowLimit", source = "borrowLimit")
    BorrowUserQuota toEntity(BorrowUserQuotaAdjustLimitParam param);

    // 剩余名额由服务计算，审计字段不进入接口返回对象。
    @Mapping(target = "remainingCount", ignore = true)
    BorrowUserQuotaDetailResult toResult(BorrowUserQuota quota);
}
