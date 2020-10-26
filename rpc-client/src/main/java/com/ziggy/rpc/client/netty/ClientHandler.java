package com.ziggy.rpc.client.netty;

import com.ziggy.rpc.common.protocol.RpcRequest;
import com.ziggy.rpc.common.protocol.RpcResponse;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@ChannelHandler.Sharable
public class ClientHandler extends ChannelDuplexHandler {

    //记录每个请求id以及对应的future对象
    private final Map<String, CompletableFuture<RpcResponse>> futureMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RpcResponse) {
            RpcResponse response = (RpcResponse) msg;
            CompletableFuture<RpcResponse> future = futureMap.get(response.getRequestID());
            future.complete(response);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RpcRequest) {
            RpcRequest request = (RpcRequest) msg;
            futureMap.putIfAbsent(request.getRequestID(), new CompletableFuture<>());
        }
        super.write(ctx, msg, promise);
    }

    /**
     * 同步获取response
     * @param requestID
     * @return
     */
    public RpcResponse getResponseSyncronously(String requestID) {

        try {
            CompletableFuture<RpcResponse> future = futureMap.get(requestID);
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            futureMap.remove(requestID);
        }
        return null;
    }



    /**
     * 异步调用
     * @param requestID
     * @return
     */
    public CompletableFuture<RpcResponse> getResponseAsyncronously(String requestID){
        CompletableFuture<RpcResponse> future = futureMap.get(requestID);
        return future;
    }
}
