package me.forty.fedis.util;

import java.io.Serializable;

public interface TypeCallback<T> extends Serializable {
    void callback(T t);
}
