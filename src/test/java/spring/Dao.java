package spring;

import javax.sql.DataSource;

/**
 * @author Holger Thurow (thurow.h@gmail.com)
 * @since 07.06.17
 */
public class Dao {
    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
