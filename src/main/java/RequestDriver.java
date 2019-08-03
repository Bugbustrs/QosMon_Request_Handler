import database.DatabaseManager;

public class RequestDriver {

    public static void main(String[] args) {
        RequestHandler rh = new RequestHandler();
        if (DatabaseManager.init()) {
            Measurement.init();
            JobTracker.startJobTracker();
            Thread t = new Thread(new TCPServer());
            t.start();
            System.out.println("All systems go!!!!!");
        }
        rh.startServer();
    }
}
