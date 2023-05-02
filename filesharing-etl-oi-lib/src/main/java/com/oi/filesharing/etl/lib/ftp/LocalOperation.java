/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.ftp;

import com.oi.filesharing.etl.lib.core.FileSharingOperation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author mmouraam
 */
public class LocalOperation implements FileSharingOperation {

    private static final SimpleDateFormat FILE_SUFFIX = new SimpleDateFormat("yyyyMMdd");

    public LocalOperation() {
    }

    @Override
    public InputStream retrieveFileStream(String filePath, String fileName, int dateOffset) throws IOException {

        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, dateOffset);
        
        for (final File f : new File(filePath).listFiles()) {
            if (f.getName().startsWith(fileName + "_" + FILE_SUFFIX.format(calendar.getTime()))) {
                File file = new File(filePath + f.getName());
                return new FileInputStream(file);
            }
        }

        return null;
    }
}
