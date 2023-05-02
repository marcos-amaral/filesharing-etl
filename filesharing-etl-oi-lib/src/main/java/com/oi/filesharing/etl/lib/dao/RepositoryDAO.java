package com.oi.filesharing.etl.lib.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class RepositoryDAO extends AbstractRepositoryDAO {

    private static final Logger logger = LogManager.getLogger();

    private static final boolean TEST_ENVIROMENT = false;
    
    private static final String ORACLE_DATASOURCE = "jdbc/oracledb";

    static final HashMap<String, DataSource> dataSourceList = new HashMap<>();

    static {
        try {
            DataSource dataSource;

            dataSource = (DataSource) new InitialContext().lookup(ORACLE_DATASOURCE);
            dataSourceList.put(ORACLE_DATASOURCE, dataSource);
            
        } catch (NamingException ex) {
            logger.error("Erro Obtendo DataSource", ex);
        }
    }

    public RepositoryDAO() {
    }
    
    ////////////////////////////////////////////////////////////////////////
    //CONNECTION UTILS
    ////////////////////////////////////////////////////////////////////////
    @Override
    public Connection getOracleConnection() throws DbPoolFatalException {
        try {
            if (TEST_ENVIROMENT) {
                Class.forName("oracle.jdbc.OracleDriver");
                DriverManager.setLoginTimeout(10);
                return DriverManager.getConnection("jdbc:oracle:thin:@//10.3.29.30:1521/xe", "MCA_RULES", "rules@2020");
            } else {
                DataSource ds = dataSourceList.get(ORACLE_DATASOURCE);
                if(ds != null){
                    return ds.getConnection();
                }
                else{
                    DataSource dataSource = (DataSource) new InitialContext().lookup(ORACLE_DATASOURCE);
                    dataSourceList.put(ORACLE_DATASOURCE, dataSource);
                    return dataSource.getConnection();
                }
            }
        } catch (NamingException e) {
            logger.error("Error Creating Connection", e);
            throw new DbPoolFatalException("NamingException Exception Creating Connection", e);
        } catch (SQLException e) {
            logger.error("Error Creating Connection", e);
            throw new DbPoolFatalException("SQLException Creating Connection", e);
        } catch (RuntimeException e) {
            logger.error("Error Creating Connection", e);
            throw new DbPoolFatalException("Generic Exception Creating Connection", e);
        } catch (ClassNotFoundException e) {
            logger.error("Error Creating Connection", e);
            throw new DbPoolFatalException("Enviroment Exception Creating Connection", e);
        }
    }
    
    /**
     * Tests and Returns the given connection to the pool of connections.
     *
     * @param connection The connection to be returned to the pool.
     */
    @Override
    public void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.clearWarnings();
                connection.setAutoCommit(true);
            } catch (SQLException | RuntimeException e) {
                logger.error("Error Closing Connection", e);
            }
            try {
                connection.close();
            } catch (SQLException | RuntimeException e) {
                logger.error("Error Closing Connection", e);
            }
        }
    }
}
