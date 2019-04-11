package utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.util.logging.Level.SEVERE;

public class Utils {
    private static final String LOG_FOLDER = "log/";
    public static final String RMTZ_LOGGER = "RMTZ Logger";

    private final static Logger logger = Logger.getLogger(RMTZ_LOGGER);

    public static void setupLogger(boolean logToFile) {
        if (logToFile) {
            File directory = new File(LOG_FOLDER);
            if (!directory.exists()) directory.mkdir();

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
            FileHandler fileHandler;
            try {
                fileHandler = new FileHandler(LOG_FOLDER + "botlog_" + timeStamp + ".log");
                fileHandler.setFormatter(new SimpleFormatter());

                logger.addHandler(fileHandler);
            } catch (IOException e) {
                logger.log(SEVERE, "Error while trying to configure file logging...\n", e);
            }
        }
    }
}
