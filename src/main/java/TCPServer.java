import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements Runnable {
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(7000);
            System.out.println("Server Started");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread handler = new Thread(new TCPRequestHandler(clientSocket));
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
