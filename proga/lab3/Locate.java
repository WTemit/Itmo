public enum Locate {
    Under_BRIDGE("на мосте"),
    OVER_BRIDGE("под мостом"),
    SHORE("берег"),
    EMBANKMENT("на набережной"),
    CITY("город");
    private String type;

    Locate(String type) {
        this.type = type;
    }

    public String Type() {
        return this.type;
    }
}
