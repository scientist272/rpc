package netty;

import common.RpcRequest;
import common.RpcResponse;
import exception.NoIdleChannelException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serialize.JSONSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private EventLoopGroup eventLoopGroup;

    private Channel channel;

    private ClientHandler clientHandler;

    private Bootstrap bootstrap;

    private final List<Channel> channels;

    private final String host;

    private final Integer port;

    private final AtomicInteger channelIndex;

    private static final int MAX_RETRY = 5;

    private static final Object lock = new Object();


    //记录Host以及Port与client之间的映射关系
    private static Map<String, NettyClient> clientMap = new ConcurrentHashMap<>();


    private NettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
        channels = new ArrayList<>();
        channelIndex = new AtomicInteger();
    }


    /**
     * 返回可用客户端，如果未初始化则初始化，双重检测锁保证安全
     *
     * @param host
     * @param port
     * @return
     */
    public static NettyClient getClientInstance(String host, int port) {

        if (clientMap.get(genClientKey(host, port)) == null) {

            synchronized (lock) {
                if (clientMap.get(genClientKey(host, port)) == null) {
                    return init(host, port);

                } else {
                    return clientMap.get(genClientKey(host, port));
                }
            }

        } else {
            return clientMap.get(genClientKey(host, port));
        }
    }


    /**
     * 客户端初始化逻辑，需要双重检测锁保证竞态条件下的线程安全
     *
     * @param host
     * @param port
     * @return
     */
    private static NettyClient init(String host, int port) {
        NettyClient instance = new NettyClient(host, port);

        ClientHandler handler = new ClientHandler();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcEncoder(new JSONSerializer(), RpcRequest.class));
                        pipeline.addLast(new RpcDecoder(new JSONSerializer(), RpcResponse.class));
                        pipeline.addLast(handler);
                    }
                });

        //每个客户端三个连接
        for (int i = 0; i < 3; i++) {
            connect(instance, bootstrap, host, port, MAX_RETRY);
        }

        instance.clientHandler = handler;
        instance.eventLoopGroup = eventLoopGroup;
        instance.bootstrap = bootstrap;

        //将客户端加入到映射中保存
        clientMap.putIfAbsent(genClientKey(host, port), instance);

        logger.info("new client has been created, host info:{}", genClientKey(host, port));
        return instance;
    }

    /**
     * 客户端连接服务端逻辑，失败重连5次
     *
     * @param instance
     * @param bootstrap
     * @param host
     * @param port
     * @param retry
     */
    private static void connect(NettyClient instance, Bootstrap bootstrap, String host, int port, int retry) {
        bootstrap
                .connect(host, port)
                .addListener((ChannelFuture future) -> {
                    if (future.isSuccess()) {
                        logger.info("connect to {} successfully", genClientKey(host, port));
                        //将连接保存到client中
                        instance.channels.add(future.channel());
                    } else if (retry == 0) {
                        logger.error("can't connect to {}", genClientKey(host, port));
                    } else {
                        //本次重连的次数
                        int order = MAX_RETRY - retry + 1;
                        //本次重连间隔
                        int delay = 1 << order;
                        logger.error("retry to connect to host:{}, retry order:{}",
                                genClientKey(host, port), order);

                        bootstrap.config()
                                .group()
                                .schedule(() -> connect(instance, bootstrap, host, port, retry - 1),
                                        delay, TimeUnit.SECONDS);
                    }
                });
    }

    private static String genClientKey(String host, int port) {
        return host + "::" + port;
    }


    /**
     * 获取可用的channel，连接复用
     * @return
     * @throws NoIdleChannelException
     */
    public Channel nextChannel() throws NoIdleChannelException {
        return getFirstActiveChannel(0);
    }

    private Channel getFirstActiveChannel(int count) throws NoIdleChannelException {
        Channel channel = channels.get(Math.abs(channelIndex.getAndIncrement() % channels.size()));

        if (channel == null) {
            //TODO 增加自旋等待操作?
            throw new NoIdleChannelException();
        }

        if (!channel.isActive()) {
            //重连
            reconect(channel);
            if (count > channels.size()) {
                throw new NoIdleChannelException();
            }

            return getFirstActiveChannel(count + 1);
        }
        return channel;
    }

    /**
     * 重连
     */
    private void reconect(Channel channel) {
        //此处可改为原子操作
        synchronized (channel) {
            if (channels.indexOf(channel) == -1) {
                return;
            }

            Channel newChannel = bootstrap.connect(host, port).channel();
            channels.set(channels.indexOf(channel), newChannel);

            System.out.println(channels.indexOf(channel) + "位置的channel成功进行重连!");
        }
    }

}
