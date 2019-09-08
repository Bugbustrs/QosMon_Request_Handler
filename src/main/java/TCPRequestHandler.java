import database.DatabaseManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class TCPRequestHandler implements Runnable {
    private Socket clientSocket;

    public TCPRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            Scanner in = new Scanner(clientSocket.getInputStream());
            String jsonString;
            while (true) {
                if (in.hasNextLine()) {
                    jsonString = in.nextLine();
                    JSONObject request = encodeJSON(jsonString);
                    if (request.has("request_type")) {
                        String type =request.getString("request_type");
                        if(type.equalsIgnoreCase("checkin")) {
                            JSONArray jobArray = (JSONArray) OrchAPI.returnResponse(request);
                            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                            out.println(jobArray.toString());
                            out.flush();
                            System.out.println("Active Jobs Sent To Phone");
                        }else if(type.equalsIgnoreCase("summary")){
                            DatabaseManager.writePersonalData(jsonString);
                        }
                    } else{
			 if(request.getBoolean("is_experiment")){ 
                          	 Measurement.recordSuccessfulJob(request);
			 }
                        DatabaseManager.writeValues(request);
                    }
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public JSONObject encodeJSON(String jsonString) {
        JSONObject request = new JSONObject(jsonString);
        return request;
    }
}
