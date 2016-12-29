/*
 * Copyright (c) 2003, Henri Yandell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of Simple-JNDI nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.osjava.sj.loader;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.Properties;

// gives us pooling
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

/**
 * This is a wrapper for the Pooling functionality, currently provided 
 * by Jakarta DBCP. Having the wrapper allows the dependency to be 
 * optional. 
 */
public class PoolSetup {

    public static void setupConnection(String pool, String url, String username, String password, Properties properties) throws SQLException {
        // we have a pool-name to setup using dbcp
        GenericObjectPool connectionPool = new GenericObjectPool(null, 
            toInt(properties.getProperty("dbcpMaxActive"), GenericObjectPool.DEFAULT_MAX_ACTIVE),
            (byte) toInt(properties.getProperty("dbcpWhenExhaustedAction"), GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION),
            toLong(properties.getProperty("dbcpMaxWait"), GenericObjectPool.DEFAULT_MAX_WAIT),
            toInt(properties.getProperty("dbcpMaxIdle"), GenericObjectPool.DEFAULT_MAX_IDLE),
            toInt(properties.getProperty("dbcpMinIdle"), GenericObjectPool.DEFAULT_MIN_IDLE),
            toBoolean(properties.getProperty("dbcpTestOnBorrow"), GenericObjectPool.DEFAULT_TEST_ON_BORROW),
            toBoolean(properties.getProperty("dbcpTestOnReturn"), GenericObjectPool.DEFAULT_TEST_ON_RETURN),
            toLong(properties.getProperty("dbcpTimeBetweenEvictionRunsMillis"), GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS),
            toInt(properties.getProperty("dbcpNumTestsPerEvictionRun"), GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN),
            toLong(properties.getProperty("dbcpMinEvictableIdleTimeMillis"), GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS),
            toBoolean(properties.getProperty("dbcpTestWhileIdle"), GenericObjectPool.DEFAULT_TEST_WHILE_IDLE),
            toLong(properties.getProperty("dbcpSoftMinEvictableIdleTimeMillis"), GenericObjectPool.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS)
        );
//            toBoolean(properties.getProperty("dbcpLifo"), GenericObjectPool.DEFAULT_LIFO)

        ConnectionFactory connectionFactory = null;
        if(username == null || password == null) {
            // TODO: Suck configuration in and build a Properties to replace the null below
            connectionFactory = new DriverManagerConnectionFactory(url, null);
        } else {
            connectionFactory = new DriverManagerConnectionFactory(url, username, password);
        }
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, properties.getProperty("dbcpValidationQuery"), toBoolean(properties.getProperty("dbcpDefaultReadOnly"), false), toBoolean(properties.getProperty("dbcpDefaultAutoCommit"), true));
        try {
            Class.forName("org.apache.commons.dbcp.PoolingDriver");
        } catch(ClassNotFoundException cnfe) {
            // not too good
            System.err.println("WARNING: DBCP needed but not in the classpath. ");
        }
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.registerPool(pool, connectionPool);

    //  Runtime.getRuntime().addShutdownHook( new ShutdownDbcpThread(pool) );
    }

    public static String getUrl(String pool) {
        return "jdbc:apache:commons:dbcp:"+pool;
    }

    private static int toInt(String str, int def) {
        if(str == null) {
            return def;
        }
        try {
            return Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            throw new RuntimeException("Unable to parse as int: '" + str + "'", nfe);
        }
    }

    private static long toLong(String str, long def) {
        if(str == null) {
            return def;
        }
        try {
            return Long.parseLong(str);
        } catch(NumberFormatException nfe) {
            throw new RuntimeException("Unable to parse as long: '" + str + "'", nfe);
        }
    }

    private static boolean toBoolean(String str, boolean def) {
        if(str == null) {
            return def;
        } else
        if("true".equals(str)) {
            return true;
        } else
        if("false".equals(str)) {
            return false;
        } else {
            throw new RuntimeException("Unable to parse as boolean: '" + str + "'");
        }
    }

}

/*
// this is not available in the version of DBCP being used.
class ShutdownDbcpThread extends Thread {
    
    private String pool;

    public ShutdownDbcpThread(String pool) {
        this.pool = pool;
    }

    public void run() {
        try {
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
            driver.closePool(this.pool);
        } catch(SQLException sqle) {
            // failed to close
        } catch(ClassNotFoundException cnfe) {
            // oops, unable to close pools, sorry DBA
        }
    }
}
*/

