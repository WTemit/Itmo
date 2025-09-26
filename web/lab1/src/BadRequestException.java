public class BadRequestException extends RuntimeException {
    String errMsg;
    public BadRequestException(String errMsg) {
        super("You provided bad request!\n" + errMsg);
        this.errMsg = errMsg;
    }
    public String getErrMsg(){
        return "You provided bad request!\n" + errMsg;
    }
}
