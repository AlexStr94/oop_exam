package alex.strunskiy.dataclasses;

public class Category {
    String name;
    int sum;
    int budget;

    public Category(String name, int sum, int budget) {
        this.name = name;
        this.sum = sum;
        this.budget = budget;
    }

    public String getName() {
        return name;
    }

    public int getSum() {
        return sum;
    }

    public int getBudget() {
        return budget;
    }
}
