package llc.tranquil.jedis.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copyright (c) 2021 - Tranquil, LLC.
 *
 * @author 42 on Nov, 09, 2021 - 11:12 AM
 * @project Nexus
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PacketHandler {

    String value();

}
