package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Collect;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-09-09
 */
public interface ICollectService extends IService<Collect> {


    Object listPage(String name, Integer userId, Integer pageNum, Integer pageSize);
}
