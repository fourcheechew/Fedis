package llc.tranquil.jedis;

import redis.clients.jedis.Jedis;

/**
 * Copyright (c) 2021 - Tranquil, LLC.
 *
 * @author 42 on Nov, 09, 2021 - 11:25 AM
 * @project Nexus
 */
public interface JedisCommand<T> {

    T execute(Jedis jedis);

}
