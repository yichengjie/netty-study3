package com.yicj.netty.rpc.provider;

import com.yicj.netty.rpc.api.IRpcHelloService;

public class RpcHelloServiceImpl implements IRpcHelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name + "!";
    }
}
