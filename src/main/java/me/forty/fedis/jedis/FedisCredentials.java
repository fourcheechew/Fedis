package me.forty.fedis.jedis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FedisCredentials {

    private String address;
    private String password;
    private int port;

    public boolean isAuth() {
        return this.password != null && !this.password.isEmpty();
    }

}
