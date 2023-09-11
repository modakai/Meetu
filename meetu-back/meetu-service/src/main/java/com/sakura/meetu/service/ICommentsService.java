package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Comments;
import com.sakura.meetu.utils.Result;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-09-10
 */
public interface ICommentsService extends IService<Comments> {

    List<Comments> listCommentsAndUser(Integer dynamicId);

    List<Comments> listCommentsTree(List<Comments> commentsList);

    Result listPage(String name, Integer pageNum, Integer pageSize);
}
