package com.sakura.meetu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sakura.meetu.entity.Im;
import com.sakura.meetu.utils.Result;

/**
 * <p>
 * 聊天信息表 服务类
 * </p>
 *
 * @author sakura
 * @since 2023-08-20
 */
public interface IImService extends IService<Im> {

    Result initMessageList(Integer limit);

    Result queryPage(String username, String text, Integer pageNum, Integer pageSize);
}
