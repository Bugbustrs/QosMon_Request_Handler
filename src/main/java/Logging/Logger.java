package Logging;

public class Logger {
    private static String ERROR="ERROR";
    private static String INFO="INFO";

    public static void log(String tag,String message){
        System.out.println(tag+" "+message);
    }
}
