/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.wll.osfechada.task;

import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import com.oi.filesharing.etl.lib.config.TaskConfig;
import com.oi.filesharing.etl.lib.task.AbstractService;
import com.oi.filesharing.etl.wll.osfechada.dao.WllOsFechadaControl;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author mmouraam
 */
@ApplicationScoped
public class WllOsFechadaService extends AbstractService {

    @Inject
    private WllOsFechadaControl wllOsFechadaControl;

    @Inject
    private FileSharingConfig wllOsFechadaConfig;

    public void start(TaskConfig taskConfig) throws Exception {
        super.start(taskConfig, new WllOsFechadaTask());
    }

    private class WllOsFechadaTask implements Runnable {

        private int maxTries = 3;

        public WllOsFechadaTask() {
        }

        public void run() {
            for (int i = 1;; i++) {
                if (wllOsFechadaControl.callMailing(wllOsFechadaConfig)) {
                    break;
                }

                if (i < maxTries) {
                    logger.warn("ERROR IMPORTING DATA...TRYING AGAIN!");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    break;
                }
            }
        }
    }
}
