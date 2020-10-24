package netty;

import common.RpcRequest;
import common.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import serialize.JSONSerializer;

import javax.annotation.PreDestroy;


public class NettyServer implements InitializingBean {

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private ServerBootstrap serverBootstrap;

    @Autowired
    private NettyServerHandler nettyServerHandler;

    private final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Value("${rpc.server.port}")
    private int port;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * 服务端启动逻辑
     */
    private void start() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();
        serverBootstrap
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
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("bind to port:{} successfully", port);
            } else {
                logger.error("bind to port:{} fail", port);
                bind(serverBootstrap, port + 1);
            }
        });
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        logger.info("server closed");
    }
}
