package finance.mngmt.model;

import java.util.*;
import java.util.stream.Collectors;

public class Wallet {
    private String owner;
    private List<Transaction> transactions;
    private Map<String, Budget> budgets;
    private Set<String> categories;

    public Wallet(String owner) {
        this.owner = owner;
        this.transactions = new ArrayList<>();
        this.budgets = new HashMap<>();
        this.categories = new HashSet<>();

        // Добавляем стандартные категории
        addDefaultCategories();
    }

    private void addDefaultCategories() {
        // Стандартные категории доходов
        categories.add("Зарплата");
        categories.add("Бонус");
        categories.add("Инвестиции");
        categories.add("Подарок");

        // Стандартные категории расходов
        categories.add("Еда");
        categories.add("Развлечения");
        categories.add("Коммунальные услуги");
        categories.add("Транспорт");
        categories.add("Такси");
        categories.add("Одежда");
        categories.add("Здоровье");
        categories.add("Образование");
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        categories.add(transaction.getCategory());

        // Если это расход и есть бюджет для этой категории - обновляем бюджет
        if (transaction.getType() == TransactionType.EXPENSE) {
            Budget budget = budgets.get(transaction.getCategory());
            if (budget != null) {
                budget.addSpending(transaction.getAmount());
            }
        }
    }

    public void removeTransaction(String transactionId) {
        transactions.removeIf(t -> t.getId().equals(transactionId));
    }

    public void addCategory(String category) {
        categories.add(category);
    }

    public void removeCategory(String category) {
        // Не удаляем категорию, если есть транзакции с этой категорией
        boolean hasTransactions = transactions.stream()
                .anyMatch(t -> t.getCategory().equals(category));

        if (!hasTransactions) {
            categories.remove(category);
            budgets.remove(category);
        }
    }

    public void setBudget(String category, double limit) {
        budgets.put(category, new Budget(category, limit));
    }

    public void removeBudget(String category) {
        budgets.remove(category);
    }

    public double getBalance() {
        double income = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double expense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        return income - expense;
    }

    // Геттеры
    public String getOwner() { return owner; }
    public List<Transaction> getTransactions() { return new ArrayList<>(transactions); }
    public Map<String, Budget> getBudgets() { return new HashMap<>(budgets); }
    public Set<String> getCategories() { return new HashSet<>(categories); }

    public List<Transaction> getTransactionsByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());  // Исправлено с toList() на collect(Collectors.toList())
    }

    public List<Transaction> getIncomeTransactions() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .collect(Collectors.toList());  // Исправлено с toList() на collect(Collectors.toList())
    }

    public List<Transaction> getExpenseTransactions() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());  // Исправлено с toList() на collect(Collectors.toList())
    }

    public double getTotalIncome() {
        return getIncomeTransactions().stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses() {
        return getExpenseTransactions().stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @Override
    public String toString() {
        return String.format("Кошелек пользователя %s: баланс=%.2f, транзакций=%d",
                owner, getBalance(), transactions.size());
    }
}