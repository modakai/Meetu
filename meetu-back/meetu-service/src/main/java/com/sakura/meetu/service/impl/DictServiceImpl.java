package com.sakura.meetu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakura.meetu.constants.RedisKeyConstants;
import com.sakura.meetu.entity.Dict;
import com.sakura.meetu.mapper.DictMapper;
import com.sakura.meetu.service.IDictService;
import com.sakura.meetu.utils.JsonUtil;
import com.sakura.meetu.utils.LockUtil;
import com.sakura.meetu.utils.RedisUtils;
import com.sakura.meetu.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sakura
 * @since 2023-08-13
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements IDictService {
    private static final Logger log = LoggerFactory.getLogger(DictServiceImpl.class);

    private final RedisUtils redisUtils;

    private final DictMapper dictMapper;

    private final LockUtil lock;

    public DictServiceImpl(RedisUtils redisUtils, DictMapper dictMapper) {
        this.redisUtils = redisUtils;
        this.dictMapper = dictMapper;
        lock = new LockUtil(redisUtils);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Result listTypeAll(String type) {
        // 4 返回数据
        return Result.success(queryDictByTypeWithBreakdown(type));
    }

    @Override
    public void deleteCache() {
        redisUtils.delete(RedisKeyConstants.DICT_KEY);
    }

    private List<Dict> queryDictByTypeWithBreakdown(String type) {
        // 1 查看是否有缓存 (注意缓存穿透的问题以及缓存击穿)
        String dictKey = RedisKeyConstants.DICT_KEY;
        List<Dict> result;
        if (redisUtils.hExists(dictKey, type)) {
            return existsRedisDict(dictKey, type);
        }

        String lockDictKey = dictKey + type;

        // 缓存重建
        try {
            boolean isLock = lock.tryLock(lockDictKey);
            if (!isLock) {
                Thread.sleep(50);
                return queryDictByTypeWithBreakdown(type);
            }

            // 获取锁成功则再次判断缓存中是否存在数据
            if (redisUtils.hExists(dictKey, type)) {
                return existsRedisDict(dictKey, type);
            }

            // 查询数据库
            result = lambdaQuery().eq(Dict::getType, type).list();
            if (result.isEmpty()) {
                // 查询了数据库不存在的数据
                dictCacheReconstruction(dictKey, type, Collections.emptyList(), 1, TimeUnit.MINUTES);
                return null;
            }

            // 查询到数据
            dictCacheReconstruction(dictKey, type, result, 30, TimeUnit.DAYS);
        } catch (InterruptedException | RuntimeException e) {
            log.error("", e);
            throw new RuntimeException(e);
        } finally {
            lock.unLock(lockDictKey);
        }

        return result;
    }

    private List<Dict> existsRedisDict(String dictKey, String type) {

        return JsonUtil.toArrBean((String) redisUtils.hGet(dictKey, type), Dict.class);
    }

    private void dictCacheReconstruction(String key, String filed, Object value, long timeout, TimeUnit unit) {
        // 查询到数据
        redisUtils.hPut(key, filed, JsonUtil.toJson(value));
        redisUtils.expire(key, timeout, unit);
    }
}
