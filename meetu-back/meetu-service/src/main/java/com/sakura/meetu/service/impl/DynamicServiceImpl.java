package com.sakura.meetu.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Dynamic;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.mapper.DynamicMapper;
import com.sakura.meetu.service.IDynamicService;
import com.sakura.meetu.utils.Result;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-04
 */
@Service
public class DynamicServiceImpl extends ServiceImpl<DynamicMapper, Dynamic> implements IDynamicService {

    @Override
    public Dynamic findOneById(Integer id) {
        // TODO 可以走缓存
        Dynamic dynamic = getById(id);
        if (dynamic == null) {
            throw new ServiceException(Result.CODE_ERROR_404, "查询的动态不存在");
        }

        IDynamicService proxy = (IDynamicService) AopContext.currentProxy();

        // 更新动态的浏览量
        ThreadUtil.execute(() -> {
            UpdateWrapper<Dynamic> updateWrapper = new UpdateWrapper<Dynamic>()
                    .eq("id", dynamic.getId())
                    .set("view", dynamic.getView() + 1);
            proxy.update(updateWrapper);
        });
        return dynamic;
    }
}
