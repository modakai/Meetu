package com.sakura.meetu.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Dynamic;
import com.sakura.meetu.entity.User;
import com.sakura.meetu.exception.ServiceException;
import com.sakura.meetu.mapper.DynamicMapper;
import com.sakura.meetu.service.IDynamicService;
import com.sakura.meetu.utils.Result;
import com.sakura.meetu.utils.SessionUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    private final DynamicMapper dynamicMapper;

    public DynamicServiceImpl(DynamicMapper dynamicMapper) {
        this.dynamicMapper = dynamicMapper;
    }

    @Override
    public List<Dict> echartsDynamicTag() {
        List<Dict> list = CollUtil.newArrayList();
        List<Dynamic> dynamics = dynamicMapper.selectList(null);
        Set<String> tagsSet = new HashSet<>();
        dynamics.stream().map(Dynamic::getTags).forEach(tagsList -> {
            if (tagsList != null) {
                tagsSet.addAll(tagsList);
            }
        });
        for (String tag : tagsSet) {
            Dict dict = Dict.create();
            dict.set("name", tag).set("value", dynamics.stream().filter(dynamic -> dynamic.getTags() != null && dynamic.getTags().contains(tag)).count());
            list.add(dict);
        }
        return list;
    }

    @Override
    public Dynamic findOneById(Integer id) {
        // TODO 可以走缓存
        Dynamic dynamic = dynamicMapper
                .selectALLBYUserIdOrDynamicId(id, null, null).stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException(Result.CODE_ERROR_404, "查询动态不存在！"));


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

    @Override
    public Map<String, Object> listPage(String name, String type, Integer pageNum, Integer pageSize) {
        Integer userId = null;

        User currentUser = SessionUtils.getUser();  // 获取当前登录的用户信息
        if (currentUser != null) {
            String role = currentUser.getRole();  // ADMIN   USER   TEACHER
            if ("user".equals(type) && "USER".equals(role)) {  // 如果type是user，表示筛选用户自己的数据
                userId = currentUser.getId();
            }
        }

        List<Dynamic> pendingProcessingData = dynamicMapper.selectALLBYUserIdOrDynamicId(null, userId, name);
        pendingProcessingData.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));

        int total = pendingProcessingData.size();

        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        List<Dynamic> result = pendingProcessingData.subList(startIndex, endIndex);

        HashMap<String, Object> data = new HashMap<>(8);
        data.put("total", total);
        data.put("records", result);

        return data;
    }

    @Override
    public List<Dynamic> listHotDynamic() {
        return dynamicMapper.selectHotAll().stream()
                .sorted((o1, o2) -> o2.getHot().compareTo(o1.getHot()))
                .limit(8).collect(Collectors.toList());
    }

    @Override
    public List<Dict> echartsDynamicCount() {
        List<Dict> list = CollUtil.newArrayList();
        List<Map<String, Long>> dynamicCountData = dynamicMapper.analysisDynamic();
        for (Map<String, Long> data : dynamicCountData) {
            Dict dict = Dict.create();
            dict.set("name", data.get("name")).set("value", data.get("value"));
            list.add(dict);
        }
        return list.stream().sorted(Comparator.comparing(dict -> dict.getStr("name"))).collect(Collectors.toList());
    }
}
