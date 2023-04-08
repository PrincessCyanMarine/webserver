package net.cyanmarine;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static final String CRLF = "\r\n";
    public static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        Logger.init("WebServer");
        Logger.info("Initializing web server...");
        int port = 80;
        ServerSocket server = new ServerSocket(port);
        Logger.info("Web server initialized on port " + port);

        Logger.info("Initializing pages...");
        Pages.init();
        Logger.info("Pages initialized");

        Logger.info("Listening for connections...");
        server.setSoTimeout(1000);
        Logger.info("Timeout: " + server.getSoTimeout() + "ms");
        while (running.get()) {
            try { handleConnection(server.accept()); } catch (Exception ignored) { }
        }


        Logger.info("Closing web server...");
        server.close();
        Logger.info("Closed web server");
    }

    private static void handleConnection(Socket socket) throws Exception {
        Logger.write("\n\n");
        Logger.info("New Request");
        Logger.info("Handling connection from " + socket.getInetAddress().getHostAddress());
        RequestHandler request = new RequestHandler(socket);
        Thread thread = new Thread(request);
        thread.start();
    }
}