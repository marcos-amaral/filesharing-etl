package com.oi.filesharing.etl.wll.osfechada.dao;

import com.hierynomus.smbj.session.Session;
import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import com.oi.filesharing.etl.lib.config.TaskConfig;
import com.oi.filesharing.etl.lib.core.FileSharingConnection;
import com.oi.filesharing.etl.lib.core.FileSharingOperation;
import com.oi.filesharing.etl.lib.dao.DbPoolFatalException;
import com.oi.filesharing.etl.lib.dao.RepositoryDAO;
import com.oi.filesharing.etl.lib.dao.RepositoryInterface;
import com.oi.filesharing.etl.lib.ftp.LocalOperation;
import com.oi.filesharing.etl.lib.smb.SmbConnection;
import com.oi.filesharing.etl.lib.smb.SmbOperation;
import com.oi.filesharing.etl.wll.osfechada.config.WllOsFechadaConfig;
import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import oracle.jdbc.internal.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class WllOsFechadaDAO implements RepositoryInterface {

    private static final Logger logger = LogManager.getLogger();

    private final RepositoryDAO dao;

    private static final boolean FROM_LOCAL = false;
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy HH:mm");

    /**
     * Creates the database connection using the parameters specified in the
     * <code>application.yaml</code> file located in the <code>resources</code>
     * directory.
     */
    public WllOsFechadaDAO() {
        this.dao = new RepositoryDAO();
    }

    @Override
    public boolean callMailing(FileSharingConfig fileSharingConfig) {
        long inicio = System.currentTimeMillis();
        Connection connectionOracle = null;
        PreparedStatement psOracle = null;
        CallableStatement callableStatement = null;
        FileSharingConnection fileSharingConnection = null;
        FileSharingOperation fileSharingOperation = null;
        InputStream mailingFileFtp1 = null;
        InputStream mailingFileFtp2 = null;

        boolean success = false;

        try {
            connectionOracle = dao.getOracleConnection();

            /////////////////////////////////////////////////////////////////////////////
            String sqlCreate = "call ETL_WLLOSFECHADA_CREATE()";

            callableStatement = connectionOracle.prepareCall(sqlCreate);
            callableStatement.execute();

            dao.closePreparedStatement(callableStatement);
            /////////////////////////////////////////////////////////////////////////////
            if (!FROM_LOCAL) {

                fileSharingConnection = new SmbConnection(fileSharingConfig);
                fileSharingOperation = new SmbOperation((Session) fileSharingConnection.getConnection(),"ddMMyy","D");
            } else {
                fileSharingOperation = new LocalOperation();
            }
            /////////////////////////////////////////////////////////////////////////////
            boolean callMailingRJ = callMailingRJ(fileSharingConfig, fileSharingOperation, connectionOracle);
            /////////////////////////////////////////////////////////////////////////////
            boolean callMailingBA = callMailingBA(fileSharingConfig, fileSharingOperation, connectionOracle);
            /////////////////////////////////////////////////////////////////////////////
            boolean callMailingMG = callMailingMG(fileSharingConfig, fileSharingOperation, connectionOracle);
            /////////////////////////////////////////////////////////////////////////////
            boolean callMailingPE = callMailingPE(fileSharingConfig, fileSharingOperation, connectionOracle);
            /////////////////////////////////////////////////////////////////////////////
            logger.debug("DOING ETL_WLLOSFECHADA_DEPLOY...");

            String message = "";
            if(callMailingRJ && callMailingBA && callMailingMG && callMailingPE){
                message = "Success";
            }
            else{
                if(callMailingRJ){
                    message += "RJ: Success";
                } else {
                    message += "RJ: Error";
                }
                if(callMailingBA){
                    message += "|BA: Success";
                } else {
                    message += "|BA: Error";
                }
                if(callMailingMG){
                    message += "|MG: Success";
                } else {
                    message += "|MG: Error";
                }
                if(callMailingPE){
                    message += "|PE: Success";
                } else {
                    message += "|PE: Error";
                }
                
            }
            String sqlDeployOracle = "call ETL_WLLOSFECHADA_DEPLOY(?)";

            callableStatement = connectionOracle.prepareCall(sqlDeployOracle);
            //callableStatement.setQueryTimeout(4);
            callableStatement.setString(1, message);
            callableStatement.execute();

            logger.debug("ETL_WLLOSFECHADA_DEPLOY DONE!");

            dao.closePreparedStatement(callableStatement);
            /////////////////////////////////////////////////////////////////////////////
            
            success = true;

        } catch (Exception e) {
            logger.error("SQLException Executing Wll Os Fechada", e);
            try {
                String sqlUpdateStatisticException;
                String errorMessage = e.getLocalizedMessage();
                if (errorMessage != null && errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }

                sqlUpdateStatisticException
                        = "MERGE INTO TERADATA_STATISTIC D USING (SELECT 'WLLOSFECHADA' NOME FROM DUAL) S ON (D.NOME = S.NOME)\n"
                        + "   WHEN MATCHED THEN UPDATE SET D.DADO = (SELECT COUNT(*) FROM WLLOSFECHADA), D.STATUS = '" + errorMessage + "', D.DATA = CURRENT_TIMESTAMP\n"
                        + "   WHEN NOT MATCHED THEN INSERT (D.NOME, D.DADO, D.STATUS) VALUES ('WLLOSFECHADA', (SELECT COUNT(*) FROM WLLOSFECHADA), '" + errorMessage + "')";

                psOracle = connectionOracle.prepareCall(sqlUpdateStatisticException);
                psOracle.executeUpdate();

            } catch (Exception ex) {
                logger.error("SQLException Executing Wll Os Fechada", ex);
            }

        } finally {
            dao.closePreparedStatement(psOracle);
            dao.closePreparedStatement(callableStatement);
            dao.returnConnection(connectionOracle);
            if (fileSharingConnection != null) {
                try {
                    fileSharingConnection.diconnect();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (mailingFileFtp1 != null) {
                try {
                    mailingFileFtp1.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (mailingFileFtp2 != null) {
                try {
                    mailingFileFtp2.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        logger.info("Wll Os Fechada execution time: {}", (((System.currentTimeMillis() - inicio) / 1000) + "s"));

        return success;
    }

    @Override
    public TaskConfig getConfig() {
        return dao.getConfig("WLLOSFECHADA");
    }

    @Override
    public String monitor() throws SQLException, DbPoolFatalException {
        return dao.monitor("WLLOSFECHADA");
    }

    public boolean callMailingRJ(FileSharingConfig fileSharingConfig, FileSharingOperation fileSharingOperation, Connection connectionOracle) {
        long inicio = System.currentTimeMillis();
        PreparedStatement psOracle = null;
        CallableStatement callableStatement = null;
        InputStream mailingFileFtp1 = null;

        boolean success = false;

        try {
            /////////////////////////////////////////////////////////////////////////////
            logger.debug("INSERTING RJ DATA ON ORACLE DB(WLLOSFECHADA_RJ)...");

            mailingFileFtp1 = fileSharingOperation.retrieveFileStream(((WllOsFechadaConfig) fileSharingConfig).getPath(), ((WllOsFechadaConfig) fileSharingConfig).getFiles()[0],-1);
            Scanner sc = new Scanner(mailingFileFtp1, "UTF-8");

            String sqlInsert = "MERGE INTO WLLOSFECHADA D USING (SELECT ? TERMINAL FROM DUAL) S ON (D.TERMINAL = S.TERMINAL)\n"
                    + "   WHEN MATCHED THEN UPDATE SET D.INSERTED = CURRENT_TIMESTAMP, DT_FECHAMENTO = ?\n"
                    + "   WHEN NOT MATCHED THEN INSERT (D.TERMINAL,D.DT_FECHAMENTO) VALUES (?,?)";
            psOracle = connectionOracle.prepareStatement(sqlInsert);

            //StringBuffer sb = new StringBuffer(8000);
            int count = 1;
            int countFtp = 0;

            if (sc.hasNextLine()) {
                sc.nextLine();
            }
            while (sc.hasNextLine()) {
                countFtp++;
                String[] p = sc.nextLine().split("\\|");
                String ddd = new String();
                String terminal = new String();
                Date fechamentoOs = null;
                if (p[12] != null && p[12].trim().length() > 0) {
                    ddd = String.valueOf((p[12])).trim();
                    if(ddd.startsWith("0")) ddd = ddd.substring(1);
                }
                if (p[13] != null && p[13].trim().length() > 0) {
                    terminal = String.valueOf((p[13])).trim();
                }
                if (p[22] != null && p[22].trim().length() > 0) {
                    try {
                        fechamentoOs = sdf.parse(String.valueOf(p[22]).trim());
                        
                    } catch (Exception e) {
                    }
                }
                logger.debug((ddd+terminal) + "|" + fechamentoOs);
                psOracle.setString(1, ddd+terminal);
                psOracle.setString(3, ddd+terminal);
                if(fechamentoOs != null) {
                    psOracle.setTimestamp(2, new java.sql.Timestamp(fechamentoOs.getTime()));
                    psOracle.setTimestamp(4, new java.sql.Timestamp(fechamentoOs.getTime()));
                }
                else {
                    psOracle.setNull(2, OracleTypes.TIMESTAMP);
                    psOracle.setNull(4, OracleTypes.TIMESTAMP);
                }
                
                psOracle.addBatch();

                if (count == 1000) {
                    //System.out.println(sb.toString());

                    psOracle.clearParameters();
                    psOracle.executeLargeBatch();
                    //connectionOracle.commit();
                    dao.closePreparedStatement(psOracle);
                    psOracle = connectionOracle.prepareStatement(sqlInsert);
                    count = 0;
                }
                count++;

            }
            if (count < 1000 && count > 0) {
                psOracle.clearParameters();
                psOracle.executeLargeBatch();
            }

            logger.debug("INSERTED RJ DATA ON ORACLE DB({})", countFtp);

            dao.closePreparedStatement(psOracle);
            
            success = true;

        } catch (Exception e) {
            logger.error("SQLException Executing Wll Os Fechada", e);
            try {
                String sqlUpdateStatisticException;
                String errorMessage = e.getLocalizedMessage();
                if (errorMessage != null && errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }

                sqlUpdateStatisticException
                        = "MERGE INTO TERADATA_STATISTIC D USING (SELECT 'WLLOSFECHADA_RJ' NOME FROM DUAL) S ON (D.NOME = S.NOME)\n"
                        + "   WHEN MATCHED THEN UPDATE SET D.DADO = (SELECT COUNT(*) FROM WLLOSFECHADA), D.STATUS = 'RJ " + errorMessage + "', D.DATA = CURRENT_TIMESTAMP\n"
                        + "   WHEN NOT MATCHED THEN INSERT (D.NOME, D.DADO, D.STATUS) VALUES ('WLLOSFECHADA_RJ', (SELECT COUNT(*) FROM WLLOSFECHADA), 'RJ " + errorMessage + "')";

                psOracle = connectionOracle.prepareCall(sqlUpdateStatisticException);
                psOracle.executeUpdate();

            } catch (Exception ex) {
                logger.error("SQLException Executing Wll Os Fechada", ex);
            }

        } finally {
            dao.closePreparedStatement(psOracle);
            dao.closePreparedStatement(callableStatement);
            if (mailingFileFtp1 != null) {
                try {
                    mailingFileFtp1.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        logger.info("Wll Os Fechada RJ execution time: {}", (((System.currentTimeMillis() - inicio) / 1000) + "s"));

        return success;

    }

    public boolean callMailingBA(FileSharingConfig fileSharingConfig, FileSharingOperation fileSharingOperation, Connection connectionOracle) {
        long inicio = System.currentTimeMillis();
        PreparedStatement psOracle = null;
        CallableStatement callableStatement = null;
        InputStream mailingFileFtp1 = null;

        boolean success = false;

        try {
            /////////////////////////////////////////////////////////////////////////////
            logger.debug("INSERTING BA DATA ON ORACLE DB(WLLOSFECHADA_BA)...");

            mailingFileFtp1 = fileSharingOperation.retrieveFileStream(((WllOsFechadaConfig) fileSharingConfig).getPath(), ((WllOsFechadaConfig) fileSharingConfig).getFiles()[1],-1);
            Scanner sc = new Scanner(mailingFileFtp1, "UTF-8");

            String sqlInsert = "MERGE INTO WLLOSFECHADA D USING (SELECT ? TERMINAL FROM DUAL) S ON (D.TERMINAL = S.TERMINAL)\n"
                    + "   WHEN MATCHED THEN UPDATE SET D.INSERTED = CURRENT_TIMESTAMP, DT_FECHAMENTO = ?\n"
                    + "   WHEN NOT MATCHED THEN INSERT (D.TERMINAL,D.DT_FECHAMENTO) VALUES (?,?)";
            psOracle = connectionOracle.prepareStatement(sqlInsert);

            //StringBuffer sb = new StringBuffer(8000);
            int count = 1;
            int countFtp = 0;

            if (sc.hasNextLine()) {
                sc.nextLine();
            }
            while (sc.hasNextLine()) {
                countFtp++;
                String[] p = sc.nextLine().split("\\|");
                String ddd = new String();
                String terminal = new String();
                Date fechamentoOs = null;
                if (p[12] != null && p[12].trim().length() > 0) {
                    ddd = String.valueOf((p[12])).trim();
                    if(ddd.startsWith("0")) ddd = ddd.substring(1);
                }
                if (p[13] != null && p[13].trim().length() > 0) {
                    terminal = String.valueOf((p[13])).trim();
                }
                if (p[22] != null && p[22].trim().length() > 0) {
                    try {
                        fechamentoOs = sdf.parse(String.valueOf(p[22]).trim());
                        
                    } catch (Exception e) {
                    }
                }
                logger.debug((ddd+terminal) + "|" + fechamentoOs);
                psOracle.setString(1, ddd+terminal);
                psOracle.setString(3, ddd+terminal);
                if(fechamentoOs != null) {
                    psOracle.setTimestamp(2, new java.sql.Timestamp(fechamentoOs.getTime()));
                    psOracle.setTimestamp(4, new java.sql.Timestamp(fechamentoOs.getTime()));
                }
                else {
                    psOracle.setNull(2, OracleTypes.TIMESTAMP);
                    psOracle.setNull(4, OracleTypes.TIMESTAMP);
                }
                
                psOracle.addBatch();

                if (count == 1000) {
                    //System.out.println(sb.toString());

                    psOracle.clearParameters();
                    psOracle.executeLargeBatch();
                    //connectionOracle.commit();
                    dao.closePreparedStatement(psOracle);
                    psOracle = connectionOracle.prepareStatement(sqlInsert);
                    count = 0;
                }
                count++;

            }
            if (count < 1000 && count > 0) {
                psOracle.clearParameters();
                psOracle.executeLargeBatch();
            }

            logger.debug("INSERTED BA DATA ON ORACLE DB({})", countFtp);

            dao.closePreparedStatement(psOracle);
            
            success = true;

        } catch (Exception e) {
            logger.error("SQLException Executing Wll Os Fechada", e);
            try {
                String sqlUpdateStatisticException;
                String errorMessage = e.getLocalizedMessage();
                if (errorMessage != null && errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }

                sqlUpdateStatisticException
                        = "MERGE INTO TERADATA_STATISTIC D USING (SELECT 'WLLOSFECHADA_BA' NOME FROM DUAL) S ON (D.NOME = S.NOME)\n"
                        + "   WHEN MATCHED THEN UPDATE SET D.DADO = (SELECT COUNT(*) FROM WLLOSFECHADA), D.STATUS = 'BA " + errorMessage + "', D.DATA = CURRENT_TIMESTAMP\n"
                        + "   WHEN NOT MATCHED THEN INSERT (D.NOME, D.DADO, D.STATUS) VALUES ('WLLOSFECHADA_BA', (SELECT COUNT(*) FROM WLLOSFECHADA), 'BA " + errorMessage + "')";

                psOracle = connectionOracle.prepareCall(sqlUpdateStatisticException);
                psOracle.executeUpdate();

            } catch (Exception ex) {
                logger.error("SQLException Executing Wll Os Fechada", ex);
            }

        } finally {
            dao.closePreparedStatement(psOracle);
            dao.closePreparedStatement(callableStatement);
            if (mailingFileFtp1 != null) {
                try {
                    mailingFileFtp1.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        logger.info("Wll Os Fechada BA execution time: {}", (((System.currentTimeMillis() - inicio) / 1000) + "s"));

        return success;
    }

    public boolean callMailingMG(FileSharingConfig fileSharingConfig, FileSharingOperation fileSharingOperation, Connection connectionOracle) {
        long inicio = System.currentTimeMillis();
        PreparedStatement psOracle = null;
        CallableStatement callableStatement = null;
        InputStream mailingFileFtp1 = null;

        boolean success = false;

        try {
            /////////////////////////////////////////////////////////////////////////////
            logger.debug("INSERTING MG DATA ON ORACLE DB(WLLOSFECHADA_MG)...");

            mailingFileFtp1 = fileSharingOperation.retrieveFileStream(((WllOsFechadaConfig) fileSharingConfig).getPath(), ((WllOsFechadaConfig) fileSharingConfig).getFiles()[2],-1);
            Scanner sc = new Scanner(mailingFileFtp1, "UTF-8");

            String sqlInsert = "MERGE INTO WLLOSFECHADA D USING (SELECT ? TERMINAL FROM DUAL) S ON (D.TERMINAL = S.TERMINAL)\n"
                    + "   WHEN MATCHED THEN UPDATE SET D.INSERTED = CURRENT_TIMESTAMP, DT_FECHAMENTO = ?\n"
                    + "   WHEN NOT MATCHED THEN INSERT (D.TERMINAL,D.DT_FECHAMENTO) VALUES (?,?)";
            psOracle = connectionOracle.prepareStatement(sqlInsert);

            //StringBuffer sb = new StringBuffer(8000);
            int count = 1;
            int countFtp = 0;

            if (sc.hasNextLine()) {
                sc.nextLine();
            }
            while (sc.hasNextLine()) {
                countFtp++;
                String[] p = sc.nextLine().split("\\|");
                String ddd = new String();
                String terminal = new String();
                Date fechamentoOs = null;
                if (p[12] != null && p[12].trim().length() > 0) {
                    ddd = String.valueOf((p[12])).trim();
                    if(ddd.startsWith("0")) ddd = ddd.substring(1);
                }
                if (p[13] != null && p[13].trim().length() > 0) {
                    terminal = String.valueOf((p[13])).trim();
                }
                if (p[22] != null && p[22].trim().length() > 0) {
                    try {
                        fechamentoOs = sdf.parse(String.valueOf(p[22]).trim());
                        
                    } catch (Exception e) {
                    }
                }
                logger.debug((ddd+terminal) + "|" + fechamentoOs);
                psOracle.setString(1, ddd+terminal);
                psOracle.setString(3, ddd+terminal);
                if(fechamentoOs != null) {
                    psOracle.setTimestamp(2, new java.sql.Timestamp(fechamentoOs.getTime()));
                    psOracle.setTimestamp(4, new java.sql.Timestamp(fechamentoOs.getTime()));
                }
                else {
                    psOracle.setNull(2, OracleTypes.TIMESTAMP);
                    psOracle.setNull(4, OracleTypes.TIMESTAMP);
                }
                
                psOracle.addBatch();

                if (count == 1000) {
                    //System.out.println(sb.toString());

                    psOracle.clearParameters();
                    psOracle.executeLargeBatch();
                    //connectionOracle.commit();
                    dao.closePreparedStatement(psOracle);
                    psOracle = connectionOracle.prepareStatement(sqlInsert);
                    count = 0;
                }
                count++;

            }
            if (count < 1000 && count > 0) {
                psOracle.clearParameters();
                psOracle.executeLargeBatch();
            }

            logger.debug("INSERTED MG DATA ON ORACLE DB({})", countFtp);

            dao.closePreparedStatement(psOracle);
            
            success = true;

        } catch (Exception e) {
            logger.error("SQLException Executing Wll Os Fechada", e);
            try {
                String sqlUpdateStatisticException;
                String errorMessage = e.getLocalizedMessage();
                if (errorMessage != null && errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }

                sqlUpdateStatisticException
                        = "MERGE INTO TERADATA_STATISTIC D USING (SELECT 'WLLOSFECHADA_MG' NOME FROM DUAL) S ON (D.NOME = S.NOME)\n"
                        + "   WHEN MATCHED THEN UPDATE SET D.DADO = (SELECT COUNT(*) FROM WLLOSFECHADA), D.STATUS = 'MG " + errorMessage + "', D.DATA = CURRENT_TIMESTAMP\n"
                        + "   WHEN NOT MATCHED THEN INSERT (D.NOME, D.DADO, D.STATUS) VALUES ('WLLOSFECHADA_MG', (SELECT COUNT(*) FROM WLLOSFECHADA), 'MG " + errorMessage + "')";

                psOracle = connectionOracle.prepareCall(sqlUpdateStatisticException);
                psOracle.executeUpdate();

            } catch (Exception ex) {
                logger.error("SQLException Executing Wll Os Fechada", ex);
            }

        } finally {
            dao.closePreparedStatement(psOracle);
            dao.closePreparedStatement(callableStatement);
            if (mailingFileFtp1 != null) {
                try {
                    mailingFileFtp1.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        logger.info("Wll Os Fechada MG execution time: {}", (((System.currentTimeMillis() - inicio) / 1000) + "s"));

        return success;
    }

    public boolean callMailingPE(FileSharingConfig fileSharingConfig, FileSharingOperation fileSharingOperation, Connection connectionOracle) {
        long inicio = System.currentTimeMillis();
        PreparedStatement psOracle = null;
        CallableStatement callableStatement = null;
        InputStream mailingFileFtp1 = null;

        boolean success = false;

        try {
            /////////////////////////////////////////////////////////////////////////////
            logger.debug("INSERTING PE DATA ON ORACLE DB(WLLOSFECHADA_PE)...");

            mailingFileFtp1 = fileSharingOperation.retrieveFileStream(((WllOsFechadaConfig) fileSharingConfig).getPath(), ((WllOsFechadaConfig) fileSharingConfig).getFiles()[3],-1);
            Scanner sc = new Scanner(mailingFileFtp1, "UTF-8");

            String sqlInsert = "MERGE INTO WLLOSFECHADA D USING (SELECT ? TERMINAL FROM DUAL) S ON (D.TERMINAL = S.TERMINAL)\n"
                    + "   WHEN MATCHED THEN UPDATE SET D.INSERTED = CURRENT_TIMESTAMP, DT_FECHAMENTO = ?\n"
                    + "   WHEN NOT MATCHED THEN INSERT (D.TERMINAL,D.DT_FECHAMENTO) VALUES (?,?)";
            psOracle = connectionOracle.prepareStatement(sqlInsert);

            //StringBuffer sb = new StringBuffer(8000);
            int count = 1;
            int countFtp = 0;

            if (sc.hasNextLine()) {
                sc.nextLine();
            }
            while (sc.hasNextLine()) {
                countFtp++;
                String[] p = sc.nextLine().split("\\|");
                String ddd = new String();
                String terminal = new String();
                Date fechamentoOs = null;
                if (p[12] != null && p[12].trim().length() > 0) {
                    ddd = String.valueOf((p[12])).trim();
                    if(ddd.startsWith("0")) ddd = ddd.substring(1);
                }
                if (p[13] != null && p[13].trim().length() > 0) {
                    terminal = String.valueOf((p[13])).trim();
                }
                if (p[22] != null && p[22].trim().length() > 0) {
                    try {
                        fechamentoOs = sdf.parse(String.valueOf(p[22]).trim());
                        
                    } catch (Exception e) {
                    }
                }
                logger.debug((ddd+terminal) + "|" + fechamentoOs);
                psOracle.setString(1, ddd+terminal);
                psOracle.setString(3, ddd+terminal);
                if(fechamentoOs != null) {
                    psOracle.setTimestamp(2, new java.sql.Timestamp(fechamentoOs.getTime()));
                    psOracle.setTimestamp(4, new java.sql.Timestamp(fechamentoOs.getTime()));
                }
                else {
                    psOracle.setNull(2, OracleTypes.TIMESTAMP);
                    psOracle.setNull(4, OracleTypes.TIMESTAMP);
                }
                
                psOracle.addBatch();

                if (count == 1000) {
                    //System.out.println(sb.toString());

                    psOracle.clearParameters();
                    psOracle.executeLargeBatch();
                    //connectionOracle.commit();
                    dao.closePreparedStatement(psOracle);
                    psOracle = connectionOracle.prepareStatement(sqlInsert);
                    count = 0;
                }
                count++;

            }
            if (count < 1000 && count > 0) {
                psOracle.clearParameters();
                psOracle.executeLargeBatch();
            }

            logger.debug("INSERTED PE DATA ON ORACLE DB({})", countFtp);

            dao.closePreparedStatement(psOracle);
            
            success = true;

        } catch (Exception e) {
            logger.error("SQLException Executing Wll Os Fechada", e);
            try {
                String sqlUpdateStatisticException;
                String errorMessage = e.getLocalizedMessage();
                if (errorMessage != null && errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }

                sqlUpdateStatisticException
                        = "MERGE INTO TERADATA_STATISTIC D USING (SELECT 'WLLOSFECHADA_PE' NOME FROM DUAL) S ON (D.NOME = S.NOME)\n"
                        + "   WHEN MATCHED THEN UPDATE SET D.DADO = (SELECT COUNT(*) FROM WLLOSFECHADA), D.STATUS = 'PE " + errorMessage + "', D.DATA = CURRENT_TIMESTAMP\n"
                        + "   WHEN NOT MATCHED THEN INSERT (D.NOME, D.DADO, D.STATUS) VALUES ('WLLOSFECHADA_PE', (SELECT COUNT(*) FROM WLLOSFECHADA), 'PE " + errorMessage + "')";

                psOracle = connectionOracle.prepareCall(sqlUpdateStatisticException);
                psOracle.executeUpdate();

            } catch (Exception ex) {
                logger.error("SQLException Executing Wll Os Fechada", ex);
            }

        } finally {
            dao.closePreparedStatement(psOracle);
            dao.closePreparedStatement(callableStatement);
            if (mailingFileFtp1 != null) {
                try {
                    mailingFileFtp1.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        logger.info("Wll Os Fechada PE execution time: {}", (((System.currentTimeMillis() - inicio) / 1000) + "s"));

        return success;
        
    }

}
