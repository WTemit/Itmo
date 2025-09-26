import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        try {
            FileHandler fileHandler = new FileHandler(System.getProperty("user.home") + "/fcgi_app.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.INFO);

        } catch (IOException e) {
            System.err.println("CRITICAL: Could not set up logger file handler.");
            e.printStackTrace();
            return;
        }

        FCGIInterface fcgiInterface = new FCGIInterface();
        logger.info("===============================================");
        logger.info("Starting up...");
        while (fcgiInterface.FCGIaccept() >= 0) {
            try {
                logger.info("Got request");
                Map<String, String> params = RequestGetter.getRequest();
                logger.info("Parsed request");
                ResponseSender.sendJSONResponse(params.get("x"),params.get("y"),params.get("r"), params.get("isHit"), params.get("curTime"), params.get("execTime"));
                logger.info("Sent request");

            } catch (BadRequestException e) {
                ResponseSender.sendErrResponse(e);
            }
        }
    }



}