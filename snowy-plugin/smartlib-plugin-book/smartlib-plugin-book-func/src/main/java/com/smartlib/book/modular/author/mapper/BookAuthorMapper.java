package com.smartlib.book.modular.author.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlib.book.modular.author.entity.BookAuthor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * authors 基础持久化接口。
 */
public interface BookAuthorMapper extends BaseMapper<BookAuthor> {

    /**
     * 根据作品标识查询未删除的作者，按作者标识升序排列。
     */
    List<BookAuthor> selectAuthorsByWorkKey(@Param("workKey") String workKey);
}
