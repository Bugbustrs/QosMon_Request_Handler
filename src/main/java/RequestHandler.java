import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;


public class RequestHandler {
    final String GET="GET";
    final String POST="POST";
    final int SUCCESS=200;
    final int BAD_REQUEST=400;

    HttpServer server;
    public RequestHandler(){
        try{
            server=HttpServer.create(new InetSocketAddress(Config.SERVER_PORT),0);
            //needs to be put on its own method
            HttpContext context=server.createContext("/");
            context.setHandler(handleRequest());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void startServer(){
        server.start();
        System.out.println("server started");
    }

    public void handlePostRequest(HttpExchange httpExchange){
        String reqBody=getBodyString(httpExchange.getRequestBody());
        if(reqBody==null||reqBody.isEmpty()){
            //generate fail json body
            generateFailedResponse(httpExchange);
        }
        //db and orch things happen here
    }

    public String getBodyString(InputStream requestInStream){
        try {
            InputStreamReader isr = new InputStreamReader(requestInStream, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            int b;
            StringBuilder buf = new StringBuilder(512);
            while ((b = br.read()) != -1) {
                buf.append((char) b);
            }
            br.close();
            isr.close();
            return buf.toString();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void handleGetRequest(HttpExchange httpExchange){
        try {
            JSONObject response=new JSONObject();
            response.put("RequestType","Hire Me Fang");
            httpExchange.getResponseHeaders().set("Content-Type","application/json");
            httpExchange.sendResponseHeaders(SUCCESS, response.toString().getBytes().length);//response code and length
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private HttpHandler handleRequest() {
        HttpHandler handler= new HttpHandler() {
            public void handle(HttpExchange httpExchange) throws IOException {
                if(httpExchange.getRequestMethod().equals(GET)) {
                    handleGetRequest(httpExchange);
                }
                else if(httpExchange.getRequestMethod().equals(POST)){
                    handlePostRequest(httpExchange);
                }
            }
        };
        return handler;
    }

    public void generateFailedResponse(HttpExchange httpExchange){
        try {
            JSONObject failedResponse = new JSONObject();
            failedResponse.put("response", "failed");
            String failedResponseString = failedResponse.toString();
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(BAD_REQUEST, failedResponseString.getBytes().length);//response code and length
            OutputStream os = httpExchange.getResponseBody();
            os.write(failedResponseString.getBytes());
            os.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
