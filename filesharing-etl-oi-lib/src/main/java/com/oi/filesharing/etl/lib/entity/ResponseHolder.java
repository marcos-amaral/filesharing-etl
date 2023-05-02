/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.entity;

import java.io.Serializable;


public class ResponseHolder<T> implements Serializable {
    private ResponseDetail detail;
    private T response;

    public ResponseHolder(ResponseDetail detail, T response) {
        this.detail = detail;
        this.response = response;
    }

    public ResponseDetail getDetail() {
        return detail;
    }

    public void setDetail(ResponseDetail detail) {
        this.detail = detail;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }
    
    
}
