/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;


@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResponseDetail implements Serializable {
    SUCCESS(0,"Success"),
    ERROR_EMPTY_REQUEST(10,"Null parameters"),
    ERROR_NO_RULES(20,"No record found"),
    ERROR(999,"Generic error");
    
    int code;
    String message;

    private ResponseDetail(int codigo, String message) {
        this.code = codigo;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
