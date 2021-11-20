package llc.tranquil.jedis;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import llc.tranquil.jedis.packet.*;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

/**
 * Copyright (c) 2021 - Tranquil, LLC.
 *
 * @author 42businessmc@gmail.com
 * @date 11/14/2021
 * @project Nexus
 */

@Getter
public class JedisInstance {

    private final String channel;
    private final JedisPool pool;
    private final Gson gson;

    protected JedisPubSub jedisPubSub = null;
    private final Map<String, List<PacketListenerData>> listeners = new HashMap<>();

    private boolean debug;
    private boolean async;

    public JedisInstance(String channel, JedisPool pool, Gson gson, boolean debug, boolean async) {
        this.channel = channel;
        this.pool = pool;
        this.gson = gson;
        this.debug = debug;
        this.async = async;

        setupPubSub();
    }

    public <T> T runCommand(JedisCommand<T> redisCommand) {
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

        return result;
    }

    public void runCommandNoReturn(JedisCommand command) {
        Jedis jedis = this.pool.getResource();
        try {
            command.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(Packet packet) {
        try (Jedis jedis = pool.getResource()) {
            String data = gson.toJsonTree(packet.getData()).toString();
            jedis.publish(channel, packet.getId() + ";" + data.substring(1, data.length() - 1));
            if (debug) Logger.getGlobal().severe("[Nexus] Sent packet " + packet.getId() + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerListener(PacketListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.getDeclaredAnnotation(PacketHandler.class) != null && method.getParameters().length > 0) {
                if (!JsonObject.class.isAssignableFrom(method.getParameters()[0].getType())) {
                    throw new IllegalStateException("First parameter should be of JsonObject type");
                }

                String id = method.getDeclaredAnnotation(PacketHandler.class).value();
                listeners.putIfAbsent(id, new ArrayList<>());
                listeners.get(id).add(new PacketListenerData(listener, method, id));
                if (debug) Logger.getGlobal().severe("[Nexus] Added listener for " + id + ".");
            }
        }
    }

    private void setupPubSub() {
        this.jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equalsIgnoreCase(getChannel()) || channel.equalsIgnoreCase("global")) {
                    try {
                        int breakAt = message.indexOf(";");
                        String id = message.substring(0, breakAt);

                        if (listeners.containsKey(id)) {
                            JsonObject data = gson.fromJson(message.substring(breakAt + 1), JsonObject.class);

                            for (PacketListenerData listenerData : listeners.get(id)) {
                                listenerData.getMethod().invoke(listenerData.getListener(), data);
                            }

                        }
                    } catch (JsonParseException e) {
                        Logger.getGlobal().severe("[Nexus] Failed to parse message into JSON");
                        e.printStackTrace();
                    } catch (Exception e) {
                        Logger.getGlobal().severe("[Nexus] Failed to handle message");
                        e.printStackTrace();
                    }
                }
            }
        };

        if (async) {
            ForkJoinPool.commonPool().execute(() -> pool.getResource().subscribe(this.jedisPubSub, this.channel, "global"));
        } else {
            pool.getResource().subscribe(this.jedisPubSub, this.channel, "global");
        }

    }

}
