package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Collect;
import com.sakura.meetu.mapper.CollectMapper;
import com.sakura.meetu.service.ICollectService;
import com.sakura.meetu.vo.CollectVo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-09
 */
@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements ICollectService {

    private final CollectMapper collectMapper;

    public CollectServiceImpl(CollectMapper collectMapper) {
        this.collectMapper = collectMapper;
    }

    @Override
    public Object listPage(String name, Integer userId, Integer pageNum, Integer pageSize) {

        List<CollectVo> collectVos = collectMapper.selectAllByUser(userId, name);
        collectVos.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));

        int total = collectVos.size();

        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        List<CollectVo> result = collectVos.subList(startIndex, endIndex);

        HashMap<String, Object> data = new HashMap<>(8);
        data.put("total", total);
        data.put("records", result);

        return data;
    }
}
