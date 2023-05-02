/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.wll.osfechada.dao;

import com.oi.filesharing.etl.lib.dao.RepositoryControl;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WllOsFechadaControl extends RepositoryControl {

    public WllOsFechadaControl() {
        super(new WllOsFechadaDAO());
    }
}
