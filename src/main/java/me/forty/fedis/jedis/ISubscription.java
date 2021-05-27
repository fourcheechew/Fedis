package me.forty.fedis.jedis;

import com.google.gson.JsonObject;

public interface ISubscription {

    void handleMessage(String payload, JsonObject object);

    String[] subscriptionChannels();

}
