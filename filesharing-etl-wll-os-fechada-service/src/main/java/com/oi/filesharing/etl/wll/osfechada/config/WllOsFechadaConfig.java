/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.wll.osfechada.config;

import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import java.util.Arrays;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 *
 * @author mmouraam
 */
@ApplicationScoped
public class WllOsFechadaConfig implements FileSharingConfig {

    @Inject @ConfigProperty(name="filesharing_host", defaultValue = "NASPC51") 
    String host;
    
    @Inject @ConfigProperty(name="filesharing_user", defaultValue = "usr_ngrmonitor") 
    String user;
    
    @Inject @ConfigProperty(name="filesharing_password", defaultValue = "Mb3Tg4HC") 
    String password;
    
    @Inject @ConfigProperty(name="filesharing_domain", defaultValue = "OI") 
    String domain;
    
    @Inject @ConfigProperty(name="filesharing_path", defaultValue = "NAD_COBRANCA$\\AUTO_WLL\\ENTRADA") 
    String path;
    
    private String[] files;

    public WllOsFechadaConfig() {
        this.files = new String[]{"RJ.SUBNUM.WLL.FECHADA.","BA.SUBNUM.WLL.FECHADA.","MG.SUBNUM.WLL.FECHADA.","PE.SUBNUM.WLL.FECHADA."};
    }

    public String getPath() {
        return path;
    }

    public String[] getFiles() {
        return files;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "WllOsFechadaConfig{" + "host=" + host + ", user=" + user + ", password=" + password + ", domain=" + domain + ", path=" + path + ", files=" + Arrays.asList(files) + '}';
    }

    
}
