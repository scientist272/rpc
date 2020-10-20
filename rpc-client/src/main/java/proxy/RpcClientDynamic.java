package proxy;

import common.RpcRequest;
import common.RpcResponse;
import netty.NettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.UUID;

public class RpcClientDynamic<T> implements InvocationHandler {

    private Class<T> clazz;

    private final Logger logger = LoggerFactory.getLogger(RpcClientDynamic.class);

    public RpcClientDynamic(Class<T> clazz) throws Exception {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();

        String requestID = UUID.randomUUID().toString();

        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();

        Class<?>[] parameterTypes = method.getParameterTypes();

        request.setRequestID(requestID);
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(args);

        logger.info("client request:{}",request);

        Properties properties = PropertiesLoaderUtils
                .loadAllProperties("application.yaml");

        String host =  properties.getProperty("rpc.server.host");
        int port = Integer.parseInt(properties.getProperty("rpc.server.port"));

        NettyClient nettyClient = NettyClient.getClientInstance(host, port);

        RpcResponse response = nettyClient.syncSend(request);

        return response.getResult();
    }
}
