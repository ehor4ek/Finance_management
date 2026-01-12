package finance.mngmt.service;

import finance.mngmt.model.*;
import finance.mngmt.exception.*;
import finance.mngmt.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceService {
    private final UserService userService;
    private final AlertService alertService;

    public FinanceService(UserService userService, AlertService alertService) {
        this.userService = userService;
        this.alertService = alertService;
    }

    public void addIncome(double amount, String category, String description) {
        validateTransaction(amount, category);

        User user = userService.getCurrentUser();
        Transaction transaction = new Transaction(amount, TransactionType.INCOME, category, description);
        user.getWallet().addTransaction(transaction);

        alertService.checkBalanceAlerts(user.getWallet());
        System.out.printf("Доход добавлен: %.2f в категории '%s'%n", amount, category);
    }

    public void addExpense(double amount, String category, String description) {
        validateTransaction(amount, category);

        User user = userService.getCurrentUser();
        Transaction transaction = new Transaction(amount, TransactionType.EXPENSE, category, description);
        user.getWallet().addTransaction(transaction);

        // Проверяем бюджеты и баланс
        alertService.checkBudgetAlerts(user.getWallet());
        alertService.checkBalanceAlerts(user.getWallet());

        System.out.printf("Расход добавлен: %.2f в категории '%s'%n", amount, category);
    }

    public void addCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Название категории не может быть пустым");
        }

        User user = userService.getCurrentUser();
        user.getWallet().addCategory(category);
        System.out.println("Категория '" + category + "' добавлена");
    }

    public void removeCategory(String category) {
        User user = userService.getCurrentUser();
        user.getWallet().removeCategory(category);
        System.out.println("Категория '" + category + "' удалена");
    }

    public void setBudget(String category, double limit) {
        validateBudget(limit);

        User user = userService.getCurrentUser();
        user.getWallet().setBudget(category, limit);
        System.out.printf("Бюджет установлен: категория '%s', лимит %.2f%n", category, limit);
    }

    public void editBudget(String category, double newLimit) {
        validateBudget(newLimit);

        User user = userService.getCurrentUser();
        Map<String, Budget> budgets = user.getWallet().getBudgets();

        if (!budgets.containsKey(category)) {
            throw new CategoryNotFoundException("Бюджет для категории '" + category + "' не найден");
        }

        budgets.get(category).setLimit(newLimit);
        System.out.printf("Бюджет обновлен: категория '%s', новый лимит %.2f%n", category, newLimit);
    }

    public void removeBudget(String category) {
        User user = userService.getCurrentUser();
        user.getWallet().removeBudget(category);
        System.out.println("Бюджет для категории '" + category + "' удален");
    }

    private void validateTransaction(double amount, String category) {
        if (amount <= 0) {
            throw new ValidationException("Сумма должна быть положительной");
        }

        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Категория не может быть пустой");
        }

        if (!userService.isAuthenticated()) {
            throw new AuthorizationException("Пользователь не авторизован");
        }
    }

    private void validateBudget(double limit) {
        if (limit <= 0) {
            throw new ValidationException("Лимит бюджета должен быть положительным");
        }
    }

    public Map<String, Object> getStatistics(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();
        Wallet wallet = user.getWallet();

        Map<String, Object> stats = new HashMap<>();

        // Фильтруем транзакции по дате
        List<Transaction> filteredTransactions = wallet.getTransactions().stream()
                .filter(t -> !t.getDate().toLocalDate().isBefore(startDate))
                .filter(t -> !t.getDate().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Общие доходы и расходы
        double totalIncome = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpenses = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Доходы по категориям
        Map<String, Double> incomeByCategory = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // Расходы по категориям
        Map<String, Double> expensesByCategory = filteredTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        stats.put("totalIncome", totalIncome);
        stats.put("totalExpenses", totalExpenses);
        stats.put("balance", totalIncome - totalExpenses);
        stats.put("incomeByCategory", incomeByCategory);
        stats.put("expensesByCategory", expensesByCategory);
        stats.put("period", startDate + " - " + endDate);
        stats.put("transactionCount", filteredTransactions.size());

        return stats;
    }

    public Map<String, Map<String, Object>> getCategoryStatistics(List<String> categories) {
        User user = userService.getCurrentUser();
        Wallet wallet = user.getWallet();

        Map<String, Map<String, Object>> result = new HashMap<>();

        for (String category : categories) {
            if (!wallet.getCategories().contains(category)) {
                throw new CategoryNotFoundException("Категория не найдена: " + category);
            }

            Map<String, Object> categoryStats = new HashMap<>();

            double categoryIncome = wallet.getTransactions().stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .filter(t -> t.getCategory().equals(category))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double categoryExpenses = wallet.getTransactions().stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getCategory().equals(category))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            categoryStats.put("income", categoryIncome);
            categoryStats.put("expenses", categoryExpenses);
            categoryStats.put("balance", categoryIncome - categoryExpenses);

            // Информация о бюджете
            Budget budget = wallet.getBudgets().get(category);
            if (budget != null) {
                categoryStats.put("budgetLimit", budget.getLimit());
                categoryStats.put("budgetSpent", budget.getCurrentSpending());
                categoryStats.put("budgetRemaining", budget.getRemaining());
                categoryStats.put("budgetExceeded", budget.isExceeded());  // boolean значение
            }

            result.put(category, categoryStats);
        }

        return result;
    }

    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        User user = userService.getCurrentUser();

        return user.getWallet().getTransactions().stream()
                .filter(t -> !t.getDate().toLocalDate().isBefore(startDate))
                .filter(t -> !t.getDate().toLocalDate().isAfter(endDate))
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate())) // Сначала новые
                .collect(Collectors.toList());
    }

    public void transferMoney(String toUsername, double amount, String description,
                              UserRepository userRepository) {
        User sender = userService.getCurrentUser();
        User receiver = userRepository.getUser(toUsername);

        if (receiver == null) {
            throw new ValidationException("Получатель не найден: " + toUsername);
        }

        if (sender.equals(receiver)) {
            throw new ValidationException("Нельзя перевести деньги самому себе");
        }

        if (amount <= 0) {
            throw new ValidationException("Сумма перевода должна быть положительной");
        }

        if (sender.getWallet().getBalance() < amount) {
            throw new InsufficientFundsException("Недостаточно средств для перевода");
        }

        // Создаем транзакции
        Transaction senderTransaction = new Transaction(
                amount, TransactionType.EXPENSE, "Перевод",
                "Перевод пользователю " + toUsername + ": " + description
        );

        Transaction receiverTransaction = new Transaction(
                amount, TransactionType.INCOME, "Перевод",
                "Перевод от пользователя " + sender.getUsername() + ": " + description
        );

        // Добавляем транзакции
        sender.getWallet().addTransaction(senderTransaction);
        receiver.getWallet().addTransaction(receiverTransaction);

        System.out.printf("Перевод выполнен: %.2f пользователю %s%n", amount, toUsername);
    }
}
