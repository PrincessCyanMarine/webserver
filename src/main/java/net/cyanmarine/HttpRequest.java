package net.cyanmarine;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static net.cyanmarine.Main.CRLF;

public class HttpRequest extends Socket {
    private final DataOutputStream out;
    private final BufferedReader reader;
    private final String request;
    private final String header;
    private final String method;
    private final Socket socket;
    private FileInputStream fileIn;
    private String path;
    private Map<String, String> params;

    public HttpRequest(Socket socket) throws IOException {
        this.socket = socket;
        InputStream in = socket.getInputStream();
        this.out = new DataOutputStream(socket.getOutputStream());
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.request = this.reader.readLine();
        Logger.info(this.request);

        StringBuilder header = new StringBuilder();
        String line = null;
        while ((line = this.reader.readLine()).length() != 0)
            header.append(line).append(CRLF);
        this.header = header.toString();

        Logger.info("Header:\n\t" + this.header.replaceAll(CRLF, CRLF + "\t"));

        StringTokenizer tokens = new StringTokenizer(this.request);
        this.method = tokens.nextToken(); // skip over the method, which should be "GET"
        this.path = tokens.nextToken();
        int param_start = this.path.indexOf('?');
        this.params = new HashMap<>();
        if (param_start > -1) {
            for (String param : this.path.substring(param_start+1).replaceAll("\\?", "").split("&")) {
                String[] split = param.split("=");
                if (split.length >= 2)
                    this.params.put(split[0], split[1].replaceAll("%20", " "));
                else
                    this.params.put(split[0], "");
            }
            this.path = this.path.substring(0, param_start);

            Logger.info("Params: " + this.params);
        }
        Logger.info("Path: " + this.path);
    }

    public static String getContentType(String path) {
        return "Content-type: " + getMime(path) + CRLF;
    }

    public static String getMime(String path) {
        return switch (path.substring(path.lastIndexOf('.') + 1)) {
            case "htm", "html" -> "text/html";
            case "txt", "css" -> "text/plain";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "png" -> "image/png";
            case "json" -> "application/json";
            default -> "application/octet-stream";
        };
    }

    public static String getStatusMessage(int code) {
        return switch (code) {
            case 200 -> "HTTP/1.0 200 OK" + CRLF;
            case 301 -> "HTTP/1.1 301 Moved Permanently" + CRLF;
            case 308 -> "HTTP/1.1 308 Permanent Redirect" + CRLF;
            case 404 -> "HTTP/1.0 404 Not Found" + CRLF;
            case 500 -> "HTTP/1.0 500 Internal Server Error" + CRLF;
            default -> null;
        };
    }

    public void sendHeader(int statusCode) throws IOException { sendHeader(statusCode, getContentType()); }

    public void sendHeader(int statusCode, String contentType) throws IOException {
        this.out.writeBytes(getStatusMessage(statusCode));
        this.out.writeBytes(contentType);
        this.out.writeBytes(CRLF);
    }

    public void processFileRequest() throws Exception {
        processFileRequest(0);
    }

    public void processFileRequest(int statusCode) throws Exception {
        // Open the requested file.
        this.fileIn = null;
        boolean fileExists = true;
        try {
            this.fileIn = new FileInputStream(this.path);
        } catch (FileNotFoundException e) {
            try {
                this.fileIn = new FileInputStream(this.path + ".html");
                this.appendPath(".html");
            } catch (FileNotFoundException e2) {
                fileExists = false;
            }
        }

        // Construct the response message.
        Logger.info("File exists: " + fileExists);
        if (fileExists)
            Logger.info("MIME type " + getMime());
        String body = null;

        if (fileExists) {
            if (statusCode == 0) statusCode = 200;
        } else {
            Logger.info("Sending 404");
            if (statusCode == 0) {
                this.setPath("./public/404.html");
                processFileRequest(404);
                return;
            }
            statusCode = 404;
            body = "<HTML><HEAD><TITLE>Not Found</TITLE></HEAD><BODY>Not Found</BODY></HTML>";
        }

        //sendHeader(statusCode, contentTypeLine, os);

        if (fileExists) this.sendFile(statusCode);
        else sendText(statusCode, body);
    }

    public void sendFile(int statusCode) throws Exception {
        Logger.info("Sending file: " + this.path);
        sendHeader(statusCode);
        sendBytes();
        this.fileIn.close();
    }

    public void sendText(int statusCode, String entityBody, String MIME) throws IOException {
        Logger.info("Sending " + MIME + " text:\n\t" + entityBody.replaceAll("\n", "\n\t") + "\n\n");
        sendHeader(statusCode, "Content-type: " + MIME + CRLF);
        this.out.writeBytes(entityBody);
    }

    public void sendText(int statusCode, String entityBody) throws IOException {
        Logger.info("Sending text:\n\t" + entityBody.replaceAll("\n", "\n\t") + "\n\n");
        sendHeader(statusCode);
        this.out.writeBytes(entityBody);
    }

    public void sendBytes() throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = this.fileIn.read(buffer)) != -1)
            this.out.write(buffer, 0, bytes);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void prependPath(String path) {
        this.path = path + this.path;
    }

    public void appendPath(String path) {
        this.path = this.path + path;
    }

    public String getMethod() {
        return method;
    }

    public void close() throws IOException {
        this.out.close();
        this.reader.close();
        super.close();
    }

    public String getContentType() {
        return getContentType(this.path);
    }

    public String getMime() {
        return getMime(this.path);
    }

    public Map<String, String> getParams() { return params; }
}
