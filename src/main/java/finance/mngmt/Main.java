package finance.mngmt;

import finance.mngmt.cli.CLI;

public class Main {
    public static void main(String[] args) {
        try {
            CLI cli = new CLI();
            cli.start();
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}