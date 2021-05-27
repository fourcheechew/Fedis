package me.forty.fedis.jedis;

import redis.clients.jedis.Jedis;

public interface IFedisCommand<T> {

    T execute(Jedis jedis);

}
