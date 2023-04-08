package net.cyanmarine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {
    private static String name;
    private static File file;

    public static void init(String name) throws IOException {
        Logger.name = name;
        Logger.file = new File("logs/log.txt");

        File parent = file.getParentFile();
        if (parent != null)
            parent.mkdirs();
        file.createNewFile();

        log("------------------------------------------------------------------------------------------------------------------------");
        log("Logger initialized for " + name + " at " + getTime());
    }

    public static void info(String message) {
        String formattedMessage = String.format("(%s @ %s): %s", name, getTime(), message);
        System.out.println(formattedMessage);
        log(formattedMessage);
    }
    public static void write(String message){
        System.out.print(message);
        log(message);
    }

    public static void error(String message) {
        String formattedMessage = String.format("(%s @ %s): ERROR\n\t%s", name, getTime(), message);
        System.err.println();
        System.err.println(formattedMessage);
        System.err.println();
        log(formattedMessage);
    }

    public static void log(String message) {
        try {
            FileWriter writer = new FileWriter(file.getPath(), true);
            writer.write(message);
            writer.write(System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTime() {
        LocalDateTime time = LocalDateTime.now();
        return String.format("%02d/%02d/%02d %02d:%02d:%02d", time.getDayOfMonth(), time.getMonth().getValue(), time.getYear() % 1000, time.getHour(), time.getMinute(), time.getSecond());
    }
}
