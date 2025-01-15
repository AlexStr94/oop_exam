package alex.strunskiy.dataclasses;

public class Transaction {
    private final String account;
    private final String category;
    private final boolean income;
    private final int amount;

    public Transaction(String account, String category, boolean income, int amount) {
        this.account = account;
        this.category = category;
        this.income = income;
        this.amount = amount;
    }

    public String getAccount() {
        return account;
    }

    public String getCategory() {
        return category;
    }

    public boolean isIncome() {
        return income;
    }

    public int getAmount() {
        return amount;
    }
}
