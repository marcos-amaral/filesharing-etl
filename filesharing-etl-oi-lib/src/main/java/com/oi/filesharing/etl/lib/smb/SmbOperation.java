/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.smb;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.oi.filesharing.etl.lib.core.FileSharingOperation;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mmouraam
 */
public class SmbOperation implements FileSharingOperation {

    private final SimpleDateFormat fileSuffix;
    private final String suffixSeparator;

    private final Session session;

    public SmbOperation(Session session, String fileSuffix, String suffixSeparator) {
        this.session = session;
        this.fileSuffix = new SimpleDateFormat(fileSuffix);
        this.suffixSeparator = suffixSeparator;
    }

    @Override
    public InputStream retrieveFileStream(String filePath, String fileName, int dateOffset) throws IOException {

        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, dateOffset);
        
        //System.out.println("filename: "+(filePath.substring(0, filePath.indexOf("\\"))+filePath.substring(filePath.indexOf("\\") + 1)+fileName + suffixSeparator + fileSuffix.format(calendar.getTime())));
        
        DiskShare share = (DiskShare) session.connectShare(filePath.substring(0, filePath.indexOf("\\")));

        for (FileIdBothDirectoryInformation fileIdBothDirectoryInformation : share.list(filePath.substring(filePath.indexOf("\\") + 1))) {
            //System.out.println(fileIdBothDirectoryInformation.getFileName());
            if (fileIdBothDirectoryInformation.getFileName().startsWith(fileName + suffixSeparator + fileSuffix.format(calendar.getTime()))) {
                Set<SMB2ShareAccess> shareAccess = new HashSet<>();
                shareAccess.addAll(SMB2ShareAccess.ALL);

                Set<SMB2CreateOptions> createOptions = new HashSet<>();
                createOptions.add(SMB2CreateOptions.FILE_WRITE_THROUGH);

                Set<AccessMask> accessMaskSet = new HashSet<>();
                accessMaskSet.add(AccessMask.GENERIC_READ);
                com.hierynomus.smbj.share.File file;

                file = share.openFile(filePath.substring(filePath.indexOf("\\") + 1) + "\\" + fileIdBothDirectoryInformation.getFileName(), accessMaskSet, null, shareAccess, SMB2CreateDisposition.FILE_OPEN, createOptions);
                return file.getInputStream();
            }
        }

        return null;
    }

}
