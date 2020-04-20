public enum Services {
    PEOPLE(0, "people"), LOAD(1, "load"), SATELLITE(2, "satellite");

    public int serviceTypeNumber;
    public String name;

    Services(int number, String name) {
        serviceTypeNumber = number;
        this.name = name;
    }

    public static String getName(int serviceNumber) {
        Services service = values()[serviceNumber];
        if (service == null) {
            throw new IllegalArgumentException("Invalid type number: " + serviceNumber);
        }
        return service.name;
    }
}
