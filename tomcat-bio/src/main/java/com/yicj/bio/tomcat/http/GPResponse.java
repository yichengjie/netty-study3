package com.yicj.bio.tomcat.http;

import java.io.OutputStream;

public class GPResponse {
    private OutputStream out ;

    public GPResponse(OutputStream out){
        this.out = out ;
    }

    public void write(String content) throws Exception{
        // 输出也要遵循http
        // 状态码为200
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\n") ;
        sb.append("Content-Type: text/html;\n") ;
        sb.append("\r\n");
        sb.append(content) ;
        out.write(sb.toString().getBytes());
    }

}
