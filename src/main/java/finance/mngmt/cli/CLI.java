package finance.mngmt.cli;

import finance.mngmt.service.*;
import finance.mngmt.repository.*;
import finance.mngmt.model.*;
import finance.mngmt.exception.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

public class CLI {
    private final UserService userService;
    private final FinanceService financeService;
    private final StatisticsService statisticsService;
    private final AlertService alertService;
    private final FileStorage fileStorage;
    private final UserRepository userRepository;
    private final Scanner scanner;

    public CLI() {
        this.userRepository = new UserRepository();
        this.fileStorage = new FileStorage();
        this.alertService = new AlertService();
        this.userService = new UserService(userRepository);
        this.financeService = new FinanceService(userService, alertService);
        this.statisticsService = new StatisticsService();
        this.scanner = new Scanner(System.in);

        // Загружаем пользователей из файла
        loadUsers();
    }

    private void loadUsers() {
        Map<String, User> users = fileStorage.loadUsers();
        for (User user : users.values()) {
            userRepository.addUser(user);
        }
    }

    private void saveUsers() {
        Map<String, User> users = new HashMap<>();
        for (User user : userRepository.getAllUsers()) {
            users.put(user.getUsername(), user);
        }
        fileStorage.saveUsers(users);
    }

    public void start() {
        printWelcome();

        while (true) {
            try {
                if (!userService.isAuthenticated()) {
                    showAuthMenu();
                } else {
                    showMainMenu();
                }
            } catch (Exception e) {
                Formatter.printError(e.getMessage());
            }
        }
    }

