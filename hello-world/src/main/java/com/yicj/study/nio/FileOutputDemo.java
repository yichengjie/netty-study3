package com.yicj.study.nio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileOutputDemo {
    private static final byte message [] = {83,111,109,101,32,98,121,116,101,115,46} ;

    public static void main(String[] args) throws IOException {
        FileOutputStream fout = new FileOutputStream("D:\\opt\\app\\timer\\config\\hello.txt") ;
        FileChannel fc = fout.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        for (int i =0 ; i < message.length ; i ++){
            buffer.put(message[i]) ;
        }

        buffer.flip() ;
        fc.write(buffer) ;
        fout.close();
    }

}
