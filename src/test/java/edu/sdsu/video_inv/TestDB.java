package edu.sdsu.video_inv;

import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.Macro;
import edu.sdsu.its.video_inv.Models.User;
import edu.sdsu.its.video_inv.Param;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit Tests for DB Methods
 *
 * @author Tom Paulus
 *         Created on 4/20/16.
 */
public class TestDB {
    final private static Logger LOGGER = Logger.getLogger(TestDB.class);
    final private static int TEST_USER_ID = 123456;
    final private static int TEST_ITEM_ID = 987654;
    final private static int TEST_MACRO_ID = 999801;

    /**
     * Check if the KeyServer has access to the correct credentials
     */
    @Test
    public void checkParams() {
        final String db_url = Param.getParam("db-url");
        LOGGER.debug("KS.db-url = " + db_url);
        assertTrue("URL is Empty", db_url != null && db_url.length() > 0);
        assertTrue("Invalid URL", db_url.startsWith("jdbc:mysql://"));

        final String db_user = Param.getParam("db-user");
        LOGGER.debug("KS.db-user = " + db_user);
        assertTrue("Username is Empty", db_user != null && db_user.length() > 0);

        final String db_password = Param.getParam("db-password");
        LOGGER.debug("KS.db-password = " + db_password);
        assertTrue("Password is Empty", db_password != null && db_password.length() > 0);
    }


    /**
     * Test DB Connection
     */
    @Test
    public void connect() {
        Connection connection = null;
        try {
            LOGGER.debug("Attempting to connect to the DB Server");
            connection = DB.getConnection();
            LOGGER.info("DB Connection established");
            assertTrue(connection.isValid(5));
        } catch (SQLException e) {
            LOGGER.error("Problem connecting to the DB Server", e);
            fail("SQL Exception thrown while trying to connect to the DB - " + e.getMessage());
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    LOGGER.debug("DB Connection Closed");
                }
            } catch (SQLException e) {
                LOGGER.error("Problem closing the DB Connection", e);
            }
        }
    }

    /**
     * Check if the Test User is in the Database and Parsable
     */
    @Test
    public void getUser() {
        User user = DB.getUser("pub_id = " + TEST_USER_ID)[0];
        assertTrue("User not defined", user != null);
        LOGGER.debug("Found user with ID: " + TEST_USER_ID);
        assertTrue("User not Complete - Name not defined",
                user.firstName != null && user.firstName.length() > 0 &&
                        user.lastName != null && user.lastName.length() > 0);
    }

    /**
     * Check if the Test Item is in the Database and Parsable
     */
    @Test
    public void getItem() {
        Item[] items = DB.getItem("i.pub_id = " + TEST_ITEM_ID);
        assertTrue("Item not defined", items.length > 0);
        Item item = items[0];
        LOGGER.debug("Found item with ID: " + TEST_ITEM_ID);
        assertTrue("Item not Complete - Name Missing", item.name != null && item.name.length() > 0);
        assertTrue("Get item by DB_ID does not match PubBID", DB.getItem("i.id = " + item.id)[0].pubID == item.pubID);
    }

    @Test
    public void getItems() {
        Item[] items = DB.getItem("i.pub_id = " + TEST_MACRO_ID + " OR m.id = " + TEST_MACRO_ID);
        assertTrue("Macro not defined", items.length > 0);
        LOGGER.debug(String.format("Macro Lookup returned %d items", items.length));
        for (Item i : items) {
            assertTrue("Item not defined", i != null);
            assertTrue("Item not Complete - Name Missing", i.name != null && i.name.length() > 0);
        }
    }

    @Test
    public void getMacros() {
        Macro[] macros = DB.getMacro(null);
        assertTrue("No Macros Found", macros.length > 0);
        LOGGER.debug(String.format("%d macros found in DB", macros.length));
        for (Macro m : macros) {
            assertTrue("Macro not defined", m != null);
            assertTrue("Macro not Complete - Name Missing", m.name != null && m.name.length() > 0);
            assertTrue("Macro not Complete - Items Missing", m.items != null && m.items.length > 0);
        }
    }

}
