package proxy;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyFactory {

    private static final Map<Class<?>, Object>
            proxyMap = new ConcurrentHashMap<>();

    private static final Object lock = new Object();

    /**
     * 获取代理对象，双重检测锁保证线程安全
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T getProxy(Class<T> clazz) throws Exception {
        if (proxyMap.get(clazz) == null) {
            synchronized (lock) {
                if (proxyMap.get(clazz) == null) {
                    create(clazz);
                    return (T) proxyMap.get(clazz);
                } else {
                    return (T) proxyMap.get(clazz);
                }
            }
        } else {
            return (T) proxyMap.get(clazz);
        }
    }

    private static <T> void create(Class<T> clazz) throws Exception {
         proxyMap.put(clazz, Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class<?>[]{clazz}, new RpcClientDynamic<>(clazz)));
    }


}
