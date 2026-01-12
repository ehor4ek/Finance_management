package finance.mngmt.model;

public enum AlertType {
    BUDGET_EXCEEDED("Превышен бюджет"),
    BUDGET_WARNING("Бюджет почти исчерпан"),
    NEGATIVE_BALANCE("Отрицательный баланс"),
    LOW_BALANCE("Низкий баланс");

    private final String message;

    AlertType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}