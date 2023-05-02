/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.wll.osfechada.servlet;

import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import com.oi.filesharing.etl.lib.config.TaskConfig;
import com.oi.filesharing.etl.wll.osfechada.dao.WllOsFechadaControl;
import com.oi.filesharing.etl.wll.osfechada.task.WllOsFechadaService;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author mmouraam
 */
@WebServlet(urlPatterns = "/servlet", loadOnStartup = 1)
public class MainServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LogManager.getLogger();
    
    private static TaskConfig taskConfig;
    
    @Inject
    private WllOsFechadaService wllOsFechadaService;
    
    @Inject
    private WllOsFechadaControl wllOsFechadaControl;
    
    @Inject
    private FileSharingConfig wllOsFechadaConfig;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        taskConfig = wllOsFechadaControl.getConfig();
        System.out.println(taskConfig.toString());
        
        try {
            wllOsFechadaService.start(taskConfig);
        } catch (Exception e) {
            logger.error("",e);
        }

        System.out.println("TimerTask started");

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String parameter = request.getParameter("reload");
        
        response.getWriter().append(wllOsFechadaConfig.toString()).append("\n\n");
        
        response.getWriter().append("Reload: "+(Boolean.parseBoolean(parameter)==true)+"\n");
        response.getWriter().append("old Config: "+taskConfig+"\n");
        if(parameter!=null && Boolean.parseBoolean(parameter)){
            response.getWriter().append("Reloading...\n");
            taskConfig = wllOsFechadaControl.getConfig();
            wllOsFechadaService.stop();
            try {
                wllOsFechadaService.start(taskConfig);
                response.getWriter().append("new Config: "+taskConfig);
            } catch (Exception e) {
                logger.error("",e);
                response.getWriter().append("error loading config: "+e.getMessage());
                return;
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
