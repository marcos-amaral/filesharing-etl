/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.smb;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import com.oi.filesharing.etl.lib.core.FileSharingConnection;
import com.oi.filesharing.etl.lib.dao.FtpConnectionException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author mmouraam
 */
public class SmbConnection implements FileSharingConnection<Session> {

    private final Session session;
    private final Connection connection;

    public SmbConnection(FileSharingConfig fileSharingConfig) throws IOException, FtpConnectionException {
        SmbConfig config = SmbConfig.builder()
                .withTimeout(10, TimeUnit.SECONDS) // Timeout sets Read, Write, and Transact timeouts (default is 60 seconds)
                .withSoTimeout(10, TimeUnit.SECONDS) // Socket Timeout (default is 0 seconds, blocks forever)
                .build();

        SMBClient client = new SMBClient(config);

        Instant start = Instant.now();
        connection = client.connect(fileSharingConfig.getHost());
        Instant finish = Instant.now();
        System.out.println(Duration.between(start, finish).toMillis());
        AuthenticationContext ac = new AuthenticationContext(fileSharingConfig.getUser(), fileSharingConfig.getPassword().toCharArray(), fileSharingConfig.getDomain());
        session = connection.authenticate(ac);
    }

    @Override
    public Session getConnection() throws Exception {
        if (session != null) {
            return this.session;
        } else {
            throw new Exception("Connection is not initialized");
        }
    }

    @Override
    public void diconnect() throws IOException {
        try {
            connection.close();
        } catch (final IOException f) {
            throw f;
        }
    }

}
