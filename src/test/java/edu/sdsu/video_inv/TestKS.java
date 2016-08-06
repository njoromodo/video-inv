package edu.sdsu.video_inv;

import edu.sdsu.its.video_inv.Param;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test Key Server Environment Variable configuration, Test Connection to Server, and check that the necessary values have been set.
 *
 * @author Tom Paulus
 *         Created on 3/27/16.
 */
public class TestKS {
    private final static Logger LOGGER = Logger.getLogger(TestKS.class);

    /**
     * Check that the environment variables that are used by the Key Server are set.
     */
    @Test
    public void checkENV() {
        final String path = System.getenv("KSPATH");
        final String key = System.getenv("KSKEY");
        final String name = System.getenv("VIMS_APP");

        LOGGER.debug("ENV.KSPATH =" + path);
        LOGGER.debug("ENV.KSKEY =" + key);
        LOGGER.debug("ENV.VIMS_APP =" + name);

        assertTrue("Empty KS URL", path != null && path.length() > 0);
        assertTrue("Empty KS API Key", key != null && key.length() > 0);
        assertTrue("Empty App Name", name != null && name.length() > 0);
    }

    /**
     * Perform a self-test of the connection to the server.
     * Validity of the app-name and api-key are NOT checked.
     */
    @Test
    public void checkConnection() {
        assertTrue(Param.testConnection());
    }

    /**
     * Check if the KeyServer has access to the correct Database credentials
     */
    @Test
    public void checkDBConfig() {
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
     * Check that the Key Server has values set for all Session Variables and that they are valid.
     */
    @Test
    public void checkSessionConfig() {
        final String project_token = Param.getParam("project_token");
        LOGGER.debug("KS.project_token = " + project_token);
        assertTrue("Session Project Token is Empty", project_token != null && project_token.length() > 0);

        final String token_cypher = Param.getParam("token_cypher");
        LOGGER.debug("KS.token_cypher = " + token_cypher);
        assertTrue("Session Token Cypher is Empty", token_cypher != null && token_cypher.length() > 0);

        final String token_ttl = Param.getParam("token_ttl");
        LOGGER.debug("KS.token_ttl = " + token_ttl);
        assertTrue("Session Token TTL is Empty", token_ttl != null && token_ttl.length() > 0);
        assertTrue("Session Token TTL is not valid", Integer.getInteger(token_ttl) > 0);
    }
}
