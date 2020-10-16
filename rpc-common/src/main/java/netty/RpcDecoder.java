package netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import serialize.Serialize;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {
    private Serialize serializer;

    private Class<?> clazz;

    public RpcDecoder(Serialize serializer, Class<?> clazz) {
        this.serializer = serializer;
        this.clazz = clazz;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();

        int dataLength = in.readInt();

        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];

        in.readBytes(data);

        Object result = serializer.deserialize(clazz,data);

        out.add(result);
    }
}
