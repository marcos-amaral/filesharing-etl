/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.dao;

import com.oi.filesharing.etl.lib.config.TaskConfig;
import com.oi.filesharing.etl.lib.utils.Interval;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractRepositoryDAO {

    private static final Logger logger = LogManager.getLogger();

    ////////////////////////////////////////////////////////////////////////
    // CONNECTION UTILS
    ////////////////////////////////////////////////////////////////////////
    /**
     * Try to find an available connection to the NgrMonitor Database or Creates a new one if none is available
     *
     * @return Connection to the NgrMonitor Database
     * @throws DbPoolFatalException
     */
    protected abstract Connection getOracleConnection() throws DbPoolFatalException;

    protected final PreparedStatement getPreparedStatement(Connection connection, String sql, int timeout) throws SQLException {
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        prepareStatement.setQueryTimeout(timeout);
        return prepareStatement;
    }
    
    /**
     * Tests and Returns the given connection to the pool of connections.
     *
     * @param connection The connection to be returned to the pool.
     */
    protected abstract void returnConnection(Connection connection);

    /**
     * Close the given Statement, catching all errors and cleaning warnings.
     *
     * @param statement
     */
    public final void closePreparedStatement(Statement statement) {
        //logger.trace("Closing Statement");
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException | RuntimeException e) {
            logger.error("Error Closing Statement", e);
        }
    }

    /**
     * Tries to roll back the last transaction executed by the given connection, catching errors and cleaning warnings.
     *
     * @param connection
     */
    protected final void rollBackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException | RuntimeException e) {
                logger.error("SQLException RollingBack Transaction on Connection", e);
            }
            try {
                connection.clearWarnings();
            } catch (SQLException | RuntimeException e) {
                logger.error("SQLException RollingBack Transaction on Connection", e);
            }
            try {
                connection.setAutoCommit(true);
            } catch (SQLException | RuntimeException e) {
                logger.error("SQLException RollingBack Transaction on Connection", e);
            }
        }
    }

    public TaskConfig getConfig(String taskName) {
        Connection connectionOracle = null;
        PreparedStatement psOracle = null;
        ResultSet rs = null;
        TaskConfig taskConfig = new TaskConfig();

        try {
            connectionOracle = getOracleConnection();

            /////////////////////////////////////////////////////////////////////////////
            String sql = ""
                    + "SELECT "
                    + " TASK_1_HOUR,"
                    + " TASK_1_MINUTE,"
                    + " TASK_1_SECOND,"
                    + " TASK_2_HOUR,"
                    + " TASK_2_MINUTE,"
                    + " TASK_2_SECOND,"
                    + " TASK_3_HOUR,"
                    + " TASK_3_MINUTE,"
                    + " TASK_3_SECOND, "
                    + " INTERVAL, "
                    + " TASK_DAY, "
                    + " COUNT_THRESHOLD "
                    + "FROM TERADATA_CONFIG c,TERADATA_INTERVALS i WHERE TASK_NAME = ? AND TASK_INTERVAL=i.ID";

            psOracle = connectionOracle.prepareCall(sql);
            psOracle.setString(1, taskName);
            //psOracle.setQueryTimeout(4);
            rs = psOracle.executeQuery();
            
            if(rs.next()){
                taskConfig.setTask1Hour(rs.getString("TASK_1_HOUR"));
                taskConfig.setTask1Minute(rs.getString("TASK_1_MINUTE"));
                taskConfig.setTask1Second(rs.getString("TASK_1_SECOND"));
                taskConfig.setTask2Hour(rs.getString("TASK_2_HOUR"));
                taskConfig.setTask2Minute(rs.getString("TASK_2_MINUTE"));
                taskConfig.setTask2Second(rs.getString("TASK_2_SECOND"));
                taskConfig.setTask3Hour(rs.getString("TASK_3_HOUR"));
                taskConfig.setTask3Minute(rs.getString("TASK_3_MINUTE"));
                taskConfig.setTask3Second(rs.getString("TASK_3_SECOND"));
                taskConfig.setInterval(Interval.valueOf(rs.getString("INTERVAL")));
                taskConfig.setTaskDay(rs.getString("TASK_DAY"));
                taskConfig.setCountThreshold(rs.getFloat("COUNT_THRESHOLD"));
            }
            
            taskConfig.setFeriados(getFeriados());

        } catch (SQLException e) {
            logger.error("SQLException Executing Config", e);

        } catch (DbPoolFatalException e) {
            logger.error("DbPoolFatalException Executing Config", e);

        } catch (Exception e) {
            logger.error("SQLException Executing Config", e);

        } finally {
            closePreparedStatement(psOracle);
            returnConnection(connectionOracle);
        }

        return taskConfig;
    }
    
    private List<Calendar> getFeriados() {
        Connection connectionOracle = null;
        PreparedStatement psOracle = null;
        ResultSet rs = null;
        List<Calendar> feriados = new ArrayList<>();

        try {
            connectionOracle = getOracleConnection();

            /////////////////////////////////////////////////////////////////////////////
            String sql = "SELECT FERIADO FROM TERADATA_FERIADOS";

            psOracle = connectionOracle.prepareCall(sql);
            //psOracle.setQueryTimeout(4);
            rs = psOracle.executeQuery();
            
            while(rs.next()){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(rs.getDate(1));
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                
                feriados.add(calendar);
            }

        } catch (SQLException e) {
            logger.error("SQLException Executing Config Feriados", e);

        } catch (DbPoolFatalException e) {
            logger.error("DbPoolFatalException Executing Config Feriados", e);

        } catch (Exception e) {
            logger.error("SQLException Executing Config Feriados", e);

        } finally {
            closePreparedStatement(psOracle);
            returnConnection(connectionOracle);
        }

        return feriados;
    }
    
    public String monitor(String taskName) throws SQLException, DbPoolFatalException {
        Connection connectionOracle = null;
        PreparedStatement psOracle = null;
        ResultSet rs = null;
        String status = "";

        try {
            connectionOracle = getOracleConnection();

            /////////////////////////////////////////////////////////////////////////////
            String sql = "SELECT STATUS FROM TERADATA_STATISTIC s WHERE NOME = ?";

            psOracle = connectionOracle.prepareCall(sql);
            psOracle.setString(1, taskName);
            //psOracle.setQueryTimeout(4);
            rs = psOracle.executeQuery();
            
            if(rs.next()){
                status = rs.getString("STATUS");
            }
            
        } catch (SQLException e) {
            logger.error("SQLException Executing Config", e);
            throw e;

        } catch (DbPoolFatalException e) {
            logger.error("DbPoolFatalException Executing Config", e);
            throw e;

        } catch (Exception e) {
            logger.error("SQLException Executing Config", e);
            throw e;

        } finally {
            closePreparedStatement(psOracle);
            returnConnection(connectionOracle);
        }

        return status;
    }
}
