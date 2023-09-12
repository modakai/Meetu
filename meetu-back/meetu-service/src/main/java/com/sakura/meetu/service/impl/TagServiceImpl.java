package com.sakura.meetu.service.impl;

import com.sakura.meetu.entity.Tag;
import com.sakura.meetu.mapper.TagMapper;
import com.sakura.meetu.service.ITagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-09-12
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

}
