package DAO.cache;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisCacheManager {
  private static JedisPool jedisPool;
  private static Gson gson;
  private static final int TIMEOUT = 4000;

  public static void initializePool() {
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(128);
    poolConfig.setMaxIdle(50);
    poolConfig.setMinIdle(10);
    jedisPool = new JedisPool(poolConfig, "unknown", 0, TIMEOUT);
    gson = new Gson();
  }

  public static <T> void set(String key, T value){
    try(Jedis jedis = jedisPool.getResource()) {
      String jsonValue = gson.toJson(value);
      jedis.set(key, jsonValue);
    }
  }

  public static <T> T get(String key, Class<T> type) {
    try(Jedis jedis = jedisPool.getResource()) {
      String jsonValue = jedis.get(key);
      if(jsonValue == null){
        return null;
      }
      return gson.fromJson(jsonValue, type);
    }
  }

  public static void shutdown() {
    jedisPool.close();
  }
}
