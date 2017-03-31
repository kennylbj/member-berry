package kenny.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;

/**
 * Created by kennylbj on 2017/3/25.
 */
public class MemberBerryServerInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslCtx;
    private final DataManipulator manipulator;

    public MemberBerryServerInitializer(SslContext sslCtx, DataManipulator manipulator) {
        this.sslCtx = sslCtx;
        this.manipulator = manipulator;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline p = ch.pipeline();

        // Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.
        p.addLast(sslCtx.newHandler(ch.alloc()));

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(kenny.proto.Message.Request.getDefaultInstance()));

        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());

        // and then business logic.
        p.addLast(new MemberBerryServerHandler(manipulator));
    }
}
