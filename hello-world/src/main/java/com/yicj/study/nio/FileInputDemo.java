package com.yicj.study.nio;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileInputDemo {

    public static void main(String[] args) throws IOException {
        FileInputStream fin = new FileInputStream("D:\\opt\\app\\timer\\config\\conf.properties") ;
        // 从FileInputStream获取channel
        FileChannel fc = fin.getChannel();
        // 创建buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 将数据从channel写入到buffer
        fc.read(buffer) ;

        buffer.flip() ;

        while (buffer.remaining() >0){
            byte b = buffer.get();
            System.out.println((char)b);
        }
        fin.close();
    }
}
