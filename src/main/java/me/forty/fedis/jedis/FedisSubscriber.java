package me.forty.fedis.jedis;

import com.google.gson.JsonObject;
import lombok.Getter;
import me.forty.fedis.Fedis;

@Getter
public class FedisSubscriber {

    private String id;
    private Fedis fedis;
    private ISubscription subscription;
    private String[] subscribedChannels;

    public FedisSubscriber(String id, Fedis fedis, ISubscription subscription) {
        this.id = id;
        this.fedis = fedis;
        this.subscription = subscription;
        this.subscribedChannels = subscription.subscriptionChannels();
        this.fedis.getSubscribers().add(this);
    }

    public void handleMessage(String channel, JsonObject jsonObject) {
        try {
            this.subscription.handleMessage(channel, jsonObject);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasChannel(String channel) {
        for (String subscribedChannel : this.subscribedChannels) {
            if (subscribedChannel.equalsIgnoreCase(channel)) {
                return true;
            }
        }
        return false;
    }
}
