package com.sakura.meetu.service.impl;

import com.sakura.meetu.entity.Im;
import com.sakura.meetu.mapper.ImMapper;
import com.sakura.meetu.service.IImService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
