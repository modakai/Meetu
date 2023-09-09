package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Praise;

import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-09-06
 */
public interface IPraiseService extends IService<Praise> {

    Map<String, Object> listPage(Integer praiseId, String username, Integer pageNum, Integer pageSize);
}
