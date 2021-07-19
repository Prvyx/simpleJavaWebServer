package server;

import java.io.*;
import java.util.Locale;

public class UploadServlet implements Servlet{
    public void init()throws Exception{
        System.out.println("UploadServlet is inited");
    }

    public void service(byte[] requestBuffer, OutputStream out)throws Exception{
        String request=new String(requestBuffer);
        String requestHeader=request.substring(request.indexOf("\r\n")+2,request.indexOf("\r\n\r\n"));
        BufferedReader bufferedReader=new BufferedReader(new StringReader(requestHeader));
        String line=null;
        String boundary=null;
        while((line=bufferedReader.readLine())!=null){
            if(line.indexOf("Content-Type")!=-1){
                boundary=line.substring(line.indexOf("boundary=")+9,line.length());
                break;
            }
        }
        if(boundary==null){
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write("Content-Type: text/html\r\n\r\n".getBytes());
            out.write("Uploading is failed".getBytes());
            return;
        }

        int index1OfBoundary=request.indexOf(boundary);
        int index2OfBoundary=request.indexOf(boundary,index1OfBoundary+boundary.length());
        int index3OfBoundary=request.indexOf(boundary,index2OfBoundary+boundary.length());
        int contentHead=index2OfBoundary+boundary.length()+2,contentTail=index3OfBoundary-2;
        String content=request.substring(contentHead,contentTail);
        int fileHead=request.indexOf("\r\n\r\n",index2OfBoundary)+4,
                fileTail=contentTail;
        String fileContent=request.substring(fileHead,fileTail);
        //从content中查找文件名字
        String fileName=null;
        bufferedReader=new BufferedReader(new StringReader(content));
        while((line=bufferedReader.readLine())!=null){
            if(line.indexOf("filename")!=-1){
                fileName=line.substring(line.indexOf("filename=")+10,line.length()-1);
                //line.length()-1的目的：去掉"号
                break;
            }
        }
        //创建文件，并将fileContent的内容输出至新创建的文件中
        File file=new File("src/src/server/root/"+fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        FileOutputStream fileOutputStream=new FileOutputStream(file);
        int fileLen=fileTail-fileHead;
        fileOutputStream.write(requestBuffer,fileHead,fileLen);
        fileOutputStream.close();
        //注：根据测试，发现如果.txt文件里面出现”汉字“就会出现问题，现在未解决

        //创建HTTP响应报文
        out.write("HTTP 200 OK\r\n".getBytes());
        out.write("Content-Type: text/html\r\n\r\n".getBytes());
        String responseContent=
                "<html><head><title>HelloWorld</title></head><body>";
        responseContent+="<h1>Uploading is finished.<br></h1>";
        responseContent+="<h1>FileName:"+fileName+"<br></h1>";
        responseContent+="<h1>FileSize:"+fileLen+"<br></h1></body><head>";
        out.write(responseContent.getBytes());

    }
}
