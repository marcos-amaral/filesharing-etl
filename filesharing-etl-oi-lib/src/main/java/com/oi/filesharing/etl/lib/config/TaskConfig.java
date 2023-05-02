/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.config;

import com.oi.filesharing.etl.lib.utils.Interval;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author mmouraam
 */
public class TaskConfig {

    private Integer task1Hour = 1;
    private Integer task1Minute = 0;
    private Integer task1Second = 0;
    private Integer task2Hour;
    private Integer task2Minute;
    private Integer task2Second;
    private Integer task3Hour;
    private Integer task3Minute;
    private Integer task3Second;
    private Interval interval;
    private Integer taskDay;
    private float countThreshold = 0;
    private List<Calendar> feriados;

    public Integer getTask1Hour() {
        return task1Hour;
    }

    public void setTask1Hour(Integer task1Hour) {
        this.task1Hour = task1Hour;
    }
    
    public void setTask1Hour(String task1Hour) {
        if(task1Hour!=null){
            try{
                this.task1Hour = Integer.parseInt(task1Hour);
            } catch (Exception e){}
        }
    }

    public Integer getTask1Minute() {
        return task1Minute;
    }

    public void setTask1Minute(Integer task1Minute) {
        this.task1Minute = task1Minute;
    }
    
    public void setTask1Minute(String task1Minute) {
        if(task1Hour!=null){
            try{
                this.task1Minute = Integer.parseInt(task1Minute);
            } catch (Exception e){}
        }
    }

    public Integer getTask1Second() {
        return task1Second;
    }

    public void setTask1Second(Integer task1Second) {
        this.task1Second = task1Second;
    }
    
    public void setTask1Second(String task1Second) {
        if(task1Second!=null){
            try{
                this.task1Second = Integer.parseInt(task1Second);
            } catch (Exception e){}
        }
    }

    public Integer getTask2Hour() {
        return task2Hour;
    }

    public void setTask2Hour(Integer task2Hour) {
        this.task2Hour = task2Hour;
    }
    
    public void setTask2Hour(String task2Hour) {
        if(task2Hour!=null){
            try{
                this.task2Hour = Integer.parseInt(task2Hour);
            } catch (Exception e){}
        }
    }

    public Integer getTask2Minute() {
        return task2Minute;
    }

    public void setTask2Minute(Integer task2Minute) {
        this.task2Minute = task2Minute;
    }
    
    public void setTask2Minute(String task2Minute) {
        if(task2Minute!=null){
            try{
                this.task2Minute = Integer.parseInt(task2Minute);
            } catch (Exception e){}
        }
    }

    public Integer getTask2Second() {
        return task2Second;
    }

    public void setTask2Second(Integer task2Second) {
        this.task2Second = task2Second;
    }
    
    public void setTask2Second(String task2Second) {
        if(task2Second!=null){
            try{
                this.task2Second = Integer.parseInt(task2Second);
            } catch (Exception e){}
        }
    }

    public Integer getTask3Hour() {
        return task3Hour;
    }

    public void setTask3Hour(Integer task3Hour) {
        this.task3Hour = task3Hour;
    }
    
    public void setTask3Hour(String task3Hour) {
        if(task3Hour!=null){
            try{
                this.task3Hour = Integer.parseInt(task3Hour);
            } catch (Exception e){}
        }
    }

    public Integer getTask3Minute() {
        return task3Minute;
    }

    public void setTask3Minute(Integer task3Minute) {
        this.task3Minute = task3Minute;
    }
    
    public void setTask3Minute(String task3Minute) {
        if(task3Minute!=null){
            try{
                this.task3Minute = Integer.parseInt(task3Minute);
            } catch (Exception e){}
        }
    }

    public Integer getTask3Second() {
        return task3Second;
    }

    public void setTask3Second(Integer task3Second) {
        this.task3Second = task3Second;
    }
    
    public void setTask3Second(String task3Second) {
        if(task3Second!=null){
            try{
                this.task3Second = Integer.parseInt(task3Second);
            } catch (Exception e){}
        }
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
        
        if(this.interval == Interval.DIARIO){
            taskDay = null;
        }
    }

    public Integer getTaskDay() {
        return taskDay;
    }

    public void setTaskDay(Integer taskDay) {
        this.taskDay = taskDay;
    }
    
    public void setTaskDay(String taskDay) {
        if(this.interval == Interval.DIARIO){
            taskDay = null;
        }
        else if(taskDay!=null){
            try{
                this.taskDay = Integer.parseInt(taskDay);
            } catch (Exception e){}
        }
    }

    public float getCountThreshold() {
        return countThreshold;
    }

    public void setCountThreshold(float countThreshold) {
        this.countThreshold = countThreshold;
    }

    public List<Calendar> getFeriados() {
        return feriados;
    }

    public void setFeriados(List<Calendar> feriados) {
        this.feriados = feriados;
    }

    public int getDaysInterval() {
        if (interval != null) {

            switch (interval) {
                case DIARIO:
                    return 1;
                case MENSAL:
                    return 30;
                case MENSAL_DIAUTIL:
                    return 12;
                default:
                    return 1;
            }
        }
        return 1;
    }

    @Override
    public String toString() {
        return "TaskConfig{" + "task1Hour=" + task1Hour + ", task1Minute=" + task1Minute + ", task1Second=" + task1Second + ", task2Hour=" + task2Hour + ", task2Minute=" + task2Minute + ", task2Second=" + task2Second + ", task3Hour=" + task3Hour + ", task3Minute=" + task3Minute + ", task3Second=" + task3Second + ", taskDay=" + taskDay + ", interval=" + interval + ", countThreshold=" + countThreshold + '}';
    }
    
    
}
