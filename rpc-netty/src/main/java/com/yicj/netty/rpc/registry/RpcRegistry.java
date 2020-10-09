package com.yicj.netty.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 服务注册
 */
public class RpcRegistry {
    private int port ;

    public static void main(String[] args) {
        new RpcRegistry(8080).start();
    }

    public RpcRegistry(int port){
        this.port = port ;
    }

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup() ;
        EventLoopGroup workGroup = new NioEventLoopGroup() ;
        try {
            ServerBootstrap b = new ServerBootstrap() ;
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 自定义协议解码器
                            /**
                             * 入参有5个，分别解释如下
                             *int maxFrameLength: 框架的最大长度。如果帧的长度大于此值，这将抛出TooLongFrameException
                             *int lengthFieldOffset: 长度属性的偏移量。即对应的长度属性在整个消息数据中的位置
                             * int lengthFieldLength: 长度字段的长度。如果长度属性是int型，那么这个值是4(long型就是8)
                             * int lengthAdjustment: 要添加长度属性值的补偿值
                             * int initialBytesToStrip: 从解码帧中取出的第一个字节数
                             */
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                            0, 4,0, 4)) ;
                            // 自定义协议编码器
                            pipeline.addLast(new LengthFieldPrepender(4)) ;
                            // 对象参数类型编码器
                            pipeline.addLast("encoder", new ObjectEncoder()) ;
                            // 对象参数类型解码器
                            pipeline.addLast("decoder",
                                    new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null))) ;
                            pipeline.addLast(new RegistryHandler()) ;
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true) ;

            ChannelFuture future = b.bind(port).sync();
            System.out.println("GP RPC Registry start listen at : " + port);
            future.channel().closeFuture().sync() ;
        }catch (Exception e){
            bossGroup.shutdownGracefully() ;
            workGroup.shutdownGracefully() ;
        }
    }
}
