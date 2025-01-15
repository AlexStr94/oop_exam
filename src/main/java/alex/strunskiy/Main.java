package alex.strunskiy;

import alex.strunskiy.database.Database;
import alex.strunskiy.dataclasses.Category;
import alex.strunskiy.dataclasses.Transaction;
import alex.strunskiy.utils.UserInputHandler;

import java.util.ArrayList;

public class Main {
    private static final UserInputHandler userInputHandler = new UserInputHandler();
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres";
    private static final Database DATABASE = new Database(POSTGRES_URL);
    private static int currentUserId = 0;

    public static void main(String[] args) {
        if (!DATABASE.initDatabase()) {
            System.out.println("Не удалось инициализировать базу данных.");
            System.exit(0);
        }

        System.out.println("""
                Вас приветствует финансовый помощник. Основные команды:
                sign in - создать пользователя;
                login - авторизироваться;
                add account - добавить кошелек;
                add category - добавить категорию;
                add transaction - добавить транзакцию;
                get category - информация по отдельной категории;
                get results - итоги по доходам/расходам;
                exit - выйти из программы;
                """);

        while (true) {
            String command = userInputHandler.getCommand();

            switch (command) {
                case "sign in" -> createUser();
                case "login" -> login();
                case "add account" -> addAccount();
                case "add category" -> addCategory();
                case "add transaction" -> addTransaction();
                case "get category" -> getCategoryResults();
                case "get results" -> getResults();
                case "exit" -> System.exit(0);
            }
        }
    }

    private static void createUser(){
        System.out.println("Введите логин пользователя:");
        String login = userInputHandler.getLogin();
        System.out.println("Введите пароль");
        String password = userInputHandler.getPassword();
        int userId = DATABASE.createUser(login, password);
        if (userId != 0) {
            System.out.println("Пользователь успешно создан. Вы авторизованы как " + login);
            currentUserId = userId;
        } else {
            System.out.println("Возникла ошибка при создании пользователя.");
        }
    }

    private static void login(){
        System.out.println("Введите логин пользователя:");
        String login = userInputHandler.getLogin();
        System.out.println("Введите пароль");
        String password = userInputHandler.getPassword();
        int userId = DATABASE.getUser(login, password);
        if (userId != 0) {
            System.out.println("Вы успешно авторизовались как " + login);
            currentUserId = userId;
        } else {
            System.out.println("Возникла ошибка при авторизации пользователя.");
        }
    }

    private static void addAccount() {
        System.out.println("Введите название кошелька");
        String name = userInputHandler.getString();
        int accountId = DATABASE.createAccount(currentUserId, name);
        if (accountId != 0) {
            System.out.println("Вы успешно создали кошелек " + name + " c id " + accountId);
        } else {
            System.out.println("Возникла ошибка при создании кошелька.");
        }
    }

    private static void addCategory() {
        System.out.println("Введите название категории");
        String name = userInputHandler.getString();
        System.out.println("Категория относится к доходам или расходам? Введите '+' или '-'");
        boolean income = userInputHandler.getIsIncome();
        int budget = 0;
        if (!income) {
            System.out.println("Введите бюджет категории");
            budget = userInputHandler.getCategoryTransaction();
        }
        int categoryId = DATABASE.createCategory(currentUserId, name, income, budget);
        if (categoryId != 0) {
            System.out.println("Вы успешно создали категорию " + name + " c id " + categoryId);
        } else {
            System.out.println("Возникла ошибка при создании категории транзакций.");
        }
    }

    private static void addTransaction() {
        System.out.println("Введите название кошелька");
        String accountName = userInputHandler.getString();
        int accountId = DATABASE.getAccount(currentUserId, accountName);
        if (accountId == 0) {
            System.out.println("Не нашли кошелек, добавление транзакции отменено.");
            return;
        }

        System.out.println("Введите название категории");
        String categoryName = userInputHandler.getString();
        int categoryId = DATABASE.getCategory(currentUserId, categoryName);
        if (categoryId == 0) {
            System.out.println("Не нашли категории, добавление транзакции отменено.");
            return;
        }

        System.out.println("Введите размер транзакции");
        int amount = userInputHandler.getTransactionAmount();
        int transactionId = DATABASE.createTransaction(accountId, categoryId, amount);
        if (transactionId != 0) {
            System.out.println("Добавлена тразакция " +categoryName + ": " + amount + " рублей.");
        } else {
            System.out.println("Возникла ошибка при создании транзакций.");
        }
    }

    private static void getCategoryResults() {
        System.out.println("Введите название категории");
        String categoryName = userInputHandler.getString();
        int categoryId = DATABASE.getCategory(currentUserId, categoryName);
        if (categoryId == 0) {
            System.out.println("Не нашли категорию.");
            return;
        }
        ArrayList<Transaction> transactions = DATABASE.getCategoryTransaction(categoryId);
        for (Transaction transaction : transactions) {
            System.out.println("Кошелек: " + transaction.getAccount() + " . Сумма: " + transaction.getAmount());
        }
    }

    private static void getResults() {
        ArrayList<Category> incomes = DATABASE.getCategories(currentUserId, true);
        ArrayList<Category> expenses = DATABASE.getCategories(currentUserId, false);

        System.out.println("Доходы:");
        int incomes_sum = 0;
        for (Category income : incomes){
            incomes_sum += income.getSum();
            System.out.println(income.getName() + ": " + income.getSum() + " рублей.");
        }
        System.out.println("Итого: " + incomes_sum);

        System.out.println("Расходы:");
        int expenses_sum = 0;
        for (Category expense : expenses){
            expenses_sum += expense.getSum();
            int leftBudget = expense.getBudget() - expense.getSum();
            System.out.println(expense.getName() + ": " + expense.getSum() + " рублей, оставшейся бюджет: " + leftBudget + " рублей");
        }
        System.out.println("Итого: " + expenses_sum);
    }
}