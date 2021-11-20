package llc.tranquil.jedis.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * Copyright (c) 2021 - Tranquil, LLC.
 *
 * @author 42 on Nov, 09, 2021 - 12:03 PM
 * @project Nexus
 */

@Getter
@AllArgsConstructor
public class PacketListenerData {

    private PacketListener listener;
    private Method method;
    private String id;

}
