package finance.mngmt.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Transaction {
    private String id;
    private double amount;
    private TransactionType type;
    private String category;
    private LocalDateTime date;
    private String description;

    public Transaction(double amount, TransactionType type, String category, String description) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = LocalDateTime.now();
        this.description = description;
    }

    public Transaction(String id, double amount, TransactionType type, String category,
                       LocalDateTime date, String description) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public String getCategory() { return category; }
    public LocalDateTime getDate() { return date; }
    public String getDescription() { return description; }

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s - %.2f (%s)",
                getFormattedDate(),
                type.getDescription(),
                category,
                amount,
                description.isEmpty() ? "без описания" : description
        );
    }
}