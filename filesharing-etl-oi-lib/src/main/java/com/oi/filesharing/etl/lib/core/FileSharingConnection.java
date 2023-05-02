/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.core;

import java.io.IOException;

/**
 *
 * @author mmouraam
 */
public interface FileSharingConnection<T> {
    void diconnect() throws IOException;
    T getConnection() throws Exception;
}
