package finance.mngmt.cli;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formatter {
    private static final NumberFormat CURRENCY_FORMATTER;
    private static final DateTimeFormatter DATE_FORMATTER;

    static {
        CURRENCY_FORMATTER = NumberFormat.getNumberInstance(Locale.US);
        CURRENCY_FORMATTER.setMinimumFractionDigits(2);
        CURRENCY_FORMATTER.setMaximumFractionDigits(2);

        DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    }

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMATTER.format(amount);
    }

    public static String formatDate(java.time.LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static void printTable(String[] headers, String[][] data) {
        // Вычисляем максимальную ширину для каждого столбца
        int[] columnWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] != null && row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }

        // Печатаем верхнюю границу
        printLine(columnWidths);

        // Печатаем заголовки
        printRow(headers, columnWidths);

        // Печатаем разделитель
        printLine(columnWidths);

        // Печатаем данные
        for (String[] row : data) {
            printRow(row, columnWidths);
        }

        // Печатаем нижнюю границу
        printLine(columnWidths);
    }

    private static void printLine(int[] widths) {
        System.out.print("+");
        for (int width : widths) {
            System.out.print("-".repeat(width + 2) + "+");
        }
        System.out.println();
    }

    private static void printRow(String[] cells, int[] widths) {
        System.out.print("|");
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i] != null ? cells[i] : "";
            System.out.printf(" %-" + widths[i] + "s |", cell);
        }
        System.out.println();
    }

    public static void printSection(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(title);
        System.out.println("=".repeat(60));
    }

    public static void printSuccess(String message) {
        System.out.println("✓ " + message);
    }

    public static void printError(String message) {
        System.out.println("✗ Ошибка: " + message);
    }

    public static void printWarning(String message) {
        System.out.println("⚠ " + message);
    }

    public static void printInfo(String message) {
        System.out.println("ℹ " + message);
    }
}
