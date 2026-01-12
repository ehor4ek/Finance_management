package finance.mngmt.repository;

import finance.mngmt.model.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileStorage {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.ser";
    private static final String CSV_EXPORT_DIR = DATA_DIR + "/exports";

    public FileStorage() {
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(CSV_EXPORT_DIR));
        } catch (IOException e) {
            System.err.println("Ошибка при создании директорий: " + e.getMessage());
        }
    }

    public void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            System.out.println("Данные пользователей сохранены");
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении пользователей: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            Map<String, User> users = (Map<String, User>) ois.readObject();
            System.out.println("Данные пользователей загружены: " + users.size() + " пользователей");
            return users;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка при загрузке пользователей: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public void exportToCSV(User user, String filename) {
        if (!filename.toLowerCase().endsWith(".csv")) {
            filename += ".csv";
        }

        String filepath = CSV_EXPORT_DIR + "/" + filename;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
            // Записываем заголовок
            writer.println("Тип;Дата;Категория;Сумма;Описание");

            // Записываем транзакции
            for (Transaction transaction : user.getWallet().getTransactions()) {
                writer.printf("%s;%s;%s;%.2f;%s%n",
                        transaction.getType().getDescription(),
                        transaction.getFormattedDate(),
                        transaction.getCategory(),
                        transaction.getAmount(),
                        transaction.getDescription()
                );
            }

            // Записываем бюджеты
            writer.println("\nБюджеты:");
            writer.println("Категория;Лимит;Потрачено;Остаток");
            for (Budget budget : user.getWallet().getBudgets().values()) {
                writer.printf("%s;%.2f;%.2f;%.2f%n",
                        budget.getCategory(),
                        budget.getLimit(),
                        budget.getCurrentSpending(),
                        budget.getRemaining()
                );
            }

            System.out.println("Данные экспортированы в: " + filepath);
        } catch (IOException e) {
            System.err.println("Ошибка при экспорте в CSV: " + e.getMessage());
        }
    }

    public void importFromCSV(User user, String filename) {
        String filepath = CSV_EXPORT_DIR + "/" + filename;
        if (!filepath.toLowerCase().endsWith(".csv")) {
            filepath += ".csv";
        }

        File file = new File(filepath);
        if (!file.exists()) {
            System.err.println("Файл не найден: " + filepath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean readingBudgets = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (line.equals("Бюджеты:")) {
                    readingBudgets = true;
                    reader.readLine(); // Пропускаем заголовок
                    continue;
                }

                if (!readingBudgets) {
                    // Читаем транзакции
                    if (line.equals("Тип;Дата;Категория;Сумма;Описание")) {
                        continue;
                    }

                    String[] parts = line.split(";");
                    if (parts.length >= 5) {
                        try {
                            TransactionType type = parts[0].equals("Доход") ?
                                    TransactionType.INCOME : TransactionType.EXPENSE;

                            LocalDateTime date = LocalDateTime.parse(parts[1],
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                            String category = parts[2];
                            double amount = Double.parseDouble(parts[3].replace(',', '.'));
                            String description = parts[4];

                            Transaction transaction = new Transaction(
                                    UUID.randomUUID().toString(),
                                    amount, type, category, date, description
                            );
                            user.getWallet().addTransaction(transaction);
                        } catch (Exception e) {
                            System.err.println("Ошибка при чтении строки: " + line);
                        }
                    }
                } else {
                    // Читаем бюджеты
                    if (line.equals("Категория;Лимит;Потрачено;Остаток")) {
                        continue;
                    }

                    String[] parts = line.split(";");
                    if (parts.length >= 4) {
                        try {
                            String category = parts[0];
                            double limit = Double.parseDouble(parts[1].replace(',', '.'));
                            double spent = Double.parseDouble(parts[2].replace(',', '.'));

                            Budget budget = new Budget(category, limit);
                            budget.setCurrentSpending(spent);
                            user.getWallet().getBudgets().put(category, budget);
                        } catch (Exception e) {
                            System.err.println("Ошибка при чтении бюджета: " + line);
                        }
                    }
                }
            }

            System.out.println("Данные импортированы из: " + filepath);
        } catch (IOException e) {
            System.err.println("Ошибка при импорте из CSV: " + e.getMessage());
        }
    }
}