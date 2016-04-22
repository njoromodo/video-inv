package edu.sdsu.its.video_inv;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.Transaction;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Get User Object by their public ID (Not the same as their DB ID).
     *
     * @param pubID {@link int} User's Public ID
     * @return {@link User} User Object
     */
    public static User getUser(final int pubID) {
        Connection connection = getConnection();
        Statement statement = null;
        User user = null;

        int uid;
        if (pubID > Math.pow(10, 6)) {
            // Supplied Checksum includes the checksum, we don't care about the checksum
            uid = pubID / 10;
        } else if (pubID > Math.pow(10, 5)) {
            uid = pubID;
        } else {
            return null;
        }

        try {
            statement = connection.createStatement();
            final String sql = "SELECT * FROM users WHERE pub_id = " + uid + ";";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                user = new User(resultSet.getInt("id"),
                        pubID,
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getBoolean("supervisor"));
            }

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

        return user;
    }

    /**
     * Get User Object by their public ID (Not the same as their DB ID).
     *
     * @param restriction {@link String} Restriction on the Search, as a WHERE SQL Statement
     * @return {@link User} User Object
     */
    public static User[] getAllUsers(final String restriction) {
        Connection connection = getConnection();
        Statement statement = null;
        List<User> userList = new ArrayList<>();

        try {
            statement = connection.createStatement();
            final String sql = "SELECT * FROM users " + restriction + " ORDER BY last_name ASC;";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                User user = new User(resultSet.getInt("id"),
                        resultSet.getInt("pub_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getBoolean("supervisor"));
                userList.add(user);
            }

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

        User[] userArr = new User[userList.size()];
        for (int u = 0; u < userList.size(); u++) {
            userArr[u] = userList.get(u);
        }
        return userArr;
    }

    /**
     * Get User Information by Pin
     *
     * @param pin {@link String} Pin Hash
     * @return {@link User} User
     */
    public static User getUser(final String pin) {
        Connection connection = getConnection();
        Statement statement = null;
        User user = null;

        try {
            statement = connection.createStatement();
            final String sql = "SELECT * FROM users WHERE pin = '" + pin + "';";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                user = new User(resultSet.getInt("id"),
                        resultSet.getInt("pub_ID"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getBoolean("supervisor"));
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for User by Pin", e);
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

        return user;
    }

    /**
     * Get inventory item by its public id (Not the same as their DB ID).
     *
     * @param pubID {@link int} Item's Public ID
     * @return {@link Item} Inventory Item
     */
    public static Item getItem(final int pubID) {
        Connection connection = getConnection();
        Statement statement = null;
        Item item = null;

        try {
            statement = connection.createStatement();
            final String sql = "SELECT * FROM inventory WHERE pub_id = " + pubID + ";";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                item = new Item(resultSet.getInt("id"),
                        pubID,
                        resultSet.getString("name"),
                        resultSet.getString("short_name"),
                        resultSet.getString("comments") != null ? resultSet.getString("comments") : "",
                        resultSet.getBoolean("checked_out"));
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Inventory Item by pubID", e);
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

        return item;
    }

    /**
     * Get inventory item by its database id (Not the same as their Public ID).
     *
     * @param dbID {@link int} Item's DataBase ID
     * @return {@link Item} Inventory Item
     */
    public static Item getItemByDB(final int dbID) {
        Connection connection = getConnection();
        Statement statement = null;
        Item item = null;

        try {
            statement = connection.createStatement();
            final String sql = "SELECT * FROM inventory WHERE id = " + dbID + ";";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                item = new Item(dbID,
                        resultSet.getInt("pub_id"),
                        resultSet.getString("name"),
                        resultSet.getString("short_name"),
                        resultSet.getString("comments") != null ? resultSet.getString("comments") : "",
                        resultSet.getBoolean("checked_out"));
            }

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Inventory Item by dbID", e);
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

        return item;
    }

    /**
     * Retrieve a Transaction by an Item in the Transaction and the direction of the transaction
     *
     * @param item {@link Item} Item in the Transaction
     * @return {@link Transaction} Transaction Record with designated Item
     */
    public static Transaction getTransactionByItem(final Item item) {
        Connection connection = getConnection();
        Statement statement = null;
        Transaction transaction = null;

        try {
            statement = connection.createStatement();
            final String sql = "SELECT\n" +
                    "  transactions.id AS id,\n" +
                    "  owner,\n" +
                    "  out_components,\n" +
                    "  in_components,\n" +
                    "  out_time,\n" +
                    "  in_time,\n" +
                    "  pub_id          AS owner_pubID\n" +
                    "FROM transactions\n" +
                    "  JOIN users ON transactions.owner = users.id\n" +
                    "WHERE INSTR(out_components, '\"id\": " + item.id + "')\n" +
                    "ORDER BY id DESC\n" +
                    "LIMIT 1;\n";

            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                final Gson gson = new Gson();
                Transaction.Component outComponents = gson.fromJson(resultSet.getString("out_components"), Transaction.Component.class);
                Transaction.Component inComponents = gson.fromJson(resultSet.getString("in_components"), Transaction.Component.class);

                outComponents.items.forEach(Item::completeItem);
                if (inComponents != null) inComponents.items.forEach(Item::completeItem);

                Timestamp out;
                Timestamp in;

                try {
                    out = resultSet.getTimestamp("out_time");
                } catch (SQLException e) {
                    LOGGER.warn("Problem Parsing Timestamp", e);
                    out = null;
                }
                try {
                    in = resultSet.getTimestamp("in_time");
                } catch (SQLException e) {
                    LOGGER.warn("Problem Parsing Timestamp", e);
                    in = null;
                }

                transaction = new Transaction(resultSet.getInt("id"),
                        resultSet.getInt("owner"),
                        resultSet.getInt("owner_pubID"),
                        outComponents,
                        inComponents,
                        out,
                        in);
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

        return transaction;
    }

    /**
     * Retrieve a Transaction by its ID
     *
     * @param transactionID {@link int} TransactionID
     * @return {@link Transaction} Transaction Record with designated Item
     */
    public static Transaction getTransactionByID(final int transactionID) {
        Connection connection = getConnection();
        Statement statement = null;
        Transaction transaction = null;

        try {
            statement = connection.createStatement();
            final String sql = "SELECT\n" +
                    "  transactions.id AS id,\n" +
                    "  owner,\n" +
                    "  out_components,\n" +
                    "  in_components,\n" +
                    "  out_time,\n" +
                    "  in_time,\n" +
                    "  pub_id          AS owner_pubID\n" +
                    "FROM transactions\n" +
                    "  JOIN users ON transactions.owner = users.id\n" +
                    "WHERE transactions.id = " + transactionID + ";";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                final Gson gson = new Gson();
                Transaction.Component outComponents = gson.fromJson(resultSet.getString("out_components"), Transaction.Component.class);
                Transaction.Component inComponents = gson.fromJson(resultSet.getString("in_components"), Transaction.Component.class);

                outComponents.items.forEach(Item::completeItem);
                if (inComponents != null) inComponents.items.forEach(Item::completeItem);

                transaction = new Transaction(resultSet.getInt("id"),
                        resultSet.getInt("owner"),
                        resultSet.getInt("owner_pubID"),
                        outComponents,
                        inComponents,
                        resultSet.getTimestamp("out_time"),
                        resultSet.getTimestamp("in_time"));
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

        return transaction;
    }

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

    /**
     * Retrieve all Items from the Inventory
     *
     * @return {@link Item[]} All Items
     */
    public static Item[] getInventory() {
        Connection connection = getConnection();
        Statement statement = null;
        Item[] items = null;
        List<Item> ilist = new ArrayList<>();


        try {
            statement = connection.createStatement();
            final String sql = "SELECT i.id AS id, name, pub_id, short_name, comments, checked_out, t1.out_time AS out_time, t1.in_time AS in_time\n" +
                    "FROM inventory i\n" +
                    "LEFT JOIN transactions t1 ON t1.out_components REGEXP CONCAT('\"id\": ', i.id, ',')\n" +
                    "LEFT OUTER JOIN transactions t2 ON t2.out_components REGEXP CONCAT('\"id\": ', i.id, ',')\n" +
                    "  AND (t1.id < t2.id )\n" +
                    "WHERE t2.id IS NULL;\n";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Item item = new Item(resultSet.getInt("id"),
                        resultSet.getInt("pub_id"),
                        resultSet.getString("name"),
                        resultSet.getString("short_name"),
                        resultSet.getString("comments") != null ? resultSet.getString("comments") : "",
                        resultSet.getBoolean("checked_out"));

                Timestamp out;
                Timestamp in;

                try {
                    out = resultSet.getTimestamp("out_time");
                } catch (SQLException e) {
                    LOGGER.warn("Problem Parsing Timestamp", e);
                    out = null;
                }
                try {
                    in = resultSet.getTimestamp("in_time");
                } catch (SQLException e) {
                    LOGGER.warn("Problem Parsing Timestamp", e);
                    in = null;
                }

                if (in != null || out != null) {
                    SimpleDateFormat ft = new SimpleDateFormat("E. MMMM dd hh:mm a");
                    item.lastTransactionDate = ft.format(in != null ? in : out);
                } else {
                    item.lastTransactionDate = "None";
                }

                ilist.add(item);
            }

            resultSet.close();

            items = new Item[ilist.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = ilist.get(i);
            }
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Inventory Item by dbID", e);
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

        return items;
    }

    /**
     * Get Transaction History for a given item
     *
     * @param item {@link Item} Item
     * @return {@link Transaction[]} Transaction History for Item
     */
    public static Transaction[] getHistory(final Item item) {
        Connection connection = getConnection();
        Statement statement = null;
        Transaction[] transactions = null;
        List<Transaction> tlist = new ArrayList<>();


        try {
            statement = connection.createStatement();
            final String sql = "SELECT\n" +
                    "  transactions.id AS id,\n" +
                    "  owner,\n" +
                    "  out_components,\n" +
                    "  in_components,\n" +
                    "  out_time,\n" +
                    "  in_time,\n" +
                    "  pub_id          AS owner_pubID\n" +
                    "FROM transactions\n" +
                    "  JOIN users ON transactions.owner = users.id\n" +
                    "WHERE INSTR(out_components, '\"id\": " + item.id + "')\n" +
                    "ORDER BY out_time, in_time DESC;";
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                final Gson gson = new Gson();
                Transaction.Component outComponents = gson.fromJson(resultSet.getString("out_components"), Transaction.Component.class);
                Transaction.Component inComponents = gson.fromJson(resultSet.getString("in_components"), Transaction.Component.class);

                outComponents.items.forEach(Item::completeItem);
                if (inComponents != null) inComponents.items.forEach(Item::completeItem);

                Timestamp out;
                Timestamp in;

                try {
                    out = resultSet.getTimestamp("out_time");
                } catch (SQLException e) {
                    LOGGER.warn("Problem Parsing Timestamp", e);
                    out = null;
                }
                try {
                    in = resultSet.getTimestamp("in_time");
                } catch (SQLException e) {
                    LOGGER.warn("Problem Parsing Timestamp", e);
                    in = null;
                }

                Transaction transaction = new Transaction(resultSet.getInt("id"),
                        resultSet.getInt("owner"),
                        resultSet.getInt("owner_pubID"),
                        outComponents,
                        inComponents,
                        out,
                        in);

                tlist.add(transaction);
            }

            resultSet.close();

            transactions = new Transaction[tlist.size()];
            for (int i = 0; i < transactions.length; i++) {
                transactions[i] = tlist.get(i);
            }
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Transaction History for Item with DB-ID: " + item.id, e);
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

        return transactions;
    }

    /**
     * Add a new Inventory Item to the DB
     *
     * @param item {@link Item} Item to Create
     */
    public static void addItem(final Item item) {
        final String sql = "INSERT INTO inventory(pub_id, name, short_name) VALUES (" + item.pubID + ", '" + sanitize(item.name) + "', '" + sanitize(item.shortName) + "');";
        executeStatement(sql);
    }

    /**
     * Add a new VIMS User to the DB
     *
     * @param user    {@link User} User to Create
     * @param pinHash {@link String} User's Pin Hash
     */
    public static void addUser(final User user, final String pinHash) {
        final String sql = "INSERT INTO users(pub_id, first_name, last_name, supervisor, pin) VALUES (" + user.pubID +
                ", '" + sanitize(user.firstName) + "', '" + sanitize(user.lastName) + "', " + (user.supervisor ? 1 : 0) + "," +
                "'" + pinHash + "');";
        executeStatement(sql);
    }

    /**
     * Add a Transaction record to the DB
     *
     * @param transaction {@link Transaction} Transaction to Save
     */
    public static void addTransaction(final Transaction transaction) {
        final String sql = "INSERT INTO transactions (owner, out_components, out_time) VALUES (\n" +
                "  (SELECT id\n" +
                "   FROM users\n" +
                "   WHERE pub_id = " + transaction.ownerID + "),\n" +
                "  '" + transaction.out_components.toString() + "', now()\n" +
                ");";
        executeStatement(sql);
    }

    public static void updateTransaction(final Transaction transaction) {
        // fixme
        final String sql = "UPDATE transactions\n" +
                "SET in_components = '" + transaction.in_components.toString() + "',\n" +
                "  in_time = now()\n" +
                "WHERE id = " + transaction.id + ";";
        executeStatement(sql);
    }

    /**
     * Update Item Comments.
     * Item comments are saved both together with the item in the Transaction record, as well as with the Item.
     *
     * @param item {@link Item} Item to Update with Updated Comments
     */
    public static void updateComments(final Item item) {
        final String sql;
        if (item.id != 0) {
            sql = "UPDATE inventory\n" +
                    "SET comments = '" + sanitize(item.comments) + "'\n" +
                    "WHERE id = " + item.id + ";";
        } else {
            sql = "UPDATE inventory\n" +
                    "SET comments = '" + sanitize(item.comments) + "'\n" +
                    "WHERE pub_id = " + item.pubID + ";";
        }

        executeStatement(sql);
    }

    public static void updateItemStatus(final Item item) {
        final String sql;
        if (item.id != 0) {
            sql = "UPDATE inventory\n" +
                    "SET checked_out = " + (item.checked_out ? 1 : 0) + "\n" +
                    "WHERE id = " + item.id + ";";
        } else {
            sql = "UPDATE inventory\n" +
                    "SET checked_out = " + (item.checked_out ? 1 : 0) + "\n" +
                    "WHERE pub_id = " + item.pubID + ";";
        }

        executeStatement(sql);
    }

    private static String sanitize(final String s) {
        return s.replace("'", "");
    }
}
