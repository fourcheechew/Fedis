package me.forty.fedis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import me.forty.fedis.jedis.FedisCredentials;
import me.forty.fedis.jedis.FedisPubSub;
import me.forty.fedis.jedis.FedisSubscriber;
import me.forty.fedis.jedis.IFedisCommand;
import me.forty.fedis.util.TypeCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Fedis {

    public static JsonParser PARSER = new JsonParser();
    private JedisPool pool;
    private JedisPool subPool;
    private Thread thread;
    private FedisCredentials fedisCredentials;
    private List<FedisSubscriber> subscribers = new ArrayList<>();
    private String[] channels;

    public Fedis(FedisCredentials fedisCredentials, String... channels) {
        this.fedisCredentials = fedisCredentials;
        this.channels = channels;
        this.pool = new JedisPool(new JedisPoolConfig(), fedisCredentials.getAddress(), fedisCredentials.getPort(), 20000, fedisCredentials.isAuth() ? fedisCredentials.getPassword() : null, fedisCredentials.getDbID());
        this.subPool = new JedisPool(new JedisPoolConfig(), fedisCredentials.getAddress(), fedisCredentials.getPort(), 20000, fedisCredentials.isAuth() ? fedisCredentials.getPassword() : null, fedisCredentials.getSubID());
        try (Jedis jedis = this.subPool.getResource()) {
            attemptAuth(jedis);
            this.thread = new FedisThread(this);
            thread.start();
        }

    }

    public void close() {
        if (this.thread.isAlive()) {
            this.thread.stop();
        }
        this.subscribers.clear();
        if (!this.pool.isClosed()) {
            this.pool.close();
        }
    }

    public boolean isActive() {
        return this.pool != null && !this.pool.isClosed();
    }

    public void attemptAuth(Jedis jedis) {
        if (this.fedisCredentials.isAuth()) {
            jedis.auth(this.fedisCredentials.getPassword());
        }
    }

    public void write(String payloadID, JsonObject data, String channel) {
        JsonObject object = new JsonObject();
        object.addProperty("payload", payloadID);
        object.add("data", (data == null) ? new JsonObject() : data);
        this.runCommand(redis -> {
            this.attemptAuth(redis);
            redis.publish(channel, object.toString());
            return redis;
        });
    }

    public void writeSync(String payloadID, JsonObject data, String channel) {
        JsonObject object = new JsonObject();
        object.addProperty("payload", payloadID);
        object.add("data", (data == null) ? new JsonObject() : data);
        this.runCommandSync(redis -> {
            this.attemptAuth(redis);
            redis.publish(channel, object.toString());
            return redis;
        });
    }

    public <T> void runCommand(IFedisCommand<T> redisCommand) {
        this.runCommand(redisCommand, null);
    }

    public <T> void runCommand(IFedisCommand<T> redisCommand, TypeCallback<T> callback) {
        Jedis jedis = this.pool.getResource();
        T result = null;

        try {
            result = redisCommand.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();

            if (jedis != null) {
                this.pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (jedis != null) {
                this.pool.returnResource(jedis);
            }
        }
        if (callback != null) {
            callback.callback(result);
        }
    }

    public <T> T runCommandSync(IFedisCommand<T> redisCommand) {
        Jedis jedis = this.pool.getResource();
        T result;
        try {
            result = redisCommand.execute(jedis);
        } finally {
            if (jedis.isConnected()) {
                jedis.close();
            }
        }
        return result;
    }

}