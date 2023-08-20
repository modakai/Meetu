package com.sakura.meetu.utils;

import cn.hutool.core.util.BooleanUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author sakura
 * @date 2023/8/13 19:19:14 周日
 */
public class LockUtil {

    private final RedisUtils redisUtils;

    public LockUtil(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * 抢锁
     *
     * @param key 锁的key
     * @return 是否抢到
     */
    public boolean tryLock(String key) {
        Boolean isLock = redisUtils.setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(isLock);
    }

    /**
     * 释放锁
     *
     * @param key 锁的 key
     */
    public void unLock(String key) {
        redisUtils.delete(key);
    }
}
