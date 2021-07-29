package me.forty.fedis.jedis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FedisCredentials {

    private String address;
    private String password;
    private int port;
    private int dbID;
    private int subID;

    public boolean isAuth() {
        return this.password != null && !this.password.isEmpty();
    }

}
