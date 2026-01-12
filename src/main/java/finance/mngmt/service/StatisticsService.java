package finance.mngmt.service;

import finance.mngmt.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsService {

    public Map<String, Object> generateFullReport(Wallet wallet, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        // Основная статистика
        report.put("owner", wallet.getOwner());
        report.put("period", startDate + " - " + endDate);
        report.put("generatedAt", LocalDateTime.now());

        // Баланс
        report.put("currentBalance", wallet.getBalance());
        report.put("totalIncome", wallet.getTotalIncome());
        report.put("totalExpenses", wallet.getTotalExpenses());

        // Транзакции за период
        List<Transaction> periodTransactions = wallet.getTransactions().stream()
                .filter(t -> !t.getDate().toLocalDate().isBefore(startDate))
                .filter(t -> !t.getDate().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        double periodIncome = periodTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double periodExpenses = periodTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        report.put("periodIncome", periodIncome);
        report.put("periodExpenses", periodExpenses);
        report.put("periodBalance", periodIncome - periodExpenses);

        // Доходы по категориям за период
        Map<String, Double> incomeByCategory = periodTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        // Расходы по категориям за период
        Map<String, Double> expensesByCategory = periodTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        report.put("incomeByCategory", incomeByCategory);
        report.put("expensesByCategory", expensesByCategory);

        // Бюджеты
        Map<String, Map<String, Object>> budgetsReport = new HashMap<>();
        for (Budget budget : wallet.getBudgets().values()) {
            Map<String, Object> budgetInfo = new HashMap<>();
            budgetInfo.put("limit", budget.getLimit());
            budgetInfo.put("spent", budget.getCurrentSpending());
            budgetInfo.put("remaining", budget.getRemaining());
            budgetInfo.put("exceeded", budget.isExceeded());
            budgetInfo.put("warning", budget.isWarning());

            // Расходы по этой категории за период
            double categoryPeriodExpenses = periodTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getCategory().equals(budget.getCategory()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            budgetInfo.put("periodSpent", categoryPeriodExpenses);

            budgetsReport.put(budget.getCategory(), budgetInfo);
        }

        report.put("budgets", budgetsReport);

        // Анализ
        report.put("analysis", performAnalysis(wallet, periodTransactions));

        return report;
    }

    private Map<String, Object> performAnalysis(Wallet wallet, List<Transaction> periodTransactions) {
        Map<String, Object> analysis = new HashMap<>();

        double avgDailyExpense = periodTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .average()
                .orElse(0);

        analysis.put("avgDailyExpense", avgDailyExpense);

        // Самые затратные категории
        Map<String, Double> topExpenseCategories = periodTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        analysis.put("topExpenseCategories", topExpenseCategories);

        // Оценка финансового здоровья
        double income = wallet.getTotalIncome();
        double expenses = wallet.getTotalExpenses();
        double savingsRate = income > 0 ? (income - expenses) / income * 100 : 0;

        String financialHealth;
        if (savingsRate > 20) financialHealth = "Отличное";
        else if (savingsRate > 10) financialHealth = "Хорошее";
        else if (savingsRate > 0) financialHealth = "Удовлетворительное";
        else financialHealth = "Требует внимания";

        analysis.put("savingsRate", savingsRate);
        analysis.put("financialHealth", financialHealth);

        return analysis;
    }

    public void printReport(Map<String, Object> report) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ОТЧЕТ ПО ФИНАНСАМ");
        System.out.println("=".repeat(60));

        System.out.printf("Владелец: %s%n", report.get("owner"));
        System.out.printf("Период: %s%n", report.get("period"));
        System.out.printf("Сгенерирован: %s%n", report.get("generatedAt"));

        System.out.println("\n--- ОБЩАЯ ИНФОРМАЦИЯ ---");
        System.out.printf("Текущий баланс: %,.2f%n", report.get("currentBalance"));
        System.out.printf("Общий доход: %,.2f%n", report.get("totalIncome"));
        System.out.printf("Общие расходы: %,.2f%n", report.get("totalExpenses"));

        System.out.printf("\n--- ЗА ПЕРИОД ---%n");
        System.out.printf("Доход за период: %,.2f%n", report.get("periodIncome"));
        System.out.printf("Расход за период: %,.2f%n", report.get("periodExpenses"));
        System.out.printf("Баланс за период: %,.2f%n", report.get("periodBalance"));

        // Доходы по категориям
        @SuppressWarnings("unchecked")
        Map<String, Double> incomeByCategory = (Map<String, Double>) report.get("incomeByCategory");
        if (!incomeByCategory.isEmpty()) {
            System.out.println("\n--- ДОХОДЫ ПО КАТЕГОРИЯМ ---");
            incomeByCategory.forEach((category, amount) ->
                    System.out.printf("  %-20s %,.2f%n", category + ":", amount));
        }

        // Расходы по категориям
        @SuppressWarnings("unchecked")
        Map<String, Double> expensesByCategory = (Map<String, Double>) report.get("expensesByCategory");
        if (!expensesByCategory.isEmpty()) {
            System.out.println("\n--- РАСХОДЫ ПО КАТЕГОРИЯМ ---");
            expensesByCategory.forEach((category, amount) ->
                    System.out.printf("  %-20s %,.2f%n", category + ":", amount));
        }

        // Бюджеты
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> budgets = (Map<String, Map<String, Object>>) report.get("budgets");
        if (!budgets.isEmpty()) {
            System.out.println("\n--- БЮДЖЕТЫ ---");
            System.out.printf("%-20s %-15s %-15s %-15s %s%n",
                    "Категория", "Лимит", "Потрачено", "Осталось", "Статус");
            System.out.println("-".repeat(80));

            budgets.forEach((category, info) -> {
                String status = (boolean) info.get("exceeded") ? "ПРЕВЫШЕН" :
                        (boolean) info.get("warning") ? "ВНИМАНИЕ" : "OK";
                System.out.printf("%-20s %,-15.2f %,-15.2f %,-15.2f %s%n",
                        category,
                        (double) info.get("limit"),
                        (double) info.get("spent"),
                        (double) info.get("remaining"),
                        status);
            });
        }

        // Анализ
        @SuppressWarnings("unchecked")
        Map<String, Object> analysis = (Map<String, Object>) report.get("analysis");
        System.out.println("\n--- АНАЛИЗ ---");
        System.out.printf("Средний дневной расход: %,.2f%n", analysis.get("avgDailyExpense"));
        System.out.printf("Норма сбережений: %.1f%%%n", analysis.get("savingsRate"));
        System.out.printf("Финансовое здоровье: %s%n", analysis.get("financialHealth"));

        @SuppressWarnings("unchecked")
        Map<String, Double> topCategories = (Map<String, Double>) analysis.get("topExpenseCategories");
        if (!topCategories.isEmpty()) {
            System.out.println("\nТоп-5 затратных категорий:");
            topCategories.forEach((category, amount) ->
                    System.out.printf("  %-20s %,.2f%n", category + ":", amount));
        }

        System.out.println("=".repeat(60));
    }
}