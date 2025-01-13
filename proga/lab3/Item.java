public record Item(String name, String state) {
    @Override
    public String toString() {
        return (name +" оказались ему "+ state);
    }
}