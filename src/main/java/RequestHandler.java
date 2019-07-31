import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class RequestHandler {
    HttpServer server;

    public RequestHandler(){
        try{
            server=HttpServer.create(new InetSocketAddress(Config.SERVER_PORT),0);
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
    private HttpHandler handleRequest() {
        HttpHandler handler= new HttpHandler() {
            public void handle(HttpExchange httpExchange) throws IOException {
                JSONObject response=new JSONObject();
                response.put("RequestType","Hire Me Fang");
                try {
                    httpExchange.getResponseHeaders().set("Content-Type","application/json");
                    httpExchange.sendResponseHeaders(200, response.toString().getBytes().length);//response code and length
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.toString().getBytes());
                    os.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        return handler;
    }



}
