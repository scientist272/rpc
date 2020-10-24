package netty;

import common.RpcRequest;
import common.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest>
        implements ApplicationContextAware {

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private ApplicationContext applicationContext;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestID(msg.getRequestID());

        try {
            Object result = handle(msg);
            rpcResponse.setResult(result);
        }catch (Throwable throwable){
            logger.error("error occurred when invoke method",throwable);
            rpcResponse.setError(throwable.toString());
        }

        ctx.writeAndFlush(rpcResponse);
    }

    private Object handle(RpcRequest rpcRequest) throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = Class.forName(rpcRequest.getClassName());
        Object serviceInstance = applicationContext.getBean(clazz);
        logger.info("service bean:{}", serviceInstance);

        Method method = clazz
                .getDeclaredMethod(rpcRequest.getMethodName(),
                        rpcRequest.getParameterTypes());

        Object result = method.invoke(serviceInstance, rpcRequest.getParameters());

        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
