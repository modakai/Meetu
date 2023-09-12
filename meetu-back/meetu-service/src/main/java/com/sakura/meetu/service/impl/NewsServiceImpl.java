package com.sakura.meetu.service.impl;

import com.sakura.meetu.entity.News;
import com.sakura.meetu.mapper.NewsMapper;
import com.sakura.meetu.service.INewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-11
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements INewsService {

}
