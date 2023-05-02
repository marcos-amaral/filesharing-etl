/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.core;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author mmouraam
 */
public interface FileSharingOperation {
    public InputStream retrieveFileStream(String filePath, String fileName, int dateOffset) throws IOException;
}
