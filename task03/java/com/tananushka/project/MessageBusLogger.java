package com.tananushka.project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MessageBusLogger {
   private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

   private static final int THREAD_NAME_COLUMN_WIDTH = 20;

   private static final String ANSI_RESET = "\u001B[0m";
   private static final String ANSI_RED = "\u001B[31m";
   private static final String ANSI_GREEN = "\u001B[32m";
   private static final String ANSI_YELLOW = "\u001B[33m";
   private static final String ANSI_BLUE = "\u001B[34m";
   private static final String ANSI_PURPLE = "\u001B[35m";
   private static final String ANSI_CYAN = "\u001B[36m";

   private static final String HTML_RED = "#ff0000";
   private static final String HTML_GREEN = "#008000";
   private static final String HTML_YELLOW = "#ffaa00";
   private static final String HTML_BLUE = "#0000ff";
   private static final String HTML_PURPLE = "#8b00ff";
   private static final String HTML_CYAN = "#000000";
   private static final String HTML_RESET = "#000000";

   private static final String ICON_INFO = "💡";
   private static final String ICON_LOCK = "🔒";
   private static final String ICON_SUCCESS = "✅";
   private static final String ICON_UNLOCK = "🔓";
   private static final String ICON_STATS = "📊";
   private static final String ICON_ERROR = "❌";
   private static final String ICON_WARNING = "⚠️";
   private static final String ICON_SPARKLES = "✨";
   private static final String ICON_PLAY = "▶️";
   private static final String ICON_STOP = "⏹️";
   private static final String ICON_SKULL = "💀";
   private static final String ICON_REFRESH = "🔄";
   private static final String ICON_CHART = "📈";
   private static final String ICON_SYS_INFO = "ℹ️";
   private static final String ICON_THREAD = "🧵";

   private static final String LOG_DIR = "task03";
   private static final String LOG_FILE = Paths.get(LOG_DIR, "task-3-logs").toString();

   static {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE + ".md", false))) {
         writer.write("");
      } catch (IOException e) {
         System.err.println("Failed to initialize MD log file: " + e.getMessage());
      }
   }

   private static void logWithIcon(String threadName, String icon, String message, boolean isError) {
      String color = isError ? ANSI_RED : getColorForThread(threadName);
      String timeColor = ANSI_CYAN;
      String timestamp = LocalTime.now().format(formatter);

      System.out.printf("%s[%s]%s %-20s %-2s %s%n",
            timeColor, timestamp, ANSI_RESET,
            String.format("%s[%s]%s", color, padRight(threadName, THREAD_NAME_COLUMN_WIDTH), ANSI_RESET),
            icon,
            color + message + ANSI_RESET);

      String htmlColor = isError ? HTML_RED : getHtmlColorForThread(threadName);
      String logLineHtml = String.format(
            "<pre style=\"margin:0;padding:0;color:%s;\">[%s] [<span style=\"display:inline-block;width:%dch;color:%s;\">%s</span>] <span style=\"color:%s;\">%s %s</span></pre>",
            HTML_CYAN,
            timestamp,
            THREAD_NAME_COLUMN_WIDTH,
            htmlColor,
            escapeHtml(threadName),
            htmlColor,
            icon,
            escapeHtml(message)
      );
      writeToMdFile(logLineHtml);

      String logLineText = String.format(
            "[%s] [%s] %s %s",
            timestamp,
            padRight(threadName, THREAD_NAME_COLUMN_WIDTH),
            icon,
            message
      );
      writeToLogFile(logLineText);
   }

   private static String escapeHtml(String text) {
      if (text == null) return "";
      return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
   }

   private static void writeToMdFile(String logLine) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE + ".md", true))) {
         writer.write(logLine);
      } catch (IOException e) {
         System.err.println("Failed to write to MD log file: " + e.getMessage());
      }
   }

   private static void writeToLogFile(String logLine) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE + ".log", true))) {
         writer.write(logLine);
         writer.newLine();
      } catch (IOException e) {
         System.err.println("Failed to write to text log file: " + e.getMessage());
      }
   }

   private static String getColorForThread(String threadName) {
      return getString(threadName, ANSI_PURPLE, ANSI_GREEN, ANSI_YELLOW, ANSI_BLUE, ANSI_RED, ANSI_RESET);
   }

   private static String getHtmlColorForThread(String threadName) {
      return getString(threadName, HTML_PURPLE, HTML_GREEN, HTML_YELLOW, HTML_BLUE, HTML_RED, HTML_RESET);
   }

   private static String getString(String threadName, String ansiPurple, String ansiGreen, String ansiYellow, String ansiBlue, String ansiRed, String ansiReset) {
      if (threadName.equalsIgnoreCase("Main")) {
         return ansiPurple;
      } else if (threadName.contains("Producer")) {
         return ansiGreen;
      } else if (threadName.contains("Consumer")) {
         return ansiYellow;
      } else if (threadName.contains("Another string")) {
         return ansiBlue;
      } else if (threadName.contains("One more string")) {
         return ansiRed;
      }
      return ansiReset;
   }

   private static String padRight(String s, int n) {
      return String.format("%-" + n + "s", s);
   }

   public static void log(String threadName, String message) {
      logWithIcon(threadName, ICON_INFO, message, false);
   }

   public static void logThreadCreated(String threadName) {
      logWithIcon(threadName, ICON_THREAD, "Thread created", false);
   }

   public static void logThreadState(String threadName, String state) {
      logWithIcon(threadName, ICON_REFRESH, "State changed to: " + state, false);
   }

   public static void logLockAttempt(String threadName, String lockName) {
      logWithIcon(threadName, ICON_LOCK, "Attempting to acquire " + lockName, false);
   }

   public static void logLockAcquired(String threadName, String lockName) {
      logWithIcon(threadName, ICON_SUCCESS, "Acquired " + lockName, false);
   }

   public static void logLockReleased(String threadName, String lockName) {
      logWithIcon(threadName, ICON_UNLOCK, "Released " + lockName, false);
   }

   public static void logOperation(String threadName, String operation) {
      logWithIcon(threadName, ICON_STATS, operation, false);
   }

   public static void logResourceUsage(String threadName, String resource, String usage) {
      logWithIcon(threadName, ICON_CHART, resource + " usage: " + usage, false);
   }

   public static void logSuccess(String threadName, String message) {
      logWithIcon(threadName, ICON_SPARKLES, "SUCCESS: " + message, false);
   }

   public static void logError(String threadName, String message) {
      logWithIcon(threadName, ICON_ERROR, "ERROR: " + message, true);
   }

   public static void logWarning(String threadName, String message) {
      logWithIcon(threadName, ICON_WARNING, "WARNING: " + message, false);
   }

   public static void logStart(String threadName, String operation) {
      logWithIcon(threadName, ICON_PLAY, "Started: " + operation, false);
   }

   public static void logStop(String threadName, String operation) {
      logWithIcon(threadName, ICON_STOP, "Stopped: " + operation, false);
   }

   public static void logDeadlock(String threadName) {
      logWithIcon(threadName, ICON_SKULL, "DEADLOCK DETECTED!", true);
   }

   public static void logSystemInfo(String threadName, String info) {
      logWithIcon(threadName, ICON_SYS_INFO, info, false);
   }

   public static void logSeparator() {
      String separator = "=".repeat(50);
      System.out.println(separator);
      String htmlSeparator = String.format(
            "<pre style=\"margin:0;padding:0;color:#888;\">%s</pre>", separator
      );
      writeToMdFile(htmlSeparator);
      writeToLogFile(separator);
   }

   public static void closeMdLogFile() {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE + ".md", true))) {
         writer.write("");
      } catch (IOException e) {
         System.err.println("Failed to close MD log file: " + e.getMessage());
      }
   }
}
