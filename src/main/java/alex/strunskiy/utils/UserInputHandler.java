package alex.strunskiy.utils;

import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInputHandler {
    private static Scanner SCANNER;

    public UserInputHandler() {
        SCANNER = new Scanner(System.in);
    }

    public String getString() {
        return SCANNER.nextLine();
    }

    private UUID getUUID() {
        while (true) {
            String input = SCANNER.nextLine();
            try {
                return UUID.fromString(input);
            } catch (IllegalArgumentException _) {
                System.out.println("Введенное значение не соответствует формату UUID. Попробуйте еще раз.");
            }
        }
    }

    private int getInt() {
        while (true) {
            if (SCANNER.hasNextInt()) {
                return SCANNER.nextInt();
            } else {
                System.out.println("Введенное значение не является целочисленным. Попробуйте еще раз.");
            }
        }
    }

    private boolean checkString(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches();
    }


    public String getCommand() {
        return getString();
    }

    public String getLogin() {
        return  getString();
    }

    public String getPassword() {
        return  getString();
    }

    public boolean getIsIncome() {
        while (true) {
            String input = getString();
            if (checkString(input, "[+]")) {
                return true;
            } else if (checkString(input, "[-]")) {
                return false;
            } else {
                System.out.println("Вы ввели неверное значение. Введите '+' или '-'");
            }
        }
    }

    public int getCategoryTransaction() {
        return getInt();
    }

    public int getTransactionAmount() {
        return getInt();
    }

}
