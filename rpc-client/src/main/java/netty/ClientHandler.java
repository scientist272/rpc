package netty;

import common.RpcRequest;
import common.RpcResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends ChannelDuplexHandler {

    //记录每个请求id以及对应的future对象
    private final Map<String, DefaultFuture> futureMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcResponse) {
            RpcResponse response = (RpcResponse) msg;
            DefaultFuture future = futureMap.get(response.getRequestID());
            future.setResponse(response);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RpcRequest) {
            RpcRequest request = (RpcRequest) msg;
            futureMap.putIfAbsent(request.getRequestID(), new DefaultFuture());
        }
        super.write(ctx, msg, promise);
    }

    //获取response
    public RpcResponse getResponse(String requestID) {
        try {
            DefaultFuture future = futureMap.get(requestID);
            return future.getRpcResponse(5000);
        } finally {
            futureMap.remove(requestID);
        }
    }
}
