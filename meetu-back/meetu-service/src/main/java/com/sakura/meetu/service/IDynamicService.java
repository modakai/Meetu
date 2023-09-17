package com.sakura.meetu.service;

import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Dynamic;

import java.util.List;
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

    List<Dict> echartsDynamicTag();

    Dynamic findOneById(Integer id);

    Map<String, Object> listPage(String name, String type, Integer pageNum, Integer pageSize);

    List<Dynamic> listHotDynamic();

    List<Dict> echartsDynamicCount();
}
