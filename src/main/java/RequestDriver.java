import database.DatabaseManager;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RequestDriver {
    private static JSONObject CONFIGS = null;

    public static boolean setup() {
        String text = null;
        try {
            String CONFIG_FILE = ".config.json";
            text = new String(Files.readAllBytes(Paths.get(CONFIG_FILE)), StandardCharsets.UTF_8);
            CONFIGS = new JSONObject(text);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        RequestHandler rh = new RequestHandler();
        setup();
        PCAPAnalyzerDriver analyzerDriver = new PCAPAnalyzerDriver(CONFIGS.getJSONObject("file_server_config"));
        if (DatabaseManager.init(CONFIGS.getJSONObject("db_configs")) && analyzerDriver.initiate()) {
            Measurement.init();
            JobTracker.startJobTracker();
            Thread t = new Thread(new TCPServer());
            t.start();
            System.out.println("All systems go!!!!!");
        }
        rh.startServer();
    }
}
