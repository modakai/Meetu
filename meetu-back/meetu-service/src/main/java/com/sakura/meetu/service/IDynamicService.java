package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Dynamic;

import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-09-04
 */
public interface IDynamicService extends IService<Dynamic> {

    Dynamic findOneById(Integer id);

    Map<String, Object> listPage(String name, String type, Integer pageNum, Integer pageSize);
}
