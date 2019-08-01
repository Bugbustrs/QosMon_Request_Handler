import database.DatabaseManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class TCPRequestHandler implements Runnable {


    private final String MEASUREMENT_REQUEST_TYPE = "SCHEDULE_MEASUREMENT";
    private final String MEASUREMENT_CHECK_IN_TYPE = "CHECKIN";

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
                        String requestType = (String) request.get("request_type");
                        if (requestType.equalsIgnoreCase(MEASUREMENT_CHECK_IN_TYPE)) {
                            System.out.println("server receives checkin");
                            //send the client a list of available jobs;
                            sendActiveTasks();
                        } else if (requestType.equals(MEASUREMENT_REQUEST_TYPE)) {
                            //the request contains Measurement Description so should be added to the list of jobs
                            addMeasurement(request);
                            System.out.println(request);
                        } else {
                            //send error json to repeat since the type is not supported
                        }
                    }else{
                        DatabaseManager.writeValues(request);
                    }
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

    public void addMeasurement(JSONObject measurementDesc) {
        Measurement.addMeasurement(measurementDesc);
    }

    public void sendActiveTasks() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
            JSONArray activeTasks = Measurement.getjobs();
            out.println(activeTasks.toString());
            out.flush();
            System.out.println(activeTasks.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
