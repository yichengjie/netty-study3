package com.yicj.bio.tomcat.http;

public abstract class GPServlet {

    public void service(GPRequest request, GPResponse response) throws Exception{
        // 由service()方法决定是调用doGet()还是调用doPost()
        if ("GET".equalsIgnoreCase(request.getMethod())){
            doGet(request, response) ;
        }else {
            doPost(request, response) ;
        }
    }

    protected abstract void doPost(GPRequest request, GPResponse response) throws Exception;

    protected abstract void doGet(GPRequest request, GPResponse response) throws Exception;

}
