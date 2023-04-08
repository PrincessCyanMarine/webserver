package net.cyanmarine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static net.cyanmarine.Main.CRLF;

public class Pages {
    private static final Map<String, Function<HttpRequest, Boolean>> pages = new HashMap<>();

    public static void addPage(String regex, Function<HttpRequest, Boolean> function) {
        pages.put(regex, function);
    }

    public static Boolean getPage(HttpRequest request) {
        String match = null;
        for (String key : pages.keySet()) {
            if (request.getPath().matches("^" + key + "$")) {
                match = key;
                break;
            }
        }
        if (match == null) return false;
        Function<HttpRequest, Boolean> function = pages.get(match);
        if (function == null) return false;
        Logger.info("Found match for " + request.getPath());
        try {
            return function.apply(request);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.toString());
            return false;
        }
    }

    public static void init() throws IOException {
        Pages.addPage("/api/exit", (HttpRequest request) -> {
            Logger.info("Exiting...");
            try { request.sendText(200, "Exiting...", HttpRequest.getMime(".txt")); } catch (IOException ignored) {  }
            Main.running.set(false);
            return true;
        });

        Pages.addPage("/api/files/?.*", (HttpRequest request) -> readPath(request, "./public/"));
        // Pages.addPage("/api/system/files?.*", (HttpRequest request) -> readPath(request, ""));
    }

    private static boolean readPath(HttpRequest request, String start_path) {
        String path = request.getParams().get("path");
        if (path == null) path = "/";
        File fs = new File(start_path + path);
        if (fs.isDirectory()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            String[] files = fs.list();
            //if (!path.matches("^/files/*$")) sb.append("{\"path\": \"..\", \"isDir\": false}");
            if (files != null) {
                for (String file : files) {
                    sb.append("{\"path\": \"").append(file).append("\", \"isDir\": ").append(new File(fs, file).isDirectory() ? "true" : "false").append("}");
                    if (!file.equals(files[files.length - 1])) sb.append(",");
                }

            }
            sb.append("]");
            try {
                request.setPath(".json");
                request.sendText(200, sb.toString());
            } catch (IOException e) {
                try {
                    request.sendText(500, sb.toString());
                } catch (IOException ex) {
                    e.printStackTrace();
                    Logger.error(e.toString());
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String getRedirect(String path) {
        Logger.info("Redirecting to " + path);
        return "Location: " + path + CRLF;
    }
}
