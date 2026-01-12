package finance.mngmt.cli;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CommandParser {

    public static Map<String, String> parseCommand(String input) {
        Map<String, String> result = new HashMap<>();

        if (input == null || input.trim().isEmpty()) {
            result.put("command", "");
            return result;
        }

        String[] parts = input.trim().split("\\s+", 2);
        result.put("command", parts[0].toLowerCase());

        if (parts.length > 1) {
            result.put("args", parts[1]);
        } else {
            result.put("args", "");
        }

        return result;
    }

    public static List<String> parseArguments(String argsString) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : argsString.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            args.add(current.toString());
        }

        return args;
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Неверный формат даты. Используйте dd.MM.yyyy или yyyy-MM-dd");
            }
        }
    }

    public static double parseAmount(String amountStr) {
        try {
            // Заменяем запятую на точку для корректного парсинга
            amountStr = amountStr.replace(',', '.');
            double amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                throw new IllegalArgumentException("Сумма должна быть положительной");
            }

            // Округляем до 2 знаков после запятой
            return Math.round(amount * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверный формат суммы");
        }
    }

    public static List<String> parseCategories(String categoriesStr) {
        return Arrays.asList(categoriesStr.split("[,\\s]+"));
    }

    public static Map<String, String> parseKeyValuePairs(String input) {
        Map<String, String> result = new HashMap<>();

        for (String pair : input.split("\\s+")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return result;
    }

    public static boolean confirm(String message, Scanner scanner) {
        System.out.print(message + " (да/нет): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("да") || response.equals("д") || response.equals("y") || response.equals("yes");
    }
}
