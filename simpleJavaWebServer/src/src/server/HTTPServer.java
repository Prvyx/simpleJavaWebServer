package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class HTTPServer {
    private static Map<String,Servlet> servletCache=new HashMap<String,Servlet>();
    public static void main(String[] args){
        //设置端口
        int port;
        try{
            port=Integer.parseInt(args[0]);
        }catch (Exception e){
            System.out.println("未设置端口参数"+"port=8080(default)");
            port=8080;
        }

        //ServerSocket监听8080端口
        try {
            ServerSocket serverSocket=new ServerSocket(port);//注：可能有IOException异常，需要异常处理
            System.out.println("ServerSocket正在监听"+port+"端口");
            while(true){
                Socket socket=serverSocket.accept();//ServerSocket接受，提供socket（类似于一个管道）
                System.out.println("建立TCP连接："+socket.getInetAddress()+":"+socket.getPort());

                try{//service()里出现错误，会把异常向上抛
                    service(socket);
                }catch (Exception e){
                    System.out.println("请求失败");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public static  void service(Socket socket) throws Exception{//提供套接字管道服务
        //获得HTTP请求报文
        InputStream socketIn=socket.getInputStream();
        Thread.sleep(500);//
        int size=socketIn.available();
        byte[] requestBuffer=new byte[size];
        socketIn.read(requestBuffer);
        String request=new String(requestBuffer);
        System.out.println(request);//打印HTTP报文

        //解析HTTP请求报文
        String requestFirstLine=request.substring(0,request.indexOf("\r\n"));//获得request地第一行
        String[] parts=requestFirstLine.split(" ");
        String uri=null;//uri:请求的资源
        if(parts.length>=2)
            uri=parts[1];

        //服务器与用户动态交互 当用户需要访问servlet部分时，servlet自动生成html，而不是在root/中
        if(uri.indexOf("servlet")!=-1){
            String servletName=null;//servlet的具体名字
            //eg:uri(GET)="/servlet/HelloServlet?username=Tom&password=1234"或者(POST)"/servlet/HelloServlet"
            if(uri.indexOf("?")!=-1){
                servletName=uri.substring(uri.indexOf("servlet/")+8,uri.indexOf("?"));
                //find bug:subString(begin,end)=>包括begin，不包括end
            }
            else
                servletName=uri.substring(uri.indexOf("servlet/")+8,uri.length());
            Servlet servlet=servletCache.get(servletName);//从servletCache中得到servletName对应的Servlet对象
            if(servlet==null){//对象为null，创建它并保存在servletCache中
                servlet=(Servlet)Class.forName("server."+servletName).getDeclaredConstructor().newInstance();
                servlet.init();
                servletCache.put(servletName,servlet);
            }
            //进入特定的servlet.class，在里面生成自定义的html页面
            servlet.service(requestBuffer,socket.getOutputStream());
            Thread.sleep(1000);
            socket.close();
            return;
        }

        //对静态网页的处理
        String contentType=null;//正文类型
        if(uri.indexOf("htm")!=-1 || uri.indexOf("html")!=-1)
            contentType="text/html";
        else if(uri.indexOf("jpg")!=-1 || uri.indexOf("jpeg")!=-1)
            contentType="img/jpeg";
        else if(uri.indexOf("gif")!=-1)
            contentType="img/gif";
        else
            contentType="application/octet-stream";

        //制作HTTP响应报文
        String responseFirstLine="HTTP/1.1 200 OK\r\n";//报文行
        String responseHeader="Content-Type: "+contentType+"\r\n\r\n";//报文头
        InputStream responseText=HTTPServer.class.getResourceAsStream("root/"+uri);//报文正文
        //发送HTTP响应报文
        OutputStream socketOut=socket.getOutputStream();
        socketOut.write(responseFirstLine.getBytes());
        socketOut.write(responseHeader.getBytes());
        byte[] buffer=new byte[128];
        int len=0;
        while((len=responseText.read(buffer))!=-1){
            socketOut.write(buffer,0,len);
        }
        Thread.sleep(1000);
        socket.close();
    }
}
