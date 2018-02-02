package com.site.utils;

import lombok.extern.log4j.Log4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Log4j
public final class RedisUtil {

    //Redis服务器IP
    private static String ADDR = "123.207.249.95";

    //Redis的端口号
    private static int PORT = 6379;

    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    private static int MAX_ACTIVE = 1024;

    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_IDLE = 200;

    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
//    private static int MAX_WAIT = 10000;

    private static int TIMEOUT = 5000;

    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static boolean TEST_ON_BORROW = true;

    private static JedisPool jedisPool = null;

    /**
     * 初始化Redis连接池
     */
    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(MAX_IDLE);
//            config.setMaxWaitMillis(MAX_WAIT);
            config.setMaxTotal(MAX_ACTIVE);
            config.setTestOnBorrow(TEST_ON_BORROW);
            //带上认证密码
//            jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT, String.valueOf(980325));
            jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 应该放在static代码块后面，否则线程池为空，redis也为null。
     */
    private static Jedis jedis = getJedis();

    /**
     * 获取Jedis实例
     */
    public synchronized static Jedis getJedis() {
        Jedis jedis = null;
        try {
            if (jedisPool != null) {
                jedis = jedisPool.getResource();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jedis;
    }

    /**
     * 释放jedis资源
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static String setcache(String key, String value) {
        try {
            log.info(jedis.isConnected());
            return jedis.set(key, value);
        } catch (Exception e) {
            log.error(" 【RedisUtil save exception】 key:" + key + "@value:" + value);
            try {
                jedis = getJedis();
                return setcache(key, value);
            } catch (Exception e1) {
                e1.printStackTrace();
                log.error(" 【RedisUtil getFactoryBean exception】    key:" + key + "@value:" + value);
            }
        }
        return null;
    }
}