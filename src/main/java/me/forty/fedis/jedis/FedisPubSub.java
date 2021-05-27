package me.forty.fedis.jedis;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import me.forty.fedis.Fedis;
import redis.clients.jedis.JedisPubSub;

@AllArgsConstructor
public class FedisPubSub extends JedisPubSub {

    private Fedis fedis;

    @Override
    public void onMessage(String channel, String message) {
        JsonObject object = Fedis.PARSER.parse(message).getAsJsonObject();
        String payload = object.get("payload").getAsString();
        JsonObject data = object.get("data").getAsJsonObject();
        fedis.getSubscribers().forEach(jedisSubscriber -> {
            if (jedisSubscriber.hasChannel(channel)) {
                jedisSubscriber.handleMessage(payload, data);
            }
            return;
        });
        super.onMessage(channel, message);
    }
}
