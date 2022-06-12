package com.example.plato.element;

/**
 * @author zhaodongpo
 * create  2022/5/25 2:52 下午
 * @version 1.0
 */
public class ThreadMonitor {

    private static final ThreadMonitor MONITOR = new ThreadMonitor();

    public static ThreadMonitor getInstance() {
        return MONITOR;
    }


}
