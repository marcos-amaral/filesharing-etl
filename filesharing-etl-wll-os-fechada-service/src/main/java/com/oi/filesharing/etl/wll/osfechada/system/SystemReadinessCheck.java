/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.wll.osfechada.system;

import com.oi.filesharing.etl.lib.dao.DbPoolFatalException;
import com.oi.filesharing.etl.wll.osfechada.dao.WllOsFechadaControl;
import java.sql.SQLException;
import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Readiness
@ApplicationScoped
public class SystemReadinessCheck implements HealthCheck {

    private static final String READINESS_CHECK = SystemResource.class.getSimpleName()
            + " Readiness Check";

    @Inject
    @ConfigProperty(name = "inMaintenance")
    Provider<String> inMaintenance;

    @Inject
    private WllOsFechadaControl wllOsFechadaControl;
    
    @Override
    public HealthCheckResponse call() {
        if (inMaintenance != null && inMaintenance.get().equalsIgnoreCase("true")) {
            return HealthCheckResponse.down(READINESS_CHECK);
        }
        
        try {
            String status = wllOsFechadaControl.monitor();
            
            if(status!=null && !"".equals(status) && !"Success".equals(status)){
                return HealthCheckResponse.named(READINESS_CHECK)
                                    .withData("db_status", status)
                                    .status(false).build();
            }
            
        } catch (SQLException | DbPoolFatalException ex) {
            return HealthCheckResponse.named(READINESS_CHECK)
                                    .withData("db_status", ex.getMessage())
                                    .status(false).build();
        }
        
        return HealthCheckResponse.up(READINESS_CHECK);
    }

}
