package com.sakura.meetu.service.impl;

import com.sakura.meetu.entity.Log;
import com.sakura.meetu.mapper.LogMapper;
import com.sakura.meetu.service.ILogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-03
 */
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log> implements ILogService {

}
