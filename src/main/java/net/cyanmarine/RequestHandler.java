package net.cyanmarine;

import java.net.Socket;

public class RequestHandler implements Runnable {
    private final HttpRequest request;

    public RequestHandler(Socket socket) throws Exception {
        this.request = new HttpRequest(socket);
    }

    private void processRequest() throws Exception {
        Boolean page = Pages.getPage(request);
        if (Boolean.FALSE.equals(page)) {
            if (request.getPath().equals("/")) request.setPath("/index.html");
            request.prependPath("./public");
            request.processFileRequest();
        }

        request.close();
    }

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            Logger.error(e.toString());
        }
    }
}
