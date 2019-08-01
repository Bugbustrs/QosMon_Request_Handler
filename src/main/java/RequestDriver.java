import database.DatabaseManager;

public class RequestDriver {
    private static OrchAPI orcService;

    public static OrchAPI getOrcService(){
        return orcService;
    }

    public static void main(String[] args) {

        RequestHandler rh = new RequestHandler();
        if (DatabaseManager.init()) {
            Measurement.init();
            Thread t = new Thread(new TCPServer());
            t.start();
            orcService = new OrchAPI();
            System.out.println("All systems go!!!!!");
        }
        rh.startServer();
    }
}
