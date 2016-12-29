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

package org.osjava.sj.loader;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * A basic implementation of a DataSource. 
 */
public class SJDataSource implements DataSource {

    private PrintWriter pw;
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

    public SJDataSource(String driver, String url, String username, String password, Properties properties) {
        ensureLoaded(driver);
        this.driver = driver;
        this.url = url;
        this.pw = new PrintWriter(System.err);
        this.username = username;
        this.password = password;
        this.properties = properties;
    }

    // Method from Apache Commons DbUtils
    private static boolean ensureLoaded(String name) {
        try {
            Class.forName(name).newInstance();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.getConnection(this.username, this.password);
    }

    /**
     * returns a connection to the database specified in the properties and
     * creates a connection pool, if neccessary 
     */
    public Connection getConnection(String username, String password) throws SQLException {
        String tmpUrl = this.url;

        String pool = properties.getProperty("pool");
        if (pool != null) {  // we want a connection name named like the pool property
            synchronized (SJDataSource.class) {
                if (poolUrl == null) {  // we didn't create a connection pool already, so do it now
                    PoolSetup.setupConnection(pool, url, username, password, properties);
                    poolUrl = PoolSetup.getUrl(pool);
                }
            }
            tmpUrl = poolUrl;  // url is now a pooling link
        }

        if(username == null || password == null) {
            return DriverManager.getConnection(tmpUrl);
        }
        return DriverManager.getConnection(tmpUrl, username, password);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return pw;
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public void setLogWriter(PrintWriter pw) throws SQLException {
        this.pw = pw;
    }

    public void setLoginTimeout(int timeout) throws SQLException {
        // ignored
    }

    public String toString() {
        return driver + "::::" + url + "::::" + username;
    }

    public boolean equals(Object obj) {
        if(obj == null) { 
            return false;
        }
        if(obj.getClass() != this.getClass()) {
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
    public <T>  T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("This object is not a wrapper");
    }

}

