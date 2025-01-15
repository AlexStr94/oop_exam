package alex.strunskiy.database;

import alex.strunskiy.dataclasses.Category;
import alex.strunskiy.dataclasses.Transaction;

import java.sql.*;
import java.util.ArrayList;

public class Database {
    private final String databaseUrl;

    public Database(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public boolean initDatabase() {
        try (Connection conn = DriverManager.getConnection(this.databaseUrl);
             Statement st = conn.createStatement();) {
            st.execute("CREATE TABLE IF NOT EXISTS public.users (\n" +
                       "\tid int GENERATED ALWAYS AS IDENTITY NOT NULL,\n" +
                       "\tlogin varchar NOT NULL,\n" +
                       "\t\"password\" varchar NOT NULL,\n" +
                       "\tCONSTRAINT users_pk PRIMARY KEY (id),\n" +
                       "\tCONSTRAINT users_unique UNIQUE (\"login\")" +
                       ");");

            st.execute("CREATE TABLE IF NOT EXISTS public.account (\n" +
                       "\tid int GENERATED ALWAYS AS IDENTITY NOT NULL,\n" +
                       "\t\"user\" int NOT NULL,\n" +
                       "\t\"name\" varchar NOT NULL,\n" +
                       "\tCONSTRAINT account_pk PRIMARY KEY (id),\n" +
                       "\tCONSTRAINT fk_user FOREIGN KEY (\"user\") REFERENCES public.users(id),\n" +
                       "\tCONSTRAINT account_unique UNIQUE (\"name\", \"user\")" +
                       ");");
            st.execute("CREATE TABLE IF NOT EXISTS public.transaction_category (\n" +
                       "  id int GENERATED ALWAYS AS IDENTITY NOT NULL,\n" +
                       "  \"name\" varchar NOT NULL,\n" +
                       "  income bool NOT NULL,\n" +
                       "  \"user\" int NOT NULL,\n" +
                       "  budget int4 NULL,\n" +
                       "  CONSTRAINT transaction_category_pk PRIMARY KEY (id),\n" +
                       "  CONSTRAINT fk_user FOREIGN KEY (\"user\") REFERENCES public.users(id),\n" +
                       "  CONSTRAINT unique_transaction_category_user_key UNIQUE (name, income, \"user\")" +
                       ");");
            st.execute("CREATE TABLE IF NOT EXISTS public.\"transaction\" (\n" +
                       "\tid int GENERATED ALWAYS AS IDENTITY NOT NULL,\n" +
                       "\taccount int NOT NULL,\n" +
                       "\tcategory int NOT NULL,\n" +
                       "\tamount int NOT NULL,\n" +
                       "\tCONSTRAINT transaction_pk PRIMARY KEY (id),\n" +
                       "\tCONSTRAINT fk_category FOREIGN KEY (category) REFERENCES public.transaction_category(id)" +
                       ");");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public int createUser(String login, String password){
        String sql = """
                INSERT INTO public.users
                (login, password)
                VALUES(?, ?) RETURNING id;""";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return 0;
            }

        } catch (SQLException e) {
            return 0;
        }
    }

    public int getUser(String login, String password){
        String sql = "SELECT id FROM public.users WHERE login = ? AND \"password\" = ?;";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);

            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            } else {
                return 0;
            }

        } catch (SQLException e) {
            return 0;
        }
    }

    public int createAccount(int userId, String name) {
        String sql = "INSERT INTO public.account\n" +
                     "(\"user\", \"name\")\n" +
                     "VALUES(?, ?) RETURNING id;";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, name);

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return 0;
            }

        } catch (SQLException e) {
            return 0;
        }
    }

    public int getAccount(int userId, String name) {
        String sql = "SELECT id FROM public.account WHERE \"user\" = ? AND \"name\" = ?;";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, name);

            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            } else {
                return 0;
            }

        } catch (SQLException e) {
            return 0;
        }
    }

    public int createCategory(int userId, String name, boolean income, int budget) {
        String sql = "INSERT INTO public.transaction_category\n" +
                   "(\"name\", income, \"user\", budget)\n" +
                   "VALUES(?, ?, ?, ?) RETURNING id;";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, name);
            preparedStatement.setBoolean(2, income);
            preparedStatement.setInt(3, userId);

            if (!income) {
                preparedStatement.setInt(4, budget);
            } else {
                preparedStatement.setNull(4, Types.NULL);
            }

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    public int getCategory(int userId, String name) {
        String sql = "SELECT id FROM public.transaction_category WHERE \"user\" = ? AND \"name\" = ?;";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, name);

            ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                return result.getInt(1);
            } else {
                return 0;
            }

        } catch (SQLException e) {
            return 0;
        }
    }

    public int createTransaction(int accountId, int categoryId, int amount) {
        String sql = "INSERT INTO public.\"transaction\"\n" +
                     "(account, category, amount)\n" +
                     "VALUES(?, ?, ?) RETURNING id;";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, accountId);
            preparedStatement.setInt(2, categoryId);
            preparedStatement.setInt(3, amount);

            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    public ArrayList<Category> getCategories(int userId, boolean income){
        ArrayList<Category> categories = new ArrayList<>();
        String sql = "select tc.\"name\" as category, sum(t.amount), tc.budget from \"transaction\" t \n" +
                     "join transaction_category tc on t.category = tc.id \n" +
                     "join account a on a.id = t.account\n" +
                     "where a.\"user\" = ? and tc.income = ?\n" +
                     "group by tc.\"name\", tc.budget";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setBoolean(2, income);

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                Category category = new Category(result.getString(1), result.getInt(2), result.getInt(3));
                categories.add(category);
            }

        } catch (SQLException _) {}
        return categories;
    }

    public ArrayList<Transaction> getCategoryTransaction(int categoryId) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        String sql = "select a.\"name\" as account, tc.\"name\" as category, t.amount as amount, tc.income as income from transaction t\n" +
                     "join transaction_category tc on t.category = tc.id  \n" +
                     "join account a on a.id = t.account \n" +
                     "where tc.id = ?";
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, categoryId);

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                Transaction transaction = new Transaction(
                        result.getString(1),
                        result.getString(2),
                        result.getBoolean(4),
                        result.getInt(3)
                );
                transactions.add(transaction);
            }

        } catch (SQLException _) {}
        return transactions;
    }
}