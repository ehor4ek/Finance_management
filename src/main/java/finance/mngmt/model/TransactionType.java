package finance.mngmt.model;

public enum TransactionType {
    INCOME("Доход"),
    EXPENSE("Расход");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
