package com.yicj.bio.tomcat;

import com.yicj.bio.tomcat.http.GPRequest;
import com.yicj.bio.tomcat.http.GPResponse;
import com.yicj.bio.tomcat.http.GPServlet;

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
        try {
            server = new ServerSocket(this.port) ;
            System.out.println("GPTomcat 已启动，监听端口是："+ this.port);
            //2. 等待用户请求，用一个死循环来等待用户请求
            while (true){
                Socket client = server.accept();
                //3. http请求，发送的数据就是字符串---有规律的字符串(http)
                process(client) ;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void process(Socket client) throws Exception{
        InputStream is = client.getInputStream() ;
        OutputStream os = client.getOutputStream();

        //4. Request(InputStream) / Response(OutputStream)
        GPRequest request = new GPRequest(is) ;
        GPResponse response = new GPResponse(os) ;
        //5. 从协议内容中获得url，把响应的Servlet用反射进行实例化
        String url = request.getUrl();
        if (servletMapping.containsKey(url)){
            //6. 调用实例化对象的service()方法，执行具体的业务逻辑，doGet()/doPost()方法
            servletMapping.get(url).service(request,response);
        }else {
            response.write("404 - Not Found");
        }
        os.flush();
        os.close();

        is.close();
        client.close();
    }

}
