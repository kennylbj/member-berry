package kenny.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import kenny.client.views.ConsoleObserverImpl;

/**
 * Created by kennylbj on 2017/3/23.
 *
 */
public class MemberBerryClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8666"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        //FIXME Unsafe, do not use in production
        final SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            MemberBerryClientHandler handler = new MemberBerryClientHandler();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new MemberBerryClientInitializer(sslCtx, handler));

            // Make a new connection.
            b.connect(HOST, PORT).sync();

            ConsoleObserverImpl observer = new ConsoleObserverImpl(handler);
            handler.registerObserver(observer);
            observer.run();

        } finally {
            group.shutdownGracefully();
        }
    }



}
