import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import database.DatabaseManager;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Date;
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
            initContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initContext(){
        HttpContext scheduleContext = server.createContext("/");
        scheduleContext.setHandler(handleScheduleRequest());

        HttpContext jobResultsContext = server.createContext("/results");
        jobResultsContext.setHandler(handleJobResultRequest());

        HttpContext jobDescContext = server.createContext("/results/jobs");
        jobDescContext.setHandler(handleJobDescRequest());
        HttpContext appUsageContext = server.createContext("/app-usage");
        appUsageContext.setHandler(handleAppUsageRequest());
    }


    public void startServer() {
        server.start();
        System.out.println("HTTP Server Started");
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

    public Map<String, String> getQueryParams(HttpExchange httpExchange){
        String queryParams=httpExchange.getRequestURI().getQuery();
        Map<String, String> params = queryToMap(queryParams);
        return params;
    }
    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if(query==null||query.isEmpty()){
            return result;
        }
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

    private HttpHandler handleScheduleRequest() {
        HttpHandler handler = new HttpHandler() {
            public void handle(HttpExchange httpExchange) throws IOException {
                System.out.println("HTTP REQUEST Method "+httpExchange.getRequestMethod());
                if (httpExchange.getRequestMethod().equals(GET)) {
                    handleScheduleGetRequest(httpExchange);
                } else if (httpExchange.getRequestMethod().equals(POST)) {
                    handleSchedulePostRequest(httpExchange);
                }
                else if (httpExchange.getRequestMethod().equals(OPTIONS)){
                    handleOPTIONSRequest(httpExchange);
                }
            }
        };
        return handler;
    }

    private HttpHandler handleJobResultRequest() {
        HttpHandler handler = new HttpHandler() {
            public void handle(HttpExchange httpExchange) throws IOException {
                System.out.println("HTTP REQUEST Method "+httpExchange.getRequestMethod());
                if (httpExchange.getRequestMethod().equals(GET)) {
                    handleJobResultGetRequest(httpExchange);
                } else if (httpExchange.getRequestMethod().equals(POST)) {
                    handleJobResultPostRequest(httpExchange);
                }
                else if (httpExchange.getRequestMethod().equals(OPTIONS)){
                    handleOPTIONSRequest(httpExchange);
                }
            }
        };
        return handler;
    }
    private HttpHandler handleAppUsageRequest() {
        HttpHandler handler = new HttpHandler() {
            public void handle(HttpExchange httpExchange) throws IOException {
                System.out.println("HTTP REQUEST Method "+httpExchange.getRequestMethod());
                if (httpExchange.getRequestMethod().equals(GET)) {
                    handleUsageGetRequest(httpExchange);
                } else if (httpExchange.getRequestMethod().equals(POST)) {
                    handleUsagePostRequest(httpExchange);
                }
                else if (httpExchange.getRequestMethod().equals(OPTIONS)){
                    handleOPTIONSRequest(httpExchange);
                }
            }
        };
        return handler;
    }
  
    private HttpHandler handleJobDescRequest() {
        HttpHandler handler = new HttpHandler() {
            public void handle(HttpExchange httpExchange) throws IOException {
                System.out.println("HTTP REQUEST Method "+httpExchange.getRequestMethod());
                if (httpExchange.getRequestMethod().equals(GET)) {
                    handleJobDescGetRequest(httpExchange);
                } else if (httpExchange.getRequestMethod().equals(POST)) {
                    handleJobDescPostRequest(httpExchange);
                }
                else if (httpExchange.getRequestMethod().equals(OPTIONS)){
                    handleOPTIONSRequest(httpExchange);
                }
            }
        };
        return handler;
    }

    public void handleScheduleGetRequest(HttpExchange httpExchange) {
        generateFailedResponse(httpExchange);
    }

    public void handleSchedulePostRequest(HttpExchange httpExchange) {
        System.out.println("Handling POST Request");
        String reqBody = getBodyString(httpExchange.getRequestBody());
        System.out.print("Post Request\n" + reqBody);
        if (reqBody == null || reqBody.isEmpty()) {
            //generate fail json body
            generateFailedResponse(httpExchange);
            return;
        }
        //db and orch things happen here
        JSONObject request = new JSONObject(reqBody);
        String type = request.getString("request_type");
        System.out.println("Type : "+type);
        switch (type) {
            case "SCHEDULE_MEASUREMENT":
                System.out.println("Job Desc received: " + request.toString());
                DatabaseManager.insertMeasurementDetails(reqBody);
                Measurement.addMeasurement(request);
                generateSuccessResponse(httpExchange);
                break;
            default:
                return;
        }
    }

    private void handleJobDescGetRequest(HttpExchange httpExchange) {
        Map<String,String> queryParams=getQueryParams(httpExchange);
        if(queryParams.size()==0){
            generateFailedResponse(httpExchange);
            return;
        }
        String type = queryParams.get("type");
        //TODO Put DB code related to job Desc
        String results = DatabaseManager.getMeasurementOfTypes(type);
        sendPayload(httpExchange,results);
    }

    private void handleJobDescPostRequest(HttpExchange httpExchange) {
         generateFailedResponse(httpExchange);
    }

    private void handleJobResultGetRequest(HttpExchange httpExchange) {
        Map<String,String> queryParams=getQueryParams(httpExchange);
        if(queryParams.size()==0){
            generateFailedResponse(httpExchange);
            return;
        }
        String type = queryParams.get("type");
        String jobID = queryParams.get("id");
        //TODO Put DB code related to job Results provided the ID and the type
        String results = DatabaseManager.getMeasurement(type, jobID);
        sendPayload(httpExchange,results);
    }


    private void handleUsageGetRequest(HttpExchange httpExchange){
        Map<String,String> queryParams=getQueryParams(httpExchange);
        if(queryParams.size()==0){
            generateFailedResponse(httpExchange);
            return;
        }
        String email = queryParams.get("email");
        System.out.println("email received is "+ email);
        String results = DatabaseManager.readPersonalData(email,0,new Date().getTime());
        sendPayload(httpExchange,results);
    }

    private void handleUsagePostRequest(HttpExchange httpExchange){
        generateFailedResponse(httpExchange);
    }
  
    private void sendPayload(HttpExchange httpExchange, String results) {
        try {
            JSONObject successResponse = new JSONObject();
            successResponse.put("response", "success");
            successResponse.put("payload",results);
            String successResponseString = successResponse.toString();
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin","*");
            httpExchange.sendResponseHeaders(SUCCESS, successResponseString.getBytes().length);//response code and length
            OutputStream os = httpExchange.getResponseBody();
            os.write(successResponseString.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleJobResultPostRequest(HttpExchange httpExchange) {
        generateFailedResponse(httpExchange);
    }

    private void handleOPTIONSRequest(HttpExchange httpExchange){
        try {
            System.out.println("Handling OPTIONS Request");
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
            httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin","*");
            httpExchange.sendResponseHeaders(SUCCESS, 0);
            OutputStream os = httpExchange.getResponseBody();
            os.write("".getBytes());
            os.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    public void generateSuccessResponse(HttpExchange httpExchange) {
        try {
            JSONObject successResponse = new JSONObject();
            successResponse.put("response", "success");
            String failedResponseString = successResponse.toString();
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin","*");
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