    private void printWelcome() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("        СИСТЕМА УПРАВЛЕНИЯ ЛИЧНЫМИ ФИНАНСАМИ");
        System.out.println("=".repeat(60));
        System.out.println("Версия 1.0");
        System.out.println("Автор: Студент ООП");
        System.out.println("=".repeat(60) + "\n");
    }

    private void printHelp() {
        Formatter.printSection("СПРАВКА ПО КОМАНДАМ");

        System.out.println("\n--- АУТЕНТИФИКАЦИЯ ---");
        System.out.println("login [имя] [пароль]     - Вход в систему");
        System.out.println("register [имя] [пароль]  - Регистрация");
        System.out.println("logout                   - Выход из аккаунта");
        System.out.println("exit                     - Выход из приложения");

        System.out.println("\n--- УПРАВЛЕНИЕ ФИНАНСАМИ ---");
        System.out.println("add_income [сумма] [категория] [описание] - Добавить доход");
        System.out.println("add_expense [сумма] [категория] [описание] - Добавить расход");
        System.out.println("add_category [название] - Добавить категорию");
        System.out.println("remove_category [название] - Удалить категорию");

        System.out.println("\n--- БЮДЖЕТЫ ---");
        System.out.println("set_budget [категория] [лимит] - Установить бюджет");
        System.out.println("edit_budget [категория] [новый_лимит] - Изменить бюджет");
        System.out.println("remove_budget [категория] - Удалить бюджет");

        System.out.println("\n--- СТАТИСТИКА И ОТЧЕТЫ ---");
        System.out.println("balance                  - Показать баланс");
        System.out.println("stats [начало] [конец]  - Статистика за период (даты в формате дд.мм.гггг)");
        System.out.println("stats_month [мм.гггг]    - Статистика за месяц");
        System.out.println("category_stats [кат1,кат2] - Статистика по категориям");
        System.out.println("transactions            - Показать все транзакции");
        System.out.println("budgets                 - Показать все бюджеты");
        System.out.println("report [начало] [конец] - Полный отчет за период");

        System.out.println("\n--- ЭКСПОРТ/ИМПОРТ ---");
        System.out.println("export [имя_файла]      - Экспорт в CSV");
        System.out.println("import [имя_файла]      - Импорт из CSV");

        System.out.println("\n--- ПРОЧЕЕ ---");
        System.out.println("alerts                  - Показать оповещения");
        System.out.println("transfer [пользователь] [сумма] [описание] - Перевод денег");
        System.out.println("change_password         - Изменить пароль");
        System.out.println("help                    - Показать эту справку");
        System.out.println("clear                   - Очистить экран");
    }

    private void showAuthMenu() {
        System.out.println("\n" + "-".repeat(40));
        System.out.println("1. Вход");
        System.out.println("2. Регистрация");
        System.out.println("3. Выход");
        System.out.println("4. Справка");
        System.out.print("Выберите действие (1-4): ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegister();
                break;
            case "3":
                exit();
                break;
            case "4":
                printHelp();
                break;
            default:
                System.out.println("Неверный выбор. Попробуйте снова.");
        }
    }

    private void showMainMenu() {
        User currentUser = userService.getCurrentUser();
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("Пользователь: %s | Баланс: %s%n",
                currentUser.getUsername(),
                Formatter.formatCurrency(currentUser.getWallet().getBalance()));
        System.out.println("=".repeat(60));

        // Проверяем оповещения
        alertService.checkBudgetAlerts(currentUser.getWallet());
        alertService.checkBalanceAlerts(currentUser.getWallet());
        if (alertService.hasAlerts()) {
            alertService.printAlerts();
        }

        System.out.println("\nОсновные команды:");
        System.out.println("1. Добавить доход      6. Показать бюджеты     11. Экспорт");
        System.out.println("2. Добавить расход     7. Редактировать бюджет 12. Импорт");
        System.out.println("3. Добавить категорию  8. Статистика           13. Перевод");
        System.out.println("4. Установить бюджет   9. Полный отчет         14. Сменить пароль");
        System.out.println("5. Баланс             10. Все транзакции       15. Выход");
        System.out.println("\nДополнительно: stats, category_stats, alerts, clear, help");
        System.out.print("\nВведите команду или номер: ");

        String input = scanner.nextLine().trim();

        // Обработка числового выбора
        if (input.matches("\\d+")) {
            int choice = Integer.parseInt(input);
            handleNumericChoice(choice);
        } else {
            // Обработка текстовой команды
            handleCommand(input);
        }
    }

    private void handleNumericChoice(int choice) {
        try {
            switch (choice) {
                case 1:
                    addIncome();
                    break;
                case 2:
                    addExpense();
                    break;
                case 3:
                    addCategory();
                    break;
                case 4:
                    setBudget();
                    break;
                case 5:
                    showBalance();
                    break;
                case 6:
                    showBudgets();
                    break;
                case 7:
                    editBudget();
                    break;
                case 8:
                    showStatistics();
                    break;
                case 9:
                    generateReport();
                    break;
                case 10:
                    showTransactions();
                    break;
                case 11:
                    exportData();
                    break;
                case 12:
                    importData();
                    break;
                case 13:
                    transferMoney();
                    break;
                case 14:
                    changePassword();
                    break;
                case 15:
                    userService.logout();
                    saveUsers();
                    break;
                default:
                    System.out.println("Неверный номер команды");
            }
        } catch (Exception e) {
            Formatter.printError(e.getMessage());
        }
    }

    private void handleCommand(String input) {
        Map<String, String> parsed = CommandParser.parseCommand(input);
        String command = parsed.get("command");
        String args = parsed.get("args");

        try {
            switch (command) {
                case "login":
                    handleLoginCommand(args);
                    break;
                case "register":
                    handleRegisterCommand(args);
                    break;
                case "logout":
                    userService.logout();
                    saveUsers();
                    break;
                case "exit":
                    exit();
                    break;
                case "add_income":
                    handleAddIncome(args);
                    break;
                case "add_expense":
                    handleAddExpense(args);
                    break;
                case "add_category":
                    handleAddCategory(args);
                    break;
                case "remove_category":
                    handleRemoveCategory(args);
                    break;
                case "set_budget":
                    handleSetBudget(args);
                    break;
                case "edit_budget":
                    handleEditBudget(args);
                    break;
                case "remove_budget":
                    handleRemoveBudget(args);
                    break;
                case "balance":
                    showBalance();
                    break;
                case "stats":
                    handleStats(args);
                    break;
                case "stats_month":
                    handleStatsMonth(args);
                    break;
                case "category_stats":
                    handleCategoryStats(args);
                    break;
                case "transactions":
                    showTransactions();
                    break;
                case "budgets":
                    showBudgets();
                    break;
                case "report":
                    handleReport(args);
                    break;
                case "export":
                    handleExport(args);
                    break;
                case "import":
                    handleImport(args);
                    break;
                case "alerts":
                    alertService.printAlerts();
                    break;
                case "transfer":
                    handleTransfer(args);
                    break;
                case "change_password":
                    changePassword();
                    break;
                case "help":
                    printHelp();
                    break;
                case "clear":
                    clearScreen();
                    break;
                case "":
                    // Пустая команда - ничего не делаем
                    break;
                default:
                    System.out.println("Неизвестная команда. Введите 'help' для справки.");
            }
        } catch (Exception e) {
            Formatter.printError(e.getMessage());
        }
    }

    private void handleLogin() {
        System.out.print("Имя пользователя: ");
        String username = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        userService.login(username, password);
    }

    private void handleLoginCommand(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        if (argList.size() < 2) {
            System.out.println("Использование: login [имя] [пароль]");
            return;
        }

        userService.login(argList.get(0), argList.get(1));
    }

    private void handleRegister() {
        System.out.print("Имя пользователя: ");
        String username = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        System.out.print("Подтвердите пароль: ");
        String confirmPassword = scanner.nextLine().trim();

        userService.register(username, password, confirmPassword);
    }

    private void handleRegisterCommand(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        if (argList.size() < 2) {
            System.out.println("Использование: register [имя] [пароль]");
            return;
        }

        String confirmPassword = argList.size() > 2 ? argList.get(2) : argList.get(1);
        userService.register(argList.get(0), argList.get(1), confirmPassword);
    }

    private void addIncome() {
        try {
            System.out.print("Сумма дохода: ");
            double amount = CommandParser.parseAmount(scanner.nextLine());

            System.out.print("Категория: ");
            String category = scanner.nextLine().trim();

            System.out.print("Описание (необязательно): ");
            String description = scanner.nextLine().trim();

            financeService.addIncome(amount, category, description);
            Formatter.printSuccess("Доход добавлен");
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void handleAddIncome(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        if (argList.size() < 2) {
            System.out.println("Использование: add_income [сумма] [категория] [описание]");
            return;
        }

        try {
            double amount = CommandParser.parseAmount(argList.get(0));
            String category = argList.get(1);
            String description = argList.size() > 2 ? argList.get(2) : "";

            financeService.addIncome(amount, category, description);
            Formatter.printSuccess("Доход добавлен");
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void addExpense() {
        try {
            System.out.print("Сумма расхода: ");
            double amount = CommandParser.parseAmount(scanner.nextLine());

            System.out.print("Категория: ");
            String category = scanner.nextLine().trim();

            System.out.print("Описание (необязательно): ");
            String description = scanner.nextLine().trim();

            financeService.addExpense(amount, category, description);
            Formatter.printSuccess("Расход добавлен");
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void handleAddExpense(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        if (argList.size() < 2) {
            System.out.println("Использование: add_expense [сумма] [категория] [описание]");
            return;
        }

        try {
            double amount = CommandParser.parseAmount(argList.get(0));
            String category = argList.get(1);
            String description = argList.size() > 2 ? argList.get(2) : "";

            financeService.addExpense(amount, category, description);
            Formatter.printSuccess("Расход добавлен");
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void addCategory() {
        System.out.print("Название категории: ");
        String category = scanner.nextLine().trim();

        financeService.addCategory(category);
        Formatter.printSuccess("Категория добавлена");
    }

    private void handleAddCategory(String args) {
        if (args.trim().isEmpty()) {
            System.out.println("Использование: add_category [название]");
            return;
        }

        financeService.addCategory(args.trim());
        Formatter.printSuccess("Категория добавлена");
    }

    private void handleRemoveCategory(String args) {
        if (args.trim().isEmpty()) {
            System.out.println("Использование: remove_category [название]");
            return;
        }

        financeService.removeCategory(args.trim());
        Formatter.printSuccess("Категория удалена");
    }

    private void setBudget() {
        try {
            System.out.print("Категория: ");
            String category = scanner.nextLine().trim();

            System.out.print("Лимит бюджета: ");
            double limit = CommandParser.parseAmount(scanner.nextLine());

            financeService.setBudget(category, limit);
            Formatter.printSuccess("Бюджет установлен");
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void handleSetBudget(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        if (argList.size() < 2) {
            System.out.println("Использование: set_budget [категория] [лимит]");
            return;
        }

        try {
            String category = argList.get(0);
            double limit = CommandParser.parseAmount(argList.get(1));

            financeService.setBudget(category, limit);
            Formatter.printSuccess("Бюджет установлен");
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void editBudget() {
        try {
            System.out.print("Категория: ");
            String category = scanner.nextLine().trim();

            System.out.print("Новый лимит бюджета: ");
            double newLimit = CommandParser.parseAmount(scanner.nextLine());

            financeService.editBudget(category, newLimit);
            Formatter.printSuccess("Бюджет обновлен");
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void handleEditBudget(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        if (argList.size() < 2) {
            System.out.println("Использование: edit_budget [категория] [новый_лимит]");
            return;
        }

        try {
            String category = argList.get(0);
            double newLimit = CommandParser.parseAmount(argList.get(1));

            financeService.editBudget(category, newLimit);
            Formatter.printSuccess("Бюджет обновлен");
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void handleRemoveBudget(String args) {
        if (args.trim().isEmpty()) {
            System.out.println("Использование: remove_budget [категория]");
            return;
        }

        financeService.removeBudget(args.trim());
        Formatter.printSuccess("Бюджет удален");
    }

    private void showBalance() {
        User user = userService.getCurrentUser();
        Wallet wallet = user.getWallet();

        Formatter.printSection("БАЛАНС");
        System.out.printf("Текущий баланс: %s%n", Formatter.formatCurrency(wallet.getBalance()));
        System.out.printf("Общий доход: %s%n", Formatter.formatCurrency(wallet.getTotalIncome()));
        System.out.printf("Общие расходы: %s%n", Formatter.formatCurrency(wallet.getTotalExpenses()));

        if (wallet.getTotalIncome() > 0) {
            double savingsRate = (wallet.getTotalIncome() - wallet.getTotalExpenses()) /
                    wallet.getTotalIncome() * 100;
            System.out.printf("Норма сбережений: %.1f%%%n", savingsRate);
        }
    }

    private void showBudgets() {
        User user = userService.getCurrentUser();
        Map<String, Budget> budgets = user.getWallet().getBudgets();

        if (budgets.isEmpty()) {
            System.out.println("Бюджеты не установлены");
            return;
        }

        Formatter.printSection("БЮДЖЕТЫ");

        String[] headers = {"Категория", "Лимит", "Потрачено", "Осталось", "Статус"};
        String[][] data = new String[budgets.size()][5];

        int i = 0;
        for (Budget budget : budgets.values()) {
            String status = budget.isExceeded() ? "ПРЕВЫШЕН" :
                    budget.isWarning() ? "ВНИМАНИЕ" : "OK";

            data[i][0] = budget.getCategory();
            data[i][1] = Formatter.formatCurrency(budget.getLimit());
            data[i][2] = Formatter.formatCurrency(budget.getCurrentSpending());
            data[i][3] = Formatter.formatCurrency(budget.getRemaining());
            data[i][4] = status;
            i++;
        }

        Formatter.printTable(headers, data);
    }

    private void showStatistics() {
        System.out.print("Начальная дата (дд.мм.гггг) или Enter для начала месяца: ");
        String startStr = scanner.nextLine().trim();

        System.out.print("Конечная дата (дд.мм.гггг) или Enter для сегодня: ");
        String endStr = scanner.nextLine().trim();

        LocalDate startDate;
        LocalDate endDate;

        if (startStr.isEmpty()) {
            startDate = LocalDate.now().withDayOfMonth(1);
        } else {
            startDate = CommandParser.parseDate(startStr);
        }

        if (endStr.isEmpty()) {
            endDate = LocalDate.now();
        } else {
            endDate = CommandParser.parseDate(endStr);
        }

        Map<String, Object> stats = financeService.getStatistics(startDate, endDate);

        Formatter.printSection("СТАТИСТИКА ЗА ПЕРИОД");
        System.out.printf("Период: %s - %s%n",
                Formatter.formatDate(startDate),
                Formatter.formatDate(endDate));

        System.out.printf("\nОбщий доход: %s%n",
                Formatter.formatCurrency((double) stats.get("totalIncome")));
        System.out.printf("Общие расходы: %s%n",
                Formatter.formatCurrency((double) stats.get("totalExpenses")));
        System.out.printf("Баланс: %s%n",
                Formatter.formatCurrency((double) stats.get("balance")));

        @SuppressWarnings("unchecked")
        Map<String, Double> incomeByCategory = (Map<String, Double>) stats.get("incomeByCategory");
        if (!incomeByCategory.isEmpty()) {
            System.out.println("\nДоходы по категориям:");
            incomeByCategory.forEach((category, amount) ->
                    System.out.printf("  %-20s %s%n", category + ":", Formatter.formatCurrency(amount)));
        }

        @SuppressWarnings("unchecked")
        Map<String, Double> expensesByCategory = (Map<String, Double>) stats.get("expensesByCategory");
        if (!expensesByCategory.isEmpty()) {
            System.out.println("\nРасходы по категориям:");
            expensesByCategory.forEach((category, amount) ->
                    System.out.printf("  %-20s %s%n", category + ":", Formatter.formatCurrency(amount)));
        }
    }

    private void handleStats(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        LocalDate startDate;
        LocalDate endDate;

        if (argList.size() < 2) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = LocalDate.now();
        } else {
            startDate = CommandParser.parseDate(argList.get(0));
            endDate = CommandParser.parseDate(argList.get(1));
        }

        Map<String, Object> stats = financeService.getStatistics(startDate, endDate);

        Formatter.printSection("СТАТИСТИКА ЗА ПЕРИОД");
        System.out.printf("Период: %s - %s%n",
                Formatter.formatDate(startDate),
                Formatter.formatDate(endDate));

        System.out.printf("\nОбщий доход: %s%n",
                Formatter.formatCurrency((double) stats.get("totalIncome")));
        System.out.printf("Общие расходы: %s%n",
                Formatter.formatCurrency((double) stats.get("totalExpenses")));
        System.out.printf("Баланс: %s%n",
                Formatter.formatCurrency((double) stats.get("balance")));
    }

    private void handleStatsMonth(String args) {
        YearMonth month;

        if (args.trim().isEmpty()) {
            month = YearMonth.now();
        } else {
            try {
                String[] parts = args.split("\\.");
                int monthNum = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                month = YearMonth.of(year, monthNum);
            } catch (Exception e) {
                throw new ValidationException("Неверный формат месяца. Используйте мм.гггг");
            }
        }

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        Map<String, Object> stats = financeService.getStatistics(startDate, endDate);

        Formatter.printSection("СТАТИСТИКА ЗА МЕСЯЦ");
        System.out.printf("Месяц: %s%n", month.toString());

        System.out.printf("\nДоход за месяц: %s%n",
                Formatter.formatCurrency((double) stats.get("totalIncome")));
        System.out.printf("Расходы за месяц: %s%n",
                Formatter.formatCurrency((double) stats.get("totalExpenses")));
        System.out.printf("Баланс за месяц: %s%n",
                Formatter.formatCurrency((double) stats.get("balance")));
    }

    private void handleCategoryStats(String args) {
        if (args.trim().isEmpty()) {
            System.out.println("Использование: category_stats [категория1,категория2,...]");
            return;
        }

        List<String> categories = CommandParser.parseCategories(args);
        Map<String, Map<String, Object>> stats = financeService.getCategoryStatistics(categories);

        Formatter.printSection("СТАТИСТИКА ПО КАТЕГОРИЯМ");

        for (Map.Entry<String, Map<String, Object>> entry : stats.entrySet()) {
            String category = entry.getKey();
            Map<String, Object> categoryStats = entry.getValue();

            System.out.printf("\nКатегория: %s%n", category);
            System.out.printf("  Доходы: %s%n",
                    Formatter.formatCurrency((Double) categoryStats.get("income")));
            System.out.printf("  Расходы: %s%n",
                    Formatter.formatCurrency((Double) categoryStats.get("expenses")));
            System.out.printf("  Баланс: %s%n",
                    Formatter.formatCurrency((Double) categoryStats.get("balance")));

            if (categoryStats.containsKey("budgetLimit")) {
                System.out.printf("  Бюджет: %s (потрачено: %s, осталось: %s)%n",
                        Formatter.formatCurrency((Double) categoryStats.get("budgetLimit")),
                        Formatter.formatCurrency((Double) categoryStats.get("budgetSpent")),
                        Formatter.formatCurrency((Double) categoryStats.get("budgetRemaining")));

                // Проверяем, превышен ли бюджет
                if ((Boolean) categoryStats.get("budgetExceeded")) {
                    System.out.println("  ⚠ ВНИМАНИЕ: Бюджет превышен!");
                }
            }
        }
    }

    private void showTransactions() {
        User user = userService.getCurrentUser();
        List<Transaction> transactions = user.getWallet().getTransactions();

        if (transactions.isEmpty()) {
            System.out.println("Транзакций нет");
            return;
        }

        Formatter.printSection("ВСЕ ТРАНЗАКЦИИ");

        // Сортируем по дате (сначала новые)
        transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        String[] headers = {"Дата", "Тип", "Категория", "Сумма", "Описание"};
        String[][] data = new String[transactions.size()][5];

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            data[i][0] = t.getFormattedDate();
            data[i][1] = t.getType().getDescription();
            data[i][2] = t.getCategory();
            data[i][3] = Formatter.formatCurrency(t.getAmount());
            data[i][4] = t.getDescription();

            if (data[i][4].length() > 30) {
                data[i][4] = data[i][4].substring(0, 27) + "...";
            }
        }

        Formatter.printTable(headers, data);
        System.out.printf("Всего транзакций: %d%n", transactions.size());
    }

    private void generateReport() {
        System.out.print("Начальная дата отчета (дд.мм.гггг) или Enter для начала года: ");
        String startStr = scanner.nextLine().trim();

        System.out.print("Конечная дата отчета (дд.мм.гггг) или Enter для сегодня: ");
        String endStr = scanner.nextLine().trim();

        LocalDate startDate;
        LocalDate endDate;

        if (startStr.isEmpty()) {
            startDate = LocalDate.now().withDayOfYear(1);
        } else {
            startDate = CommandParser.parseDate(startStr);
        }

        if (endStr.isEmpty()) {
            endDate = LocalDate.now();
        } else {
            endDate = CommandParser.parseDate(endStr);
        }

        User user = userService.getCurrentUser();
        Map<String, Object> report = statisticsService.generateFullReport(
                user.getWallet(), startDate, endDate);

        statisticsService.printReport(report);
    }

    private void handleReport(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        LocalDate startDate;
        LocalDate endDate;

        if (argList.size() < 2) {
            startDate = LocalDate.now().withDayOfYear(1);
            endDate = LocalDate.now();
        } else {
            startDate = CommandParser.parseDate(argList.get(0));
            endDate = CommandParser.parseDate(argList.get(1));
        }

        User user = userService.getCurrentUser();
        Map<String, Object> report = statisticsService.generateFullReport(
                user.getWallet(), startDate, endDate);

        statisticsService.printReport(report);
    }

    private void exportData() {
        System.out.print("Имя файла для экспорта (без .csv): ");
        String filename = scanner.nextLine().trim();

        if (filename.isEmpty()) {
            filename = userService.getCurrentUser().getUsername() + "_export_" +
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        }

        fileStorage.exportToCSV(userService.getCurrentUser(), filename);
        Formatter.printSuccess("Данные экспортированы");
    }

    private void handleExport(String args) {
        String filename = args.trim();

        if (filename.isEmpty()) {
            filename = userService.getCurrentUser().getUsername() + "_export_" +
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        }

        fileStorage.exportToCSV(userService.getCurrentUser(), filename);
        Formatter.printSuccess("Данные экспортированы");
    }

    private void importData() {
        System.out.print("Имя файла для импорта (без .csv): ");
        String filename = scanner.nextLine().trim();

        if (filename.isEmpty()) {
            System.out.println("Не указано имя файла");
            return;
        }

        if (!CommandParser.confirm("Импортировать данные? Существующие данные не будут удалены.", scanner)) {
            System.out.println("Импорт отменен");
            return;
        }

        fileStorage.importFromCSV(userService.getCurrentUser(), filename);
        Formatter.printSuccess("Данные импортированы");
    }

    private void handleImport(String args) {
        if (args.trim().isEmpty()) {
            System.out.println("Использование: import [имя_файла]");
            return;
        }

        if (!CommandParser.confirm("Импортировать данные? Существующие данные не будут удалены.", scanner)) {
            System.out.println("Импорт отменен");
            return;
        }

        fileStorage.importFromCSV(userService.getCurrentUser(), args.trim());
        Formatter.printSuccess("Данные импортированы");
    }

    private void transferMoney() {
        System.out.print("Имя пользователя-получателя: ");
        String toUsername = scanner.nextLine().trim();

        System.out.print("Сумма перевода: ");
        double amount;
        try {
            amount = CommandParser.parseAmount(scanner.nextLine());
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }

        System.out.print("Описание перевода: ");
        String description = scanner.nextLine().trim();

        if (!CommandParser.confirm("Подтвердить перевод?", scanner)) {
            System.out.println("Перевод отменен");
            return;
        }

        financeService.transferMoney(toUsername, amount, description, userRepository);
        Formatter.printSuccess("Перевод выполнен");
    }

    private void handleTransfer(String args) {
        List<String> argList = CommandParser.parseArguments(args);
        if (argList.size() < 2) {
            System.out.println("Использование: transfer [пользователь] [сумма] [описание]");
            return;
        }

        String toUsername = argList.get(0);
        double amount;
        try {
            amount = CommandParser.parseAmount(argList.get(1));
        } catch (Exception e) {
            throw new ValidationException(e.getMessage());
        }

        String description = argList.size() > 2 ? argList.get(2) : "";

        if (!CommandParser.confirm("Подтвердить перевод?", scanner)) {
            System.out.println("Перевод отменен");
            return;
        }

        financeService.transferMoney(toUsername, amount, description, userRepository);
        Formatter.printSuccess("Перевод выполнен");
    }

    private void changePassword() {
        System.out.print("Текущий пароль: ");
        String oldPassword = scanner.nextLine().trim();

        System.out.print("Новый пароль: ");
        String newPassword = scanner.nextLine().trim();

        System.out.print("Подтвердите новый пароль: ");
        String confirmPassword = scanner.nextLine().trim();

        userService.changePassword(oldPassword, newPassword, confirmPassword);
        Formatter.printSuccess("Пароль изменен");
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void exit() {
        saveUsers();
        System.out.println("\nСпасибо за использование системы управления финансами!");
        System.out.println("До свидания!");
        scanner.close();
        System.exit(0);
    }
}
