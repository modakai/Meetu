package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Praise;
import com.sakura.meetu.mapper.PraiseMapper;
import com.sakura.meetu.service.IPraiseService;
import com.sakura.meetu.vo.PraiseVo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-06
 */
@Service
public class PraiseServiceImpl extends ServiceImpl<PraiseMapper, Praise> implements IPraiseService {

    private final PraiseMapper praiseMapper;

    public PraiseServiceImpl(PraiseMapper praiseMapper) {
        this.praiseMapper = praiseMapper;
    }

    @Override
    public Map<String, Object> listPage(Integer praiseId, String username, Integer pageNum, Integer pageSize) {

        List<PraiseVo> praiseList = praiseMapper.selectAllByUser(praiseId, username);
        praiseList.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));

        int total = praiseList.size();

        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        List<PraiseVo> result = praiseList.subList(startIndex, endIndex);

        HashMap<String, Object> data = new HashMap<>(8);
        data.put("total", total);
        data.put("records", result);

        return data;
    }
}
