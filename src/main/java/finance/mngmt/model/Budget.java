package finance.mngmt.model;

public class Budget {
    private String category;
    private double limit;
    private double currentSpending;
    private double warningThreshold; // порог предупреждения (например, 80%)

    public Budget(String category, double limit) {
        this(category, limit, 0.8); // по умолчанию предупреждение при 80%
    }

    public Budget(String category, double limit, double warningThreshold) {
        this.category = category;
        this.limit = limit;
        this.currentSpending = 0;
        this.warningThreshold = warningThreshold;
    }

    public double getRemaining() {
        return limit - currentSpending;
    }

    public boolean isExceeded() {
        return currentSpending > limit;
    }

    public boolean isWarning() {
        return currentSpending >= limit * warningThreshold && !isExceeded();
    }

    public void addSpending(double amount) {
        this.currentSpending += amount;
    }

    public void resetSpending() {
        this.currentSpending = 0;
    }

    // Геттеры и сеттеры
    public String getCategory() { return category; }
    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }
    public double getCurrentSpending() { return currentSpending; }
    public void setCurrentSpending(double currentSpending) { this.currentSpending = currentSpending; }
    public double getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(double warningThreshold) { this.warningThreshold = warningThreshold; }

    @Override
    public String toString() {
        return String.format("Бюджет '%s': лимит=%.2f, потрачено=%.2f, осталось=%.2f",
                category, limit, currentSpending, getRemaining());
    }
}