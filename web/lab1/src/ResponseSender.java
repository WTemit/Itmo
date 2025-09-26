public class ResponseSender {
    public static void sendJSONResponse(String x, String y, String r, String isHit, String currentTime, String executionTime) {
        String json = """
            {"x": %s, "y": %s, "r": %s, "isHit":%s, "curTime": "%s", "execTime": %s}
            """.formatted(x, y, r, isHit, currentTime, executionTime);

        System.out.println("HTTP/1.1 200 OK");
        System.out.println("Content-Type: application/json; charset=utf-8\n");
        System.out.println(json);
    }
    public static void sendErrResponse(BadRequestException e){
        System.out.println("HTTP/1.1 400 Bad Request");
        System.out.println("Content-Type: text/plain; charset=utf-8\n");
        System.out.println(e.getErrMsg());
    }
}
