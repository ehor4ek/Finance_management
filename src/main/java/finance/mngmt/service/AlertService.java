package finance.mngmt.service;

import finance.mngmt.model.*;
import java.util.*;

public class AlertService {
    private List<String> alerts;

    public AlertService() {
        this.alerts = new ArrayList<>();
    }

    public void checkBudgetAlerts(Wallet wallet) {
        alerts.clear();

        for (Budget budget : wallet.getBudgets().values()) {
            if (budget.isExceeded()) {
                alerts.add(String.format(
                        AlertType.BUDGET_EXCEEDED.getMessage() +
                                ": Категория '%s'. Лимит: %.2f, Потрачено: %.2f, Превышение: %.2f",
                        budget.getCategory(),
                        budget.getLimit(),
                        budget.getCurrentSpending(),
                        Math.abs(budget.getRemaining())
                ));
            } else if (budget.isWarning()) {
                alerts.add(String.format(
                        AlertType.BUDGET_WARNING.getMessage() +
                                ": Категория '%s'. Лимит: %.2f, Потрачено: %.2f (%.1f%%), Осталось: %.2f",
                        budget.getCategory(),
                        budget.getLimit(),
                        budget.getCurrentSpending(),
                        (budget.getCurrentSpending() / budget.getLimit()) * 100,
                        budget.getRemaining()
                ));
            }
        }
    }

    public void checkBalanceAlerts(Wallet wallet) {
        double balance = wallet.getBalance();
        double totalExpenses = wallet.getTotalExpenses();
        double totalIncome = wallet.getTotalIncome();

        if (balance < 0) {
            alerts.add(String.format(
                    AlertType.NEGATIVE_BALANCE.getMessage() +
                            ": Текущий баланс: %.2f",
                    balance
            ));
        } else if (balance < 100) {
            alerts.add(String.format(
                    AlertType.LOW_BALANCE.getMessage() +
                            ": Текущий баланс: %.2f",
                    balance
            ));
        }

        if (totalExpenses > totalIncome) {
            alerts.add("Внимание: Расходы превышают доходы!");
        }
    }

    public List<String> getAlerts() {
        return new ArrayList<>(alerts);
    }

    public void clearAlerts() {
        alerts.clear();
    }

    public boolean hasAlerts() {
        return !alerts.isEmpty();
    }

    public void printAlerts() {
        if (alerts.isEmpty()) {
            System.out.println("Оповещений нет");
            return;
        }

        System.out.println("\n=== ОПОВЕЩЕНИЯ ===");
        for (String alert : alerts) {
            System.out.println("⚠ " + alert);
        }
        System.out.println("=================\n");
    }
}
