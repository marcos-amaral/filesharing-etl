/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.lib.task;

import com.oi.filesharing.etl.lib.config.TaskConfig;
import com.oi.filesharing.etl.lib.utils.Interval;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author mmouraam
 */
public abstract class AbstractService {

    protected static final Logger logger = LogManager.getLogger();

    protected static EtlTimer etlTimer1;
    protected static EtlTimer etlTimer2;
    protected static EtlTimer etlTimer3;

    public void start(TaskConfig taskConfig, Runnable runnable) throws Exception {
        if (taskConfig.getTask1Hour() != null && taskConfig.getTask1Minute() != null && taskConfig.getTask1Second() != null
                && (
                    (taskConfig.getInterval() == Interval.MINUTO && taskConfig.getTask1Minute() > 0)
                    || taskConfig.getInterval() == Interval.DIARIO
                    || ((taskConfig.getInterval() == Interval.MENSAL || taskConfig.getInterval() == Interval.MENSAL_DIAUTIL) && taskConfig.getTaskDay() != null)
                )
           ) {
            logger.debug("Start task1");
            etlTimer1 = new EtlTimer(runnable,
                    taskConfig.getTaskDay(),
                    taskConfig.getTask1Hour(),
                    taskConfig.getTask1Minute(),
                    taskConfig.getTask1Second(),
                    taskConfig.getInterval(),
                    taskConfig.getFeriados());
        }

        if (taskConfig.getTask2Hour() != null && taskConfig.getTask2Minute() != null && taskConfig.getTask2Second() != null
                && (taskConfig.getInterval() == Interval.DIARIO
                || ((taskConfig.getInterval() == Interval.MENSAL || taskConfig.getInterval() == Interval.MENSAL_DIAUTIL) && taskConfig.getTaskDay() != null))) {
            logger.debug("Start task2");
            etlTimer2 = new EtlTimer(runnable,
                    taskConfig.getTaskDay(),
                    taskConfig.getTask2Hour(),
                    taskConfig.getTask2Minute(),
                    taskConfig.getTask2Second(),
                    taskConfig.getInterval(),
                    taskConfig.getFeriados());
        }

        if (taskConfig.getTask3Hour() != null && taskConfig.getTask3Minute() != null && taskConfig.getTask3Second() != null
                && (taskConfig.getInterval() == Interval.DIARIO
                || ((taskConfig.getInterval() == Interval.MENSAL || taskConfig.getInterval() == Interval.MENSAL_DIAUTIL) && taskConfig.getTaskDay() != null))) {
            logger.debug("Start task3");
            etlTimer3 = new EtlTimer(runnable,
                    taskConfig.getTaskDay(),
                    taskConfig.getTask3Hour(),
                    taskConfig.getTask3Minute(),
                    taskConfig.getTask3Second(),
                    taskConfig.getInterval(),
                    taskConfig.getFeriados());
        }
    }

    public void stop() {
        if (etlTimer1 != null) {
            etlTimer1.cancelCurrent();
            logger.debug("task1 cancelled");
        }
        if (etlTimer2 != null) {
            etlTimer2.cancelCurrent();
            logger.debug("task2 cancelled");
        }
        if (etlTimer3 != null) {
            etlTimer3.cancelCurrent();
            logger.debug("task3 cancelled");
        }
    }

    protected class EtlTimer {

        private final Runnable task;
        private final Integer dayOfMonth;
        private final Integer hourOfDay;
        private final Integer minute;
        private final Integer second;
        private final Interval interval;
        private final List<Calendar> feriados;
        private Timer current = new Timer();

        public EtlTimer(Runnable runnable, Integer day, Integer hour, Integer minute, Integer second, Interval interval, List<Calendar> feriados) throws Exception {
            this.task = runnable;
            this.dayOfMonth = day;
            this.hourOfDay = hour;
            this.minute = minute;
            this.second = second;
            this.interval = interval;
            this.feriados = feriados;
            schedule();
        }

