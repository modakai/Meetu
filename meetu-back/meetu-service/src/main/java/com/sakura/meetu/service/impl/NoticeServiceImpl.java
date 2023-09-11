package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.entity.Notice;
import com.sakura.meetu.mapper.NoticeMapper;
import com.sakura.meetu.service.INoticeService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-08-31
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {

}
