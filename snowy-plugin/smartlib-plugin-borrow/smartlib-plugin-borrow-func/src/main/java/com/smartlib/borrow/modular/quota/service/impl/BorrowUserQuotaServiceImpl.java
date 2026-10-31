package com.smartlib.borrow.modular.quota.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartlib.borrow.modular.quota.convert.BorrowUserQuotaConvert;
import com.smartlib.borrow.modular.quota.entity.BorrowUserQuota;
import com.smartlib.borrow.modular.quota.mapper.BorrowUserQuotaMapper;
import com.smartlib.borrow.modular.quota.param.BorrowUserQuotaAdjustLimitParam;
import com.smartlib.borrow.modular.quota.result.BorrowUserQuotaDetailResult;
import com.smartlib.borrow.modular.quota.service.BorrowUserQuotaService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.exception.CommonException;

@Service
public class BorrowUserQuotaServiceImpl extends ServiceImpl<BorrowUserQuotaMapper, BorrowUserQuota>
        implements BorrowUserQuotaService {

    @Resource
    private BorrowUserQuotaConvert borrowUserQuotaConvert;

    @Override
    public BorrowUserQuotaDetailResult detail(String userId) {
        BorrowUserQuota quota = this.getById(userId);
        if (quota == null) {
            quota = new BorrowUserQuota();
            quota.setUserId(userId);
            quota.setBorrowLimit(0);
            quota.setBorrowedCount(0);
            quota.setRowVersion(0L);
        }
        BorrowUserQuotaDetailResult result = borrowUserQuotaConvert.toResult(quota);
        result.setRemainingCount(Math.max(result.getBorrowLimit() - result.getBorrowedCount(), 0));
        return result;
    }

    @Override
    public void initialize(String userId) {
        if (this.getById(userId) == null) {
            BorrowUserQuota quota = new BorrowUserQuota();
            quota.setUserId(userId);
            quota.setBorrowLimit(5);
            quota.setBorrowedCount(0);
            quota.setRowVersion(0L);
            try {
                this.save(quota);
            } catch (DuplicateKeyException e) {
                // 并发初始化由主键兜底；在失败的插入结束后读取已提交记录。
                if (this.getById(userId) == null) {
                    throw e;
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BorrowUserQuotaDetailResult adjustLimit(BorrowUserQuotaAdjustLimitParam param) {
        BorrowUserQuota quota = this.getById(param.getUserId());
        if (quota == null) {
            throw new CommonException("借阅名额记录不存在，userId值为：{}", param.getUserId());
        }
        BorrowUserQuota update = borrowUserQuotaConvert.toEntity(param);
        update.setRowVersion(param.getExpectedVersion());
        if (!this.updateById(update)) {
            throw new CommonException("借阅名额版本冲突，请重新查询后提交");
        }
        return detail(param.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void occupyQuota(String userId, int count) {
        if (count <= 0) {
            throw new CommonException("借阅数量必须大于0");
        }
        BorrowUserQuota quota = new BorrowUserQuota();
        quota.setUserId(userId);
        if (this.baseMapper.occupyQuota(quota, count) != 1) {
            throw new CommonException("借阅名额不存在或剩余名额不足");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseQuota(String userId) {
        if (!this.update(new BorrowUserQuota(), new LambdaUpdateWrapper<BorrowUserQuota>()
                .eq(BorrowUserQuota::getUserId, userId)
                .gt(BorrowUserQuota::getBorrowedCount, 0)
                .setSql("borrowed_count = borrowed_count - 1, row_version = row_version + 1"))) {
            throw new CommonException("借阅名额记录不存在或未归还数量异常");
        }
    }
}
