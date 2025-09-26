import com.fastcgi.FCGIInterface;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class RequestGetter {
    private static final Logger logger = Logger.getLogger(RequestGetter.class.getName());

    public static Map<String, String> getRequest(){

        logger.info("Got into getrequest");
        long startTime = System.nanoTime();
        logger.info("prepared to get property");
        String queryString = FCGIInterface.request.params.getProperty("QUERY_STRING");
        if (queryString == null || queryString.isBlank()) {
            throw new BadRequestException("No Input!");
        }
        logger.info("checked querry not null, getting ready to parse query");

        Map<String, String> params = parseQuery(queryString);
        logger.info("parsed query");
        double x = Double.parseDouble(params.get("x"));
        if(x > 5 || x < -4){
            throw new BadRequestException("X exceed limit!");
        }
        double y = Double.parseDouble(params.get("y"));
        if(y > 5 || y < -5){
            throw new BadRequestException("Y exceed limit!");
        }
        double r = Double.parseDouble(params.get("r"));
        if(r > 3 || r < 1){
            throw new BadRequestException("R exceed limit!");
        }
        logger.info("checked nums");
        boolean isHit = Utility.checkArea(x, y, r);
        logger.info("checked area");
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        long executionTime = (System.nanoTime() - startTime);
        params.put("curTime", currentTime);
        params.put("execTime", String.valueOf(executionTime));
        params.put("isHit", String.valueOf(isHit));
        return params;
    }

    public static Map<String, String> parseQuery(String queryString){
        Map<String, String> params = new HashMap<>();
        String[] paramsSplit = queryString.split("&");
        if (paramsSplit.length != 3){
            throw new BadRequestException("Not enough params");
        }
        for (String param : paramsSplit) {
            if(!(param.matches("\\w+=-?\\d+.?\\d*") && param.length()>2 && param.length()<8)){
                throw new BadRequestException("TF you doing with those params?...");
            }
            String[] pair = param.split("=");
            if (pair.length == 2) {
                params.put(pair[0], pair[1]);
            }
        }
        if(!(params.containsKey("x") && params.containsKey("y") && params.containsKey("r") )){
            throw new BadRequestException("Query is corrupted!");
        }
        return params;
    }
}
