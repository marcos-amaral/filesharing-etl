package com.oi.filesharing.etl.lib.dao;

import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import com.oi.filesharing.etl.lib.config.TaskConfig;
import java.sql.SQLException;

public class RepositoryControl {

    private static RepositoryInterface repository;

    public RepositoryControl(RepositoryInterface repositoryImpl) {
        repository = repositoryImpl; 
    }
 
    public boolean callMailing(FileSharingConfig fileSharingConfig){
        return repository.callMailing(fileSharingConfig);
    }
    
    public TaskConfig getConfig(){
        return repository.getConfig();
    }
    
    public String monitor() throws SQLException, DbPoolFatalException{
        return repository.monitor();
    }
}
