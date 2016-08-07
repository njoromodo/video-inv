package edu.sdsu.its.video_inv;

import edu.sdsu.its.video_inv.API.Quote;
import edu.sdsu.its.video_inv.Models.*;
import org.apache.log4j.Logger;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Communicate with Inventory DB
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class DB {
    private static final String db_url = Param.getParam("db-url");
    private static final String db_user = Param.getParam("db-user");
    private static final String db_password = Param.getParam("db-password");

    private static final Logger LOGGER = Logger.getLogger(DB.class);
    private static final StrongPasswordEncryptor PASSWORD_ENCRYPTOR = new StrongPasswordEncryptor();


    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(db_url, db_user, db_password);
        } catch (Exception e) {
            LOGGER.fatal("Problem Initializing DB Connection", e);
        }

        return connection;
    }

    /**
     * Execute a SQL Statement
     *
     * @param sql {@link String} SQL Statement to Execute
     */
    private static void executeStatement(final String sql) {
        new Thread() {
            @Override
            public void run() {
                Statement statement = null;
                Connection connection = getConnection();

                try {
                    statement = connection.createStatement();
                    LOGGER.info(String.format("Executing SQL Statement - \"%s\"", sql));
                    statement.execute(sql);

                } catch (SQLException e) {
                    LOGGER.error("Problem Executing Statement \"" + sql + "\"", e);
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.warn("Problem Closing Statement", e);
                        }
                    }
                }
            }
        }.start();
    }


    // ====================== Users ======================

    /**
     * Get an Array of Users who match the specified criteria.
     * id is the Internal Identifier
     * pub_id is the Public Identifier
     *
     * @param restriction {@link String} Restriction on the Search, as a WHERE SQL Statement, the WHERE is already included
     * @return {@link User[]} Array of User Objects
     */
    public static User[] getUser(String restriction) {
        Connection connection = getConnection();
        Statement statement = null;
        List<User> users = new ArrayList<>();

        try {
            statement = connection.createStatement();
            restriction = restriction == null ? "" : " WHERE " + restriction;
            final String sql = "SELECT * FROM users " + restriction + " ORDER BY last_name ASC;";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                User user = new User(resultSet.getInt("id"),
                        resultSet.getInt("pub_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getBoolean("supervisor"));
                users.add(user);
            }

            LOGGER.debug(String.format("Retrieved %d users from DB", users.size()));
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for User by PubID", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return users.toArray(new User[]{});
    }

    public static boolean checkPin (User user, String pin) {
        return PASSWORD_ENCRYPTOR.checkPassword(pin, user.getPin());
    }

    /**
     * Add a new VIMS User to the DB
     *
     * @param user {@link User} User to Create
     */
    public static void createUser(final User user) {
        user.setPin(PASSWORD_ENCRYPTOR.encryptPassword(user.getPin()));
        final String sql = "INSERT INTO users(pub_id, first_name, last_name, supervisor, pin) VALUES (" + user.pubID +
                ", '" + sanitize(user.firstName) + "', '" + sanitize(user.lastName) + "', " + (user.supervisor ? 1 : 0) + "," +
                "'" + user.getPin() + "');";
        executeStatement(sql);
    }

    /**
     * Update a User in the DB, all fields that are not null will be update.
     * Updates are based off of the User's ID (Internal)
     *
     * @param user {@link User} Updated User
     */
    public static void updateUser(final User user) {
        String values = "";
        if (user.pubID != 0) values += "pub_id=" + user.pubID + ",";
        if (user.firstName != null && user.firstName.length() > 0)
            values += "first_name='" + sanitize(user.firstName) + "',";
        if (user.lastName != null && user.lastName.length() > 0)
            values += "last_name='" + sanitize(user.lastName) + "',";
        if (user.supervisor != null) values += "supervisor=" + (user.supervisor ? 1 : 0) + ",";
        if (user.getPin() != null && user.getPin().length() > 0) {
            user.setPin(PASSWORD_ENCRYPTOR.encryptPassword(user.getPin()));
            values += "pin='" + user.getPin() + "',";
        }

        //language=SQL
        final String sql = "UPDATE users SET " + values.substring(0, values.length() - 1) + " WHERE id=" + user.dbID + ";";
        // The last character of the values string is removed, since it is a comma and would cause a SQL exception if not removed.
        executeStatement(sql);
    }

    // ====================== Transactions ======================

    /**
     * Retreive All, or a specific transaction(s) based on a SQL Restriction statement.
     *
     * @param restriction {@link String} SQL WHERE condition, Excluding WHERE Operator
     * @return {@link Transaction[]} Transactions meeting the specified restriction
     */
    public static Transaction[] getTransaction(String restriction) {
        Connection connection = getConnection();
        Statement statement = null;
        Map<String, Transaction> transactions = new HashMap<>();

        try {
            statement = connection.createStatement();
            if (restriction == null || restriction.length() == 0) restriction = "TRUE";

            //langauge=SQL
            final String sql = "SELECT\n" +
                    "  t.id         AS `transaction_id`,\n" +
                    "  u.id         AS `owner_db_id`,\n" +
                    "  u.pub_id     AS `owner_pub_id`,\n" +
                    "  u.first_name AS `owner_first_name`,\n" +
                    "  u.last_name  AS `owner_last_name`,\n" +
                    "  u.supervisor AS `owner_user_level`,\n" +
                    "  s.id         AS `supervisor_db_id`,\n" +
                    "  s.pub_id     AS `supervisor_pub_id`,\n" +
                    "  s.first_name AS `supervisor_first_name`,\n" +
                    "  s.last_name  AS `supervisor_last_name`,\n" +
                    "  s.supervisor AS `supervisor_user_level`,\n" +
                    "  t.time       AS `transaction_time`,\n" +
                    "  t.direction  AS `transaction_direction`,\n" +
                    "  i.id         AS `component_id`,\n" +
                    "  i.pub_id     AS `component_pub_id`,\n" +
                    "  i.category   AS `component_cat_id`,\n" +
                    "  c.name       AS `component_cat_name`,\n" +
                    "  i.name       AS `component_name`,\n" +
                    "  i.comments   AS `component_condition`\n" +
                    "FROM transactions t\n" +
                    "  LEFT OUTER JOIN inventory i ON t.item_id = i.id\n" +
                    "  LEFT OUTER JOIN categories c ON i.category = c.id\n" +
                    "  LEFT OUTER JOIN users u ON t.owner = u.id\n" +
                    "  LEFT OUTER JOIN users s ON t.supervisor = s.id" +
                    "WHERE " + restriction + ";";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String transaction_id = resultSet.getString("transaction_id");
                if (!transactions.containsKey(transaction_id)) {
                    // Add the transaction shell if it does not yet exist in the MAP
                    Transaction transaction = new Transaction(
                            transaction_id,
                            new User(
                                    resultSet.getInt("owner_db_id"),
                                    resultSet.getInt("owner_pub_id"),
                                    resultSet.getString("owner_first_name"),
                                    resultSet.getString("owner_last_name"),
                                    resultSet.getBoolean("owner_user_level")
                            ),
                            new User(
                                    resultSet.getInt("supervisor_pub_id"),
                                    resultSet.getInt("supervisor_pub_id"),
                                    resultSet.getString("supervisor_first_name"),
                                    resultSet.getString("supervisor_last_name"),
                                    resultSet.getBoolean("supervisor_user_level")
                            ),
                            resultSet.getTimestamp("transaction_time"),
                            resultSet.getBoolean("transaction_direction")
                    );
                    transactions.put(transaction_id, transaction);
                }

                // Now add the component(s) to the transaction
                Transaction.Component component = new Transaction.Component(
                        resultSet.getInt("component_id"),
                        resultSet.getInt("component_pub_id"),
                        new Category(
                                resultSet.getInt("component_cat_id"),
                                resultSet.getString("component_cat_name")
                        ),
                        resultSet.getString("component_name"),
                        resultSet.getString("component_condition")
                );

                transactions.get(transaction_id).components.add(component);
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Transaction Record by Item", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return transactions.values().toArray(new Transaction[]{});
    }

    /**
     * Add a Transaction record to the DB
     *
     * @param transaction {@link Transaction} Transaction to Save
     */
    public static void createTransaction(final Transaction transaction) {
        Statement statement = null;
        Connection connection = null;

        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            statement = connection.createStatement();

            for (Transaction.Component component : transaction.components) {
                //language=SQL

                // Create Transaction Record
                statement.addBatch("INSERT INTO transactions VALUES ('" + sanitize(transaction.id) + "', " + component.id + ", " + transaction.owner.dbID + ", NOW(), " + transaction.supervisor.dbID + ", '" + sanitize(component.comments) + "', " + (transaction.direction ? 1 : 0) + ")");

                // Update Item Comments
                statement.addBatch("UPDATE inventory\n" +
                        "SET comments = '" + sanitize(component.comments) + "'\n" +
                        "WHERE id = " + component.id + ";");

                // Update Item Status (Checked In/Out)
                statement.addBatch("UPDATE inventory\n" +
                        "SET checked_out = " + (transaction.direction ? 0 : 1) + "\n" +  // Direction: TRUE = IN & FALSE = OUT
                        "WHERE id = " + component.id + ";");
            }

            statement.executeBatch();
        } catch (Exception e) {
            LOGGER.warn("Problem Creating New Transaction Record", e);
        } finally {

            if (statement != null) {
                try {
                    connection.setAutoCommit(true);
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }
    }

    // ====================== Quotes ======================

    /**
     * Get the total number of Quotes in the DB.
     * This method assumes that the quotes are have a sequential unique ID!
     *
     * @return {@link int} Number of Quotes in the DB
     */

    public static int getNumQuotes() {
        Connection connection = getConnection();
        Statement statement = null;
        int maxInt = 1;

        try {
            statement = connection.createStatement();
            final String sql = "SELECT MAX(id) FROM quotes;";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                maxInt = resultSet.getInt(1);
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Max Quote ID", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return maxInt;
    }

    /**
     * Returns the Quote with the specified ID
     *
     * @param quoteNum {@link int} Quote ID
     * @return {@link Quote.QuoteModel} Quote Model (Author and Text)
     */
    public static Quote.QuoteModel getQuote(final int quoteNum) {
        Connection connection = getConnection();
        Statement statement = null;
        Quote.QuoteModel quote = null;

        try {
            statement = connection.createStatement();
            final String sql = "SELECT author, text FROM quotes WHERE id = " + quoteNum + ";";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                quote = new Quote.QuoteModel(resultSet.getString("author"),
                        resultSet.getString("text"));
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Quote by ID", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return quote;
    }

    // ====================== Items ======================

    public static Item[] getItem(String restriction) {
        Connection connection = getConnection();
        Statement statement = null;
        List<Item> items = new ArrayList<>();

        try {
            statement = connection.createStatement();
            if (restriction == null || restriction.length() == 0) restriction = "TRUE";

            //language=SQL
            final String sql = "SELECT DISTINCT\n" +
                    "  i.id        AS id,\n" +
                    "  pub_id,\n" +
                    "  i.name      AS name,\n" +
                    "  i.category  AS categoryID,\n" +
                    "  cat.name    AS category_name,\n" +
                    "  short_name,\n" +
                    "  comments,\n" +
                    "  checked_out,\n" +
                    "  t1.time AS last_transaction_time,\n" +
                    "  t1.id as last_transaction_id\n" +
                    "FROM inventory i\n" +
                    "  LEFT JOIN macros m ON m.itemIDs RLIKE CONCAT('[[.[.]|, ]', i.id, '[ ,|[.].]]')\n" +
                    "  LEFT JOIN transactions t1 ON t1.item_id = i.id\n" +
                    "  LEFT OUTER JOIN transactions t2 ON t2.item_id = i.id\n" +
                    "                                     AND (t1.time < t2.time)\n" +
                    "  LEFT JOIN categories cat ON i.category = cat.id\n" +
                    "\n" +
                    "WHERE t2.id IS NULL\n" +
                    "AND (" + restriction + ");";

            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Item item = new Item(resultSet.getInt("id"),
                        resultSet.getInt("pub_id"),
                        new Category(resultSet.getInt("categoryID"), resultSet.getString("category_name")),
                        resultSet.getString("name"),
                        resultSet.getString("short_name"),
                        resultSet.getString("comments") != null ? resultSet.getString("comments") : "",
                        resultSet.getBoolean("checked_out"));

                Timestamp transactionDate;

                try {
                    transactionDate = resultSet.getTimestamp("last_transaction_time");
                } catch (SQLException e) {
                    LOGGER.warn("Problem Parsing Timestamp", e);
                    transactionDate = null;
                }

                if (transactionDate != null) {
                    SimpleDateFormat ft = new SimpleDateFormat("E. MMMM dd hh:mm a");
                    item.lastTransactionDate = ft.format(transactionDate);
                } else {
                    item.lastTransactionDate = "None";
                }

                String last_transaction_id = resultSet.getString("last_transaction_id");
                item.lastTransactionID = last_transaction_id != null ? last_transaction_id : "";

                items.add(item);
            }
            LOGGER.debug(String.format("Retrieved %d items from the DB", items.size()));

            resultSet.close();

        } catch (SQLException e) {
            LOGGER.warn("Problem retrieving items from DB", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return items.toArray(new Item[]{});
    }

    /**
     * Add a new Inventory Item to the DB
     *
     * @param item {@link Item} Item to Create
     */
    public static void createItem(final Item item) {
        final String sql = "INSERT INTO inventory(pub_id, name, short_name) VALUES (" + item.pubID + ", '" +
                sanitize(item.name) + "', '" + sanitize(item.shortName) + "');";
        executeStatement(sql);
    }

    /**
     * Update an Inventory Item.
     * All fields that are not null will be updated. A valid Item ID (Internal) needs to be supplied.
     *
     * @param item {@link Item} Updated Item
     */
    public static void updateItem(final Item item) {
        String values = "";
        if (item.pubID != 0) values += "pub_id = " + item.pubID + ",";
        if (item.category != null) values += "category = " + (item.category.id != null ? item.category.id : "null") + ",";
        if (item.name != null) values += "name = '" + item.name + "',";
        if (item.shortName != null) values += "short_name = '" + item.shortName + "',";
        if (item.comments != null) values += "comments = '" + item.comments + "',";

        //language=SQL
        final String sql = "UPDATE inventory SET " + values.substring(0, values.length() - 1) + " WHERE id=" + item.id + ";";
        // The last character of the values string is removed, since it is a comma and would cause a SQL exception if not removed.
        executeStatement(sql);
    }

    /**
     * Delete an Item from the Inventory. This is only possible if the item has never been checked out or in.
     * This action is NOT reversible.
     * The Item must have an Internal ID defined.
     *
     * @param item {@link Item} Item to Delete
     */
    public static void deleteItem(final Item item) {
        LOGGER.warn("Deleting Item with ID: " + item.id);

        //language=SQL
        final String sql = "DELETE FROM inventory WHERE id = " + item.id + ";";
        executeStatement(sql);
    }

//    ====================== Macros ======================

    /**
     * Retrieve all macros from the DB that meet a specific restriction.
     * If the restriction is null or an empty string, all macros will be returned.
     *
     * @param restriction {@link String} SQL WHERE string, excluding WHERE Operator
     * @return {@link Macro[]} Array of Macros matching restriction
     */
    public static Macro[] getMacro(String restriction) {
        Connection connection = getConnection();
        Statement statement = null;
        List<Macro> macros = new ArrayList<>();

        if (restriction == null || restriction.length() == 0) restriction = "TRUE";

        try {
            statement = connection.createStatement();
            final String sql = "SELECT * FROM macros WHERE " + restriction + ";";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Macro macro = new Macro(resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("itemIDs"));
                macros.add(macro);
            }

            resultSet.close();
            LOGGER.debug(String.format("Retrieved %d macros from DB", macros.size()));

        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Macros", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return macros.toArray(new Macro[]{});
    }

    /**
     * Add a new Macro to the DB
     *
     * @param macro {@link Macro} Macro to Create
     */
    public static void createMacro(final Macro macro) {
        //language=SQL
        final String sql = "INSERT INTO macros (id, name, itemIDs) VALUES (" + macro.id + ", '" + macro.name + "', '" + Arrays.toString(macro.items) + "');\n";
        executeStatement(sql);
    }

    /**
     * Update a Macro.
     * The ID must be defined for the update to be successful.
     *
     * @param macro {@link Macro} Macro with updated values. The ID cannot be changed.
     */
    public static void updateMacro(final Macro macro) {
        //language=SQL
        final String sql = "UPDATE macros SET name='" + macro.name + "', itemIDs = '" + Arrays.toString(macro.items) + "' WHERE id = " + macro.id + ";";
        executeStatement(sql);
    }

    /**
     * Delete a Macro. This action is non-revertible.
     * The Macro must have an ID defined.
     *
     * @param macro {@link Macro} Macro to delete
     */
    public static void deleteMacro(final Macro macro) {
        LOGGER.warn("Deleting Macro with ID: " + macro.id);
        //language=SQL
        final String sql = "DELETE FROM macros WHERE id=" + macro.id + ";";
        executeStatement(sql);
    }

//    ====================== Categories ======================

    public static Category[] getCategory(Integer id) {
        Connection connection = getConnection();
        Statement statement = null;
        List<Category> categories = new ArrayList<>();

        String restriction;
        if (id != null) restriction = "id = " + id;
        else restriction = "TRUE";

        try {
            statement = connection.createStatement();
            final String sql = "SELECT * FROM categories WHERE " + restriction + ";";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Category category = new Category(resultSet.getInt("id"),
                        resultSet.getString("name"));
                categories.add(category);
            }

            resultSet.close();

        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Categories", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return categories.toArray(new Category[]{});
    }

    public static Category createCategory(Category category) {
        //language=SQL
        final String statement_sql = "INSERT INTO categories (`id`, `name`) VALUES (" + category.id + ", '" + category.name + "');";
        final String query_sql = "SELECT * FROM categories WHERE id=LAST_INSERT_ID();";

        Connection connection = getConnection();
        Statement statement = null;

        try {
            statement = connection.createStatement();
            LOGGER.info(String.format("Executing SQL Statement - \"%s\"", statement));
            statement.execute(statement_sql);

            LOGGER.info(String.format("Executing SQL Query - \"%s\"", query_sql));
            ResultSet resultSet = statement.executeQuery(query_sql);

            if (resultSet.next()) {
                category.id = resultSet.getInt("id");
            }

            resultSet.close();

        } catch (SQLException e) {
            LOGGER.error("Problem creating new Item in DB", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return category;
    }

    public static void updateCategory(final Category category) {
        //language=SQL
        String sql = "UPDATE categories SET name = '" + category.name + "' WHERE id=" + category.id + ";";
        executeStatement(sql);
    }

    public static void deleteCategory(final Category category) {
        LOGGER.warn(String.format("Deleting Category #%d (%s)", category.id, category.name));

        //language=SQL
        String sql = "DELETE FROM categories WHERE id = " + category.id + ";";
        executeStatement(sql);
    }

    private static String sanitize(final String s) {
        return s.replace("'", "");
    }
}
