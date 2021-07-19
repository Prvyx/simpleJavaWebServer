package server;

import java.io.OutputStream;

public class HelloServlet implements Servlet{
    public void init()throws Exception{
        System.out.println("HelloServlet is inited");
    }

    public void service(byte[] requestBuffer, OutputStream out)throws Exception{
        String request=new String(requestBuffer);
        String requestFirstLine=request.substring(0,request.indexOf("\r\n"));
        String[] parts=requestFirstLine.split(" ");
        String method=parts[0];
        String uri=parts[1];
        String username=null;
        if(method.equalsIgnoreCase("GET") && uri.indexOf("username")!=-1){
            //method=="GET"的处理方法
            String parameters=uri.substring(uri.indexOf("?")+1,uri.length());
            //part[0]="username=Tom"
            String[] part=parameters.split("&");
            String[] par=part[0].split("=");
            username=par[1];
        }
        else{
            //method=="POST"的处理方法:username在HTTP报文的正文中
            String content=request.substring(request.indexOf("\r\n\r\n")+4,request.length());
            if(content.indexOf("username")!=-1){
                String[] part=content.split("&");
                String[] par=part[0].split("=");
                username=par[1];
            }
        }

        //生成HTTP响应报文
        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Content-Type: text/html\r\n\r\n".getBytes());
        String content="<html><head><title>HelloWorld"
                +"</title></head><body>";
        content+="<h1>Hello:"+username+"</h1></body><head>";
        out.write(content.getBytes());
    }
}
