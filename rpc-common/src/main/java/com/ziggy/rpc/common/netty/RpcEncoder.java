package com.ziggy.rpc.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.ziggy.rpc.common.serialize.Serialize;

public class RpcEncoder extends MessageToByteEncoder {

    private Serialize serializer;

    private Class<?> clazz;

    public RpcEncoder(Serialize serializer, Class<?> clazz) {
        this.serializer = serializer;
        this.clazz = clazz;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf)
            throws Exception {
        if(serializer!=null && clazz.isInstance(o)){
            byte[] bytes = serializer.serialize(o);
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
