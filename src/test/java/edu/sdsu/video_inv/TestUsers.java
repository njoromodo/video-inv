package edu.sdsu.video_inv;

import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test Users and their related functionality.
 *
 * @author Tom Paulus
 *         Created on 8/6/16.
 */
public class TestUsers {
    private static final Logger LOGGER = Logger.getLogger(TestUsers.class);

    private static final int TEST_USER_ID = 999002;
    private static final String TEST_USER_FNAME = "Test";
    private static final String TEST_USER_LNAME = "User";
    private static final boolean TEST_USER_ACCESS = false;
    private static final String TEST_USER_PIN = "abcd";

    private static final String UPDATE_USER_FNAME = "Jane";
    private static final String UPDATE_USER_LNAME = "Doe";
    private static final boolean UPDATE_USER_ACCESS = true;
    private static final String UPDATE_USER_PIN = "fghi";

    private static User USER;

    @BeforeClass
    public static void setUp() throws Exception {
        LOGGER.info("Creating New Test User");
        USER = new User(TEST_USER_ID, TEST_USER_FNAME, TEST_USER_LNAME, TEST_USER_ACCESS);
        USER.setPin(TEST_USER_PIN);
        DB.createUser(USER);
        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute
        USER.completeUser();
        LOGGER.debug("Created New User: " + USER.toString());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.warn(String.format("Deleting Test User (ID: %d/%d)", USER.dbID, USER.pubID));
        DB.deleteUser(USER);
    }

    @Test
    public void getUser() {
        LOGGER.info("Fetching all users and checking for completeness");
        User[] users = DB.getUser(null);
        assertTrue("No Users found", users.length > 0);
        LOGGER.debug(String.format("Retrieved %d users from DB", users.length));

        boolean test_user_found = false;
        for (User u : users) {
            assertNotNull("User not defined", u);
            assertTrue("User has no ID", u.dbID != 0 && u.pubID != 0);
            assertTrue("User not Complete - Name not defined",
                    u.firstName != null && u.firstName.length() > 0 &&
                            u.lastName != null && u.lastName.length() > 0);
            assertNotNull("User has no status defined", u.supervisor);

            if (u.equals(USER)) test_user_found = true;
        }

        assertTrue("Test User not found in DB", test_user_found);

    }

    @Test
    public void updateUser() throws Exception {
        LOGGER.info("Updating User");
        LOGGER.debug("Current User: " + USER.toString());
        USER.firstName = UPDATE_USER_FNAME;
        USER.lastName = UPDATE_USER_LNAME;
        USER.supervisor = UPDATE_USER_ACCESS;
        USER.setPin(UPDATE_USER_PIN);

        DB.updateUser(USER);
        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute
        LOGGER.debug("Updated User: " + USER.toString());

        User[] fetched = DB.getUser("pub_id = " + USER.pubID);
        assertTrue("User not found in DB", fetched.length > 0);
        assertEquals("User not Updated Correctly", USER, fetched[0]);
        assertTrue("Pin not updated correctly", DB.checkPin(fetched[0], UPDATE_USER_PIN));
    }
}
