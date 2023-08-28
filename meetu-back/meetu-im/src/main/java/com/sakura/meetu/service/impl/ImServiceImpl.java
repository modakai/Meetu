package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Im;
import com.sakura.meetu.entity.ImVo;
import com.sakura.meetu.mapper.ImMapper;
import com.sakura.meetu.service.IImService;
import com.sakura.meetu.utils.Result;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 聊天信息表 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-08-20
 */
@Service
public class ImServiceImpl extends ServiceImpl<ImMapper, Im> implements IImService {

    private final ImMapper imMapper;

    public ImServiceImpl(ImMapper imMapper) {
        this.imMapper = imMapper;
    }

    @Override
    public Result initMessageList(Integer limit) {

        if (limit > 120 || limit <= 0) {
            return Result.error(Result.CODE_ERROR_400, "参数有误");
        }

        // 要封装数据 但是级联查询好像用不了
        List<ImVo> result = imMapper.selectImMessageLimit(limit);
        // 排序
        result.sort(Comparator.comparing(ImVo::getId));

        return Result.success(result);
    }

    /**
     * <sql>
     * SELECT
     * im.id, im.uid, im.text, im.img, username
     * FROM
     * im
     * LEFT JOIN sys_user ON im.uid = sys_user.uid
     * </sql>
     *
     * @param username
     * @param text
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Result queryPage(String username, String text, Integer pageNum, Integer pageSize) {

        List<ImVo> imList = imMapper.selectAllWhere(username, text);
        // 分页
        int totalCount = imList.size(); // 总记录数

        // 计算起始索引和结束索引
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);

        List<ImVo> resultList = imList.subList(startIndex, endIndex); // 手动分页获取结果
        HashMap<String, Object> data = new HashMap<>(8);
        data.put("total", totalCount);
        data.put("records", resultList);

        return Result.success(data);
    }
}
