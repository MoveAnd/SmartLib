package com.smartlib.book.modular.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartlib.book.modular.work.entity.BookWork;
import com.smartlib.book.modular.work.param.BookWorkPageParam;
import org.apache.ibatis.annotations.Param;

/**
 * works 基础持久化接口。
 */
public interface BookWorkMapper extends BaseMapper<BookWork> {

    /**
     * 根据标题、作者和白名单排序条件分页查询作品摘要字段。
     */
    Page<BookWork> selectWorkPage(@Param("page") Page<BookWork> page,
                                  @Param("param") BookWorkPageParam param);

    /**
     * 根据与分页相同的筛选条件统计未删除的作品数量。
     */
    long countWorks(@Param("param") BookWorkPageParam param);
}
