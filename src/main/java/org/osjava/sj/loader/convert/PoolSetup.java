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

package org.osjava.sj.loader.convert;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This is a wrapper for the Pooling functionality, currently provided
 * by Jakarta DBCP. Having the wrapper allows the dependency to be
 * optional.
 */
public class PoolSetup {

    static void setupConnection(String name, String url, String username, String password, Properties properties) throws SQLException {
        GenericObjectPool connectionPool = new GenericObjectPool(null,
                NumberUtils.toInt(properties.getProperty("dbcpMaxActive"), GenericObjectPool.DEFAULT_MAX_ACTIVE),
                (byte) NumberUtils.toInt(properties.getProperty("dbcpWhenExhaustedAction"), GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION),
                NumberUtils.toLong(properties.getProperty("dbcpMaxWait"), GenericObjectPool.DEFAULT_MAX_WAIT),
                NumberUtils.toInt(properties.getProperty("dbcpMaxIdle"), GenericObjectPool.DEFAULT_MAX_IDLE),
                NumberUtils.toInt(properties.getProperty("dbcpMinIdle"), GenericObjectPool.DEFAULT_MIN_IDLE),
                BooleanUtils.toBoolean(properties.getProperty("dbcpTestOnBorrow")) /* GenericObjectPool.DEFAULT_TEST_ON_BORROW */,
                BooleanUtils.toBoolean(properties.getProperty("dbcpTestOnReturn") /* GenericObjectPool.DEFAULT_TEST_ON_RETURN */),
                NumberUtils.toLong(properties.getProperty("dbcpTimeBetweenEvictionRunsMillis"), GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS),
                NumberUtils.toInt(properties.getProperty("dbcpNumTestsPerEvictionRun"), GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN),
                NumberUtils.toLong(properties.getProperty("dbcpMinEvictableIdleTimeMillis"), GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS),
                BooleanUtils.toBoolean(properties.getProperty("dbcpTestWhileIdle") /* GenericObjectPool.DEFAULT_TEST_WHILE_IDLE */),
                NumberUtils.toLong(properties.getProperty("dbcpSoftMinEvictableIdleTimeMillis"), GenericObjectPool.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS)
        );
//            toBoolean(properties.getProperty("dbcpLifo"), GenericObjectPool.DEFAULT_LIFO)

        ConnectionFactory connectionFactory;
        if (username == null || password == null) {
            // TODO: Suck configuration in and build a Properties to replace the null below
            connectionFactory = new DriverManagerConnectionFactory(url, null);
        }
        else {
            connectionFactory = new DriverManagerConnectionFactory(url, username, password);
        }
        new PoolableConnectionFactory(
                connectionFactory,
                connectionPool,
                null,
                properties.getProperty("dbcpValidationQuery"),
                BooleanUtils.toBoolean(properties.getProperty("dbcpDefaultReadOnly")),
                BooleanUtils.toBooleanDefaultIfNull(
                        BooleanUtils.toBooleanObject(
                                properties.getProperty("dbcpDefaultAutoCommit")), true));
        try {
            Class.forName("org.apache.commons.dbcp.PoolingDriver");
        }
        catch (ClassNotFoundException e) {
            // not too good
            System.err.println("WARNING: DBCP needed but not in the classpath. ");
        }
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.registerPool(name, connectionPool);

        //  Runtime.getRuntime().addShutdownHook( new ShutdownDbcpThread(name) );
    }

    static String getUrl(String poolName) {
        return "jdbc:apache:commons:dbcp:" + poolName;
    }

}
