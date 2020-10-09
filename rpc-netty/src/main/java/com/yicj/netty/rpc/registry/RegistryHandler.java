package com.yicj.netty.rpc.registry;

import com.yicj.netty.rpc.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RegistryHandler extends ChannelInboundHandlerAdapter {

    // 保存所有可用的服务
    public static ConcurrentMap<String,Object> registryMap = new ConcurrentHashMap<>() ;
    //保存所有相关的服务类
    private List<String> classNames = new ArrayList<>() ;

    public RegistryHandler(){
        // 完成递归扫描
        scannerClass("com.yicj.netty.rpc.provider") ;
        doRegister() ;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object() ;
        InvokerProtocol request = (InvokerProtocol) msg ;
        // 当客户端建立连接时，需要从自定义协议中获取信息，以及具体的服务和实参
        // 使用反射调用
        if (registryMap.containsKey(request.getClassName())){
            Object instance = registryMap.get(request.getClassName());
            Method method = instance.getClass().getMethod(request.getMethodName(), request.getParams());
            result = method.invoke(instance, request.getValues());
        }
        ctx.write(result) ;
        ctx.flush() ;
        ctx.close() ;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close() ;
    }

    // 递归扫描
    private void scannerClass(String packageName) {
        String path = packageName.replaceAll("\\.", "/");
        URL url = this.getClass().getClassLoader().getResource(path);
        File dir = new File(url.getFile()) ;
        for (File file: dir.listFiles()){
            //  如果是一个文件夹，继续递归
            if (file.isDirectory()){
                scannerClass(packageName +"." + file.getName());
            }else {
                String className = packageName + "." + file.getName().replace(".class","") ;
                classNames.add(className) ;
            }
        }
    }

    // 完成注册
    private void doRegister() {
        if (classNames.size() ==0){
            return;
        }
        for (String className: classNames){
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.putIfAbsent(i.getName(), clazz.newInstance()) ;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
