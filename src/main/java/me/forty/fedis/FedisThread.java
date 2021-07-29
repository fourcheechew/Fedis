package me.forty.fedis;

import me.forty.fedis.jedis.FedisPubSub;
import redis.clients.jedis.Jedis;

public class FedisThread extends Thread {

    private Fedis fedis;

    public FedisThread(Fedis fedis) {
        this.fedis = fedis;
        setDaemon(true);
        setName("Fedis - Sub Thread");
    }

    @Override
    public void run() {
        try (Jedis jedis = fedis.getSubPool().getResource()) {
            jedis.subscribe(new FedisPubSub(fedis), fedis.getChannels());
        } catch (Exception e) {} //blank cause bitches
    }
}
