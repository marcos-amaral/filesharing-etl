/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.ftp;

import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import com.oi.filesharing.etl.lib.core.FileSharingConnection;
import com.oi.filesharing.etl.lib.dao.FtpConnectionException;
import java.io.IOException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author mmouraam
 */
public class FtpConnection implements FileSharingConnection<FTPClient> {

    private final FTPClient ftpClient = new FTPClient();

    public FtpConnection(FileSharingConfig fileSharingConfig) throws IOException, FtpConnectionException {
        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_NT);
        ftpClient.configure(config);
        
        ftpClient.connect(fileSharingConfig.getHost());

        int reply = ftpClient.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new FtpConnectionException("FTP server refused connection.", null);
        }

        if (!ftpClient.login(fileSharingConfig.getUser(), fileSharingConfig.getPassword())) {
            ftpClient.disconnect();
            throw new FtpConnectionException("FTP server refused login.", null);
        }

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setDefaultTimeout(3000);
        ftpClient.setDataTimeout(3000);
        ftpClient.setSoTimeout(3000);
        ftpClient.setConnectTimeout(3000);
    }

    @Override
    public FTPClient getConnection() throws Exception {
        if (ftpClient != null) {
            return this.ftpClient;
        } else {
            throw new Exception("Connection is not initialized");
        }
    }

    @Override
    public void diconnect() throws IOException {
        ftpClient.noop(); // check that control connection is working OK

        ftpClient.logout();

        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (final IOException f) {
                throw f;
            }
        }
    }

}
