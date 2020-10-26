package com.ziggy.rpc.server.netty;

import com.ziggy.rpc.common.netty.RpcDecoder;
import com.ziggy.rpc.common.netty.RpcEncoder;
import com.ziggy.rpc.common.protocol.RpcRequest;
import com.ziggy.rpc.common.protocol.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ziggy.rpc.common.serialize.JSONSerializer;

import javax.annotation.PreDestroy;

@Component
public class NettyServer implements InitializingBean {

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private ServerBootstrap serverBootstrap;

    private final NettyServerHandler nettyServerHandler;

    private final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Value("${rpc.server.port}")
    private int port;

    @Autowired
    public NettyServer(NettyServerHandler nettyServerHandler) {
        this.nettyServerHandler = nettyServerHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    /**
     * 服务端启动逻辑
     */
    private void start() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .channel(NioServerSocketChannel.class)
                .group(boss, worker)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline channelPipeline = ch.pipeline();

                        channelPipeline
                                .addLast(new RpcEncoder(new JSONSerializer(), RpcResponse.class));

                        channelPipeline
                                .addLast(new RpcDecoder(new JSONSerializer(), RpcRequest.class));

                        channelPipeline.addLast(nettyServerHandler);
                    }

                });

        bind(serverBootstrap,port);
    }

    private void bind(ServerBootstrap serverBootstrap, int port) {
        ChannelFuture channelFuture = serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("bind to port:{} successfully", port);
            } else {
                logger.error("bind to port:{} fail", port);
                bind(serverBootstrap, port + 1);
            }
        });
        channelFuture.awaitUninterruptibly();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        logger.info("server closed");
    }
}
