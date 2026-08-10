package com.vectorcalculator;

import java.util.Arrays;

public class Debug {
    
    public static int debug = -1; //-1 is no debug, 0 is print all debug statements

    public static void println(Object o) {
        println(0, o);
    }

    public static void println(int v, Object o) {
        if (debug == 0 || debug == v) {
            System.out.println(o);
        }
    }

    public static void println() {
        if (debug >= 0) {
            System.out.println();
        }
    }

    public static void printf(String format, Object... args) {
        printf(0, format, args);
    }

    public static void printf(int v, String format, Object... args) {
        if (debug == 0 || debug == v) {
            System.out.printf(format, args);
        }
    }

    public static void print(Object o) {
        print(0, o);
    }

    public static void print(int v, Object o) {
        if (debug == 0 || debug == v) {
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