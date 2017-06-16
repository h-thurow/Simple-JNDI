package spring;

import javax.sql.DataSource;

/**
 * Created by hot on 07.06.17.
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
