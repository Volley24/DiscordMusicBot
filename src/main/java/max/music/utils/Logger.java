package max.music.utils;

public class Logger {
    public enum LogType{
        NONE, INFO, WARN, ERROR
    }
    private static final LogType CURRENT_LOG_TYPE = LogType.INFO;

    public static void log(String message, LogType logType){
        if(logType.ordinal() <= CURRENT_LOG_TYPE.ordinal()){
            System.out.println("[" + logType + "]: " + message);
        }
    }
}
