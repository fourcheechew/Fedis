package llc.tranquil.jedis.packet;

import lombok.Getter;

/**
 * Copyright (c) 2021 - Tranquil, LLC.
 *
 * @author 42 on Nov, 09, 2021 - 11:19 AM
 * @project Nexus
 */

@Getter
public class Packet {

    private String id;
    private Object[] data;

    public Packet(String id, Object... data) {
        this.id = id;
        this.data = data;
    }

}
