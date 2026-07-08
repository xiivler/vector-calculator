package com.vectorcalculator;

import java.util.Arrays;

public class Debug {
    
    public static boolean debug = false;

    public static void println(Object o) {
        if (debug) {
            System.out.println(o);
        }
    }

    public static void println() {
        if (debug) {
            System.out.println();
        }
    }

    public static void printf(String format, Object... args) {
        if (debug) {
            System.out.printf(format, args);
        }
    }

    public static void print(Object o) {
        if (debug) {
            System.out.print(o);
        }
    }

    public static void printArray(int[][] arr) {
        System.out.print("{");
        for (int i = 0; i < arr.length; i++) {
            String end = ", ";
            if (i == arr.length - 1) {
                end = "}";
            }
            Debug.println(Arrays.toString(arr[i]).replace('[','{').replace(']','}') + end);
        }
    }

    public static void printArray(boolean[][] arr) {
        System.out.print("{");
        for (int i = 0; i < arr.length; i++) {
            String end = ", ";
            if (i == arr.length - 1) {
                end = "}";
            }
            Debug.println(Arrays.toString(arr[i]).replace('[','{').replace(']','}') + end);
        }
    }

    public static void printArray(double[][] arr) {
        System.out.print("{");
        for (int i = 0; i < arr.length; i++) {
            String end = ", ";
            if (i == arr.length - 1) {
                end = "}";
            }
            Debug.println(Arrays.toString(arr[i]).replace('[','{').replace(']','}') + end);
        }
    }
}