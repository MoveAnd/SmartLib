package com.smartlib.borrow.modular.idempotent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlib.borrow.modular.idempotent.entity.IdempotentRecord;

/** 通用幂等记录持久化接口。 */
public interface IdempotentRecordMapper extends BaseMapper<IdempotentRecord> {
}
