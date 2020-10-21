package com.ender.fix;

public class IdGenerator {
    private static int orderIdCounter     = 0;
    private static int executionIdCounter = 0;

    public static String genExecutionID() {
        return Integer.toString(executionIdCounter++);
    }

    public static String genOrderID() {
        return Integer.toString(orderIdCounter++);
    }
}
