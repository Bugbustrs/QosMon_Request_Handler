import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.DatabaseManager;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;


public class RequestHandler {
    private final String GET = "GET";
    private final String POST = "POST";
    private final String OPTIONS="OPTIONS";
    private final int SUCCESS = 200;
    private final int BAD_REQUEST = 400;
    private final int NUM_OF_QUEUED_CONNECTIONS=10;

    private HttpServer server;
    public RequestHandler() {
        try {
            server = HttpServer.create(new InetSocketAddress(Config.SERVER_PORT), NUM_OF_QUEUED_CONNECTIONS);
            //needs to be put on its own method
            HttpContext context = server.createContext("/");
            context.setHandler(handleRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        server.start();
        System.out.println("server started");
    }

    public void handlePostRequest(HttpExchange httpExchange) {
        String reqBody = getBodyString(httpExchange.getRequestBody());
        System.out.print("Post Request" + reqBody);
        if (reqBody == null || reqBody.isEmpty()) {
            //generate fail json body
            generateFailedResponse(httpExchange);
            return;
        }
        //db and orch things happen here
        JSONObject request = new JSONObject(reqBody);
        String type = request.getString("request_type");
        switch (type) {
            case "SCHEDULE_MEASUREMENT":
                System.out.println("Request received: " + request.toString());
                System.out.println("Scheduling being done now");
                //DatabaseManager.insertMeasurementDetails(reqBody);
                Measurement.addMeasurement(request);
                generateSuccessResponse(httpExchange);
                break;
            default:
                return;
        }
    }

    public String getBodyString(InputStream requestInStream) {
        try {
            InputStreamReader isr = new InputStreamReader(requestInStream, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            int b;
            int bufferCapacity=512;
            StringBuilder buf = new StringBuilder(bufferCapacity);
            while ((b = br.read()) != -1) {
                buf.append((char) b);
            }
            br.close();
            isr.close();
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void handleGetRequest(HttpExchange httpExchange) {
        try {
            Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
            String type = params.get("type");
            String response = DatabaseManager.getMeasurement(type);
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(SUCCESS, response.toString().getBytes().length);//response code and length
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> queryToMap(String query) {
        System.out.println(query);
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private void handleOPTIONSRequest(HttpExchange httpExchange){
        try {
            httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            //Vary: Accept-Encoding, Origin
            //Keep-Alive: timeout=2, max=100
            //Connection: Keep-Alive
            httpExchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            httpExchange.getResponseHeaders().set("Access-Control-Allow-Headers", "X-PINGOTHER, Content-Type");
            httpExchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
            httpExchange.getResponseHeaders().set("Vary", "Accept-Encoding, Origin");
            httpExchange.getResponseHeaders().set("Keep-Alive", "timeout=2, max=100");
            httpExchange.getResponseHeaders().set("Connection", "Keep-Alive");
            httpExchange.sendResponseHeaders(SUCCESS, 0);
            OutputStream os = httpExchange.getResponseBody();
            os.write("".getBytes());
            os.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    private HttpHandler handleRequest() {
        HttpHandler handler = new HttpHandler() {
            public void handle(HttpExchange httpExchange) throws IOException {
                System.out.println("http method: " + httpExchange.getRequestMethod());
                if (httpExchange.getRequestMethod().equals(GET)) {
                    handleGetRequest(httpExchange);
                } else if (httpExchange.getRequestMethod().equals(POST)) {
                    handlePostRequest(httpExchange);
                }
                else if (httpExchange.getRequestMethod().equals(OPTIONS)){
                    System.out.println(getBodyString(httpExchange.getRequestBody()));
                    handleOPTIONSRequest(httpExchange);
                }
            }
        };
        return handler;
    }

    public void generateSuccessResponse(HttpExchange httpExchange) {
        try {
            JSONObject successResponse = new JSONObject();
            successResponse.put("response", "success");
            String failedResponseString = successResponse.toString();
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(SUCCESS, failedResponseString.getBytes().length);//response code and length
            OutputStream os = httpExchange.getResponseBody();
            os.write(failedResponseString.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateFailedResponse(HttpExchange httpExchange) {
        try {
            JSONObject failedResponse = new JSONObject();
            failedResponse.put("response", "failed");
            String failedResponseString = failedResponse.toString();
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.sendResponseHeaders(BAD_REQUEST, failedResponseString.getBytes().length);//response code and length
            OutputStream os = httpExchange.getResponseBody();
            os.write(failedResponseString.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
