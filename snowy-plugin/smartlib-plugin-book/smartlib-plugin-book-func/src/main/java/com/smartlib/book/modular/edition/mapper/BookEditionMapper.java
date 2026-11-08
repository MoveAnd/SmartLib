package com.smartlib.book.modular.edition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartlib.book.modular.edition.entity.BookEdition;
import org.apache.ibatis.annotations.Param;

/** editions 基础持久化接口。 */
public interface BookEditionMapper extends BaseMapper<BookEdition> {

    /** 在编辑事务中锁定有效版本，读取完整 JSON，避免并发合并覆盖。 */
    BookEdition selectForEdit(@Param("key") String key);
}
