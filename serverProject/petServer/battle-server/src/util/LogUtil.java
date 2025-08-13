package util;

import common.load.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    static Logger debugLogger = LoggerFactory.getLogger("Debug");
    static Logger infoLogger = LoggerFactory.getLogger("Info");
    static Logger warnLogger = LoggerFactory.getLogger("Warn");
    static Logger errorLogger = LoggerFactory.getLogger("Error");
    static Logger exceptionLogger = LoggerFactory.getLogger("Exception");

    public static synchronized void debug(String msg) {
        if (ServerConfig.getInstance().isDebug()) {
            debugLogger.debug(msg);
        }
    }

    public static synchronized void debug(String msg, Object... params) {
        if (ServerConfig.getInstance().isDebug()) {
            debugLogger.debug(msg, params);
        }
    }

    public static synchronized void info(String msg) {
        infoLogger.info(msg);
    }

    public static synchronized void info(String msg, Object... params) {
        infoLogger.info(msg, params);
    }


    public static synchronized void warn(String msg) {
        warnLogger.warn(msg);
    }

    public static synchronized void warn(String msg, Object... params) {
        warnLogger.warn(msg, params);
    }


    public static synchronized void error(String msg) {
        errorLogger.error(msg);
    }

    public static synchronized void error(String msg, Object... params) {
        errorLogger.error(msg, params);
    }

    public static synchronized void printStackTrace(Exception e) {
        if (e != null) {
            String stackInfo = formatStackInfo(e);
            exceptionLogger.error(stackInfo);
        }
    }

    private static String formatStackInfo(Exception e) {
        if (e != null) {
            StackTraceElement[] stackArray = e.getStackTrace();
            String stackInfo = formatStackTrace(stackArray, 0);
            Throwable cause = e.getCause();
            return cause != null ? cause.toString() + stackInfo : e.toString() + stackInfo;
        } else {
            return "";
        }
    }

    private static String formatStackTrace(StackTraceElement[] stackArray, int skipCount) {
        if (stackArray == null) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("\n");
            for (int i = skipCount; i < stackArray.length; ++i) {
                StackTraceElement element = stackArray[i];
                sb.append("\tat " + element.toString() + "\n");
            }
            return sb.toString();
        }
    }
}
