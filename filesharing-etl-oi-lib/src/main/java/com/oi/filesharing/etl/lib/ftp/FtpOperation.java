/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.ftp;

import com.oi.filesharing.etl.lib.core.FileSharingOperation;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * @author mmouraam
 */
public class FtpOperation implements FileSharingOperation {

    private static final SimpleDateFormat FILE_SUFFIX = new SimpleDateFormat("yyyyMMdd");
    
    private final FTPClient ftpClient;

    public FtpOperation(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    @Override
    public InputStream retrieveFileStream(String filePath, String fileName, int dateOffset) throws IOException {

        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, dateOffset);
        
        for (final FTPFile f : ftpClient.listFiles(filePath)) {
            if (f.getName().startsWith(fileName+"_"+FILE_SUFFIX.format(calendar.getTime()))) {
                return ftpClient.retrieveFileStream(filePath + f.getName());
            }
        }
        
        return null;
    }
}
