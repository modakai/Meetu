package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Dict;
import com.sakura.meetu.utils.Result;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-08-13
 */
public interface IDictService extends IService<Dict> {

    /**
     * 根据 type 类型获取数据库中的所有字典
     *
     * @param type 数据字典类型
     * @return
     */
    Result listTypeAll(String type);

    void deleteCache();
}
