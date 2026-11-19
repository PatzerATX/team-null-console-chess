package chess.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Sets up Java's built-in logging ({@code java.util.logging}) to write one
 * line per event to {@code logs/chess.log}.
 */
public final class LoggingSetup {

    /** Name of the top-level logger for the whole program. */
    public static final String ROOT_LOGGER = "chess";

    /** Folder the log file goes in. */
    private static final String LOG_FOLDER = "logs";

    /** Keeps a reference so the configured logger isn't garbage-collected. */
    private static Logger configured;

    /** Prevents creating objects; this class only holds static methods. */
    private LoggingSetup() {
    }

    /**
     * Sends all {@code chess.*} log messages to {@code logs/chess.log},
     * adding to the file across runs. Safe to call more than once.
     *
     * @return {@code true} if the log file is ready, {@code false} if it couldn't be opened
     */
    public static synchronized boolean configure() {
        if (configured != null) {
            return true;
        }
        Logger root = Logger.getLogger(ROOT_LOGGER);
        try {
            File folder = new File(LOG_FOLDER);
            if (!folder.isDirectory() && !folder.mkdirs()) {
                return false;
            }
            FileHandler file = new FileHandler(LOG_FOLDER + "/chess.log", true);
            file.setFormatter(new OneLineFormatter());
            root.addHandler(file);
            root.setLevel(Level.INFO);
            configured = root;
            return true;
        } catch (IOException e) {
            root.log(Level.WARNING, "Could not open the log file; logging to the console only.", e);
            return false;
        }
    }

    /**
     * Formats a log record as {@code 2026-12-02 14:05:31 INFO  message}.
     */
    private static final class OneLineFormatter extends Formatter {

        /**
         * Formats one record.
         *
         * @param record the log record
         * @return one line of text
         */
        @Override
        public String format(LogRecord record) {
            String line = String.format("%1$tF %1$tT %2$-7s %3$s%n",
                    record.getMillis(), record.getLevel().getName(), formatMessage(record));
            if (record.getThrown() != null) {
                line += "        " + record.getThrown() + System.lineSeparator();
            }
            return line;
        }
    }
}
