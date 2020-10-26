package com.ziggy.rpc.common.serialize;

import java.io.IOException;

public interface Serialize {
    /**
     *  序列化接口
     * @param obejct
     * @return
     * @throws IOException
     */
    byte[] serialize(Object obejct) throws IOException;

    /**
     *  反序列化接口
     * @param clazz
     * @param bytes
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;
}
