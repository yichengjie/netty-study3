package com.yicj.bio.tomcat.servlet;

import com.yicj.bio.tomcat.http.GPRequest;
import com.yicj.bio.tomcat.http.GPResponse;
import com.yicj.bio.tomcat.http.GPServlet;

public class FirstServlet extends GPServlet {

    protected void doPost(GPRequest request, GPResponse response) throws Exception {
        this.doPost(request, response);
    }

    protected void doGet(GPRequest request, GPResponse response) throws Exception {
        response.write("This is First Servlet") ;
    }
}
