package serialize;

import com.alibaba.fastjson.JSON;

import java.io.IOException;


/**
 * JSON序列化器
 */
public class JSONSerializer implements Serialize {
    @Override
    public byte[] serialize(Object obejct) throws IOException {
        return JSON.toJSONBytes(obejct);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        return JSON.parseObject(bytes, clazz);
    }
}
