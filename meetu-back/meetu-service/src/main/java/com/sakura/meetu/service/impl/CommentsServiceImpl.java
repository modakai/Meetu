package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Comments;
import com.sakura.meetu.mapper.CommentsMapper;
import com.sakura.meetu.service.ICommentsService;
import com.sakura.meetu.utils.Result;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-10
 */
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements ICommentsService {

    private final CommentsMapper commentsMapper;

    public CommentsServiceImpl(CommentsMapper commentsMapper) {
        this.commentsMapper = commentsMapper;
    }

    @Override
    public List<Comments> listCommentsAndUser(Integer dynamicId) {
        List<Comments> commentsList = commentsMapper.selectAllByDynamicId(dynamicId);
        return listCommentsTree(commentsList);
    }

    @Override
    public List<Comments> listCommentsTree(List<Comments> commentsList) {
        List<Comments> level1 = new ArrayList<>();
        Map<Integer, List<Comments>> commentMap = new HashMap<>();

        for (Comments comment : commentsList) {
            if (comment.getPid() == null) {
                level1.add(comment);
            } else {
                commentMap.putIfAbsent(comment.getPid(), new ArrayList<>());
                commentMap.get(comment.getPid()).add(comment);
            }
        }
        // 树化 二级
        for (Comments comment : level1) {
            List<Comments> level2 = commentMap.getOrDefault(comment.getId(), new ArrayList<>());

//            for (Comments level2Comments : level2) {
//                level2Comments.setPName(comment.getName());
//            }

            comment.setChildren(level2);
        }

        return level1;
    }

    @Override
    public Result listPage(String name, Integer pageNum, Integer pageSize) {
        List<Comments> comments = commentsMapper.selectAllByDynamicName(name);
        comments.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));

        int total = comments.size();

        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        List<Comments> result = comments.subList(startIndex, endIndex);

        HashMap<String, Object> data = new HashMap<>(8);
        data.put("total", total);
        data.put("records", result);
        return Result.success(data);
    }
}
