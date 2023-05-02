/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.dao;

import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import com.oi.filesharing.etl.lib.config.TaskConfig;
import java.sql.SQLException;

/**
 *
 * @author mmouraam
 */
public interface RepositoryInterface {
    
    public boolean callMailing(FileSharingConfig fileSharingConfig);
    public TaskConfig getConfig();
    public String monitor() throws SQLException, DbPoolFatalException;
}
