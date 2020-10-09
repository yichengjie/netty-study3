package com.yicj.bio.tomcat;

import com.yicj.bio.tomcat.http.GPRequest;
import com.yicj.bio.tomcat.http.GPResponse;
import com.yicj.bio.tomcat.http.GPServlet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GPTomcat {
    private int port = 8080 ;
    private ServerSocket server ;
    private Map<String, GPServlet> servletMapping = new HashMap<>() ;
    private Properties webXml = new Properties() ;


    public static void main(String[] args) {

        new GPTomcat().start();
    }

    private void init(){
        // 加载web.xml文件，同时初始化ServerMapping对象
        try {
            String WEB_INFO = this.getClass().getResource("/").getPath();
            FileInputStream fis = new FileInputStream(WEB_INFO+"web.properties") ;
            webXml.load(fis) ;
            for (Object k: webXml.keySet()){
                String key = k.toString() ;
                if (key.endsWith(".url")){
                    String servletName = key.replaceAll("\\.url$", "");
                    String url = webXml.getProperty(key) ;
                    String className = webXml.getProperty(servletName +".className") ;
                    // 单实例，多线程
                    GPServlet obj = (GPServlet) Class.forName(className).newInstance() ;
                    servletMapping.put(url, obj) ;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void start(){
        // 加载配置文件，初始化servletMapping
        init();
        // netty封装了Nio的reactor模型，boss，worker
        //boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup() ;
        //worker线程
        EventLoopGroup workerGroup = new NioEventLoopGroup() ;
        try {
            //1. 创建对象
            ServerBootstrap server = new ServerBootstrap() ;
            //2. 配置参数
            //链式编程
            server.group(bossGroup, workerGroup)
                //主线程处理类，看到这样的写法，底层就是用反射
                .channel(NioServerSocketChannel.class)
                //子线程处理类，Handler
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    // 客户端的初始化处理
                    @Override
                    protected void initChannel(SocketChannel client) throws Exception {
                    // 无锁化串行编程
                    //Netty对http的封装，对顺序有要求
                    //HttpResponseEncoder编码器
                    //责任链模式，双向链表InBound OutBound
                    client.pipeline().addLast(new HttpResponseEncoder()) ;
                    // HttpRequestDecoder解码器
                    client.pipeline().addLast(new HttpRequestDecoder()) ;
                    //业务逻辑处理
                    client.pipeline().addLast(new GPTomcatHandler()) ;
                    }
                }) ;
            // 针对主线程的配置 分配线程最大数量128
            server.option(ChannelOption.SO_BACKLOG, 128) ;
            // 针对子线程的配置，保持长连接
            server.childOption(ChannelOption.SO_KEEPALIVE, true) ;
            //3. 启动服务器
            ChannelFuture f = server.bind(port).sync();
            System.out.println("GPTomcat已启动，监听端口是：" + port);
            f.channel().closeFuture().sync() ;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //关闭线程池
            bossGroup.shutdownGracefully() ;
            workerGroup.shutdownGracefully() ;
        }
    }


    public class GPTomcatHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest){
                System.out.println("hello");
                HttpRequest req = (HttpRequest) msg ;
                // 转交给我们自己的Request实现
                GPRequest request = new GPRequest(ctx,req) ;
                //转交给我们自己的Response
                GPResponse response = new GPResponse(ctx, req) ;
                // 实际业务处理
                String url = request.getUrl();
                if (servletMapping.containsKey(url)){
                    servletMapping.get(url).service(request, response);
                }else {
                    response.write("404 - Not Found");
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        }
    }

}
