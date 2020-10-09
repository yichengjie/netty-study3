package com.yicj.bio.tomcat.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GPResponse {
    // SocketChannel
    private ChannelHandlerContext ctx ;
    private HttpRequest req ;

    public GPResponse(ChannelHandlerContext ctx, HttpRequest req){
        this.ctx = ctx ;
        this.req = req ;
    }


    public void write(String content) throws Exception{
        try {
            if (content == null || content.length() ==0){
                return;
            }
            // 输出也要遵循http
            // 状态码为200
            FullHttpResponse response = new DefaultFullHttpResponse(
                    //设置http版本HTTP 1.1
                    HttpVersion.HTTP_1_1,
                    //设置响应状态码
                    HttpResponseStatus.OK,
                    //将输出内容编码格式设置为UTF-8
                    Unpooled.wrappedBuffer(content.getBytes(StandardCharsets.UTF_8))
            );
            response.headers().set("Content-Type","text/html") ;
            ctx.write(response) ;
        }finally {
            ctx.flush();
            ctx.close();
        }
    }
}