        private void schedule() throws Exception {
            cancelCurrent();
            current = new Timer(true);

            current.schedule(new TimerTask() {
                public void run() {
                    try {
                        task.run();
                    } finally {
                        try {
                            schedule();
                        } catch (Exception ex) {
                            logger.error("", ex);
                        }
                    }
                }
            }, nextDate());

            logger.debug("task scheduled(dayOfMonth:{},hourOfDay:{},minute:{},second:{},interval:{})", dayOfMonth, hourOfDay, minute, second, interval);
        }

        public void cancelCurrent() {
            current.cancel();
            current.purge();
        }

        private Date nextDate() throws Exception {
            Calendar runDate = Calendar.getInstance();

            switch (interval) {
                case MINUTO:
                    runDate.set(Calendar.MINUTE, runDate.get(Calendar.MINUTE) + minute);
                    break;
                case DIARIO:
                    runDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    runDate.set(Calendar.MINUTE, minute);
                    runDate.set(Calendar.SECOND, second);
                    
                    if (runDate.getTime().before(new Date())) {
                        runDate.set(Calendar.DAY_OF_MONTH, runDate.get(Calendar.DAY_OF_MONTH) + 1);
                    }
                    break;
                case MENSAL:
                    runDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    runDate.set(Calendar.MINUTE, minute);
                    runDate.set(Calendar.SECOND, second);
                    
                    runDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (runDate.getTime().before(new Date())) {
                        runDate.add(Calendar.MONTH, runDate.get(Calendar.MONTH) + 1);
                    }
                    break;
                case MENSAL_DIAUTIL:
                    runDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    runDate.set(Calendar.MINUTE, minute);
                    runDate.set(Calendar.SECOND, second);
                    
                    int countDiasUteis = 0;
                    Calendar diaUtil = Calendar.getInstance();
                    diaUtil.set(Calendar.HOUR_OF_DAY, 0);
                    diaUtil.set(Calendar.MINUTE, 0);
                    diaUtil.set(Calendar.SECOND, 0);
                    diaUtil.set(Calendar.MILLISECOND, 0);
                    for (int i = 1; i <= 31; i++) {
                        diaUtil.set(Calendar.DAY_OF_MONTH, i);
                        if (diaUtil.get(Calendar.MONTH) != Calendar.getInstance().get(Calendar.MONTH)) {
                            break;
                        }

                        if (diaUtil.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && diaUtil.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
                                && feriados != null && !feriados.contains(diaUtil)) {
                            countDiasUteis++;
                        }

                        if (countDiasUteis == dayOfMonth) {
                            break;
                        }
                    }
                    runDate.set(Calendar.DAY_OF_MONTH, diaUtil.get(Calendar.DAY_OF_MONTH));

                    if (runDate.getTime().before(new Date())) {//se o dia util ja passou, busca dia util no proximo mes
                        diaUtil.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH) + 1);

                        countDiasUteis = 0;
                        for (int i = 1; i <= 31; i++) {
                            diaUtil.set(Calendar.DAY_OF_MONTH, i);
                            if (diaUtil.get(Calendar.MONTH) != (Calendar.getInstance().get(Calendar.MONTH) + 1)) {
                                break;
                            }

                            if (diaUtil.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && diaUtil.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
                                    && feriados != null && !feriados.contains(diaUtil)) {
                                countDiasUteis++;
                            }

                            if (countDiasUteis == dayOfMonth) {
                                break;
                            }
                        }
                        runDate.set(Calendar.DAY_OF_MONTH, diaUtil.get(Calendar.DAY_OF_MONTH));
                        runDate.set(Calendar.MONTH, diaUtil.get(Calendar.MONTH));
                    }

                    if (countDiasUteis != dayOfMonth) {
                        throw new Exception("Dia útil não encontrado: " + dayOfMonth);
                    }
                    break;
            }

            logger.debug("task date: {}", runDate.getTime());

            return runDate.getTime();
        }
    }
    
    public static void main(String[] args) {
        Calendar runDate = Calendar.getInstance();
        runDate.set(Calendar.HOUR_OF_DAY, 23);
        runDate.set(Calendar.MINUTE, 59);
        runDate.set(Calendar.MINUTE, runDate.get(Calendar.MINUTE) + 5);
        
        System.out.println(runDate.getTime());
    }
}
