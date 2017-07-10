/*
 * Copyright (c) 2005, Henri Yandell
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

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A basic implementation of a DataSource with optional connection pooling.
 */
public class SJDataSource implements DataSource {

    private PrintWriter printWriter;
    private String username;
    private String password;
    private String url;
    private String driver;

    // for pooling
    private Properties properties;

    /**
     * if a connection pool has been built, its url is stored in here
     */
    private String poolUrl = null;

    public SJDataSource(String driverName, String url, String username, String password, Properties properties) {
        ensureLoaded(driverName);
        this.driver = driverName;
        this.url = url;
        this.printWriter = new PrintWriter(System.err);
        this.username = username;
        this.password = password;
        this.properties = properties;
    }

    // Method from Apache Commons DbUtils
    private static boolean ensureLoaded(String name) {
        try {
            Class.forName(name).newInstance();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * @see SJDataSource#getConnection(String, String)
     */
    public Connection getConnection() throws SQLException {
        return this.getConnection(this.username, this.password);
    }

    /**
     * With every call a new connection is returned unless property "pool" was set.
     */
    public Connection getConnection(String username, String password) throws SQLException {
        String poolName = properties.getProperty("pool");
        if (poolName != null) {  // we want a connection name named like the pool property
            synchronized (SJDataSource.class) {
                if (poolUrl == null) {  // we didn't create a connection pool already, so do it now
                    PoolSetup.setupConnection(poolName, url, username, password, properties);
                    poolUrl = PoolSetup.getUrl(poolName);
                }
            }
            return getConnection(username, password, poolUrl);
        }
        else {
            return getConnection(username, password, url);
        }
    }

    private Connection getConnection(String username, String password, String tmpUrl) throws SQLException {
        if (username == null || password == null) {
            return DriverManager.getConnection(tmpUrl);
        }
        else {
            return DriverManager.getConnection(tmpUrl, username, password);
        }
    }

    public PrintWriter getLogWriter() throws SQLException {
        return printWriter;
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public void setLogWriter(PrintWriter pw) throws SQLException {
        this.printWriter = pw;
    }

    public void setLoginTimeout(int timeout) throws SQLException {
        // ignored
    }

    public String toString() {
        return driver + "::::" + url + "::::" + username;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        SJDataSource other = (SJDataSource) obj;

        return other.url.equals(this.url) &&
                other.driver.equals(this.driver) &&
                other.username.equals(this.username);
    }

    public int hashCode() {
        return this.url.hashCode() & this.username.hashCode() & this.driver.hashCode();
    }

    // Added by JDK 1.6 via java.sql.Wrapper
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    // Added by JDK 1.6 via java.sql.Wrapper
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("This object is not a wrapper");
    }

    // Patch for Java 1.7
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("This class does not support this operation.");
    }

}

