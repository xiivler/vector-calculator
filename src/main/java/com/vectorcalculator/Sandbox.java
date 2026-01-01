package com.vectorcalculator;

import java.util.Arrays;

import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

public class Sandbox {
    
    public static void main(String[] args) {
        //test();
        //System.out.println(basePossible[30][26]);
        tabTest();
    }

    public static void tabTest() {
        

        JPanel panel = new JPanel();

        //Create and set up the window.
        JFrame frame = new JFrame("TabbedPaneDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        
        JComponent panel3 = makeTextPanel("Homing Cap Throw Options");
        tabbedPane.addTab("Tab 3", null, panel3,
                "Still does nothing");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
        
        JComponent panel4 = makeTextPanel(
                "Panel #4 (has a preferred size of 410 x 50).");
        panel4.setPreferredSize(new Dimension(410, 50));
        tabbedPane.addTab("Tab 4", null, panel4,
                "Does nothing at all");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);
        
        //Add the tabbed pane to this panel.
        panel.add(tabbedPane);
        
        tabbedPane.setPreferredSize(new Dimension(1000, 30));
        panel.setPreferredSize(new Dimension(1000, 1000));

        //The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        //Add content to the window.
        frame.add(panel, BorderLayout.CENTER);

        //VectorCalculator vc = new VectorCalculator();
        //VectorCalculator.main(null);

        //frame.add(VectorCalculator.all);
        
        //Display the window.
        frame.pack();
        frame.setPreferredSize(new Dimension(1000, 1000));
        frame.setVisible(true);
        
    }
        
    protected static JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }


    public static void test() {
        System.out.println("Started");
        VectorCalculator.main(null);
        int[][] preset = VectorCalculator.midairPresets[1];
        
        int[][] possible = new int[41][41];
        double[][] firstFrameDecels = new double[41][41];
        
        for (int i = 31; i <= 31; i++) {
            for (int j = 25; j <= 25; j++) {
                //try {
                if (i < 3 || j < 11) {
                    possible[i][j] = -1;
                    continue;
                }
                    //System.out.println(i + ", " + j);
                    Properties.p.initialFrames = 73;
                    preset[0][1] = i;
                    preset[1][1] = j;
                    VectorCalculator.addPreset(preset);
                    Properties.p.rightVector = false;
                    VectorMaximizer maximizer = VectorCalculator.getMaximizer();
                    maximizer.alwaysDiveTurn = true;
                    //maximizer.neverDiveTurn = true;
                    Properties.p.diveCapBounceTolerance = 0.02;
                    Properties.p.diveFirstFrameDecel = 0;
                    maximizer.firstFrameDecelIncrement = 0.1;
                    Properties.p.diveCapBounceAngle = 12;
                    maximizer.maximize();
                    if (maximizer != null) {
                        possible[i][j] = maximizer.isDiveCapBouncePossible(-1, true, true, true, true, true);
                        if (i < 40 && possible[i][j] != basePossible[i][j]) {
                            System.out.println("Difference at (" + i + ", " + j + "): " + possible[i][j] + " vs " + basePossible[i][j]);
                        }
                        firstFrameDecels[i][j] = maximizer.firstFrameDecel;
                        //System.out.println(preset[0][1] + " " + preset[1][1] + " " + maximizer.isDiveCapBouncePossible(true, true, true, true) + " " + maximizer.ctType + " " + maximizer.firstFrameDecel);
                        //System.out.println("Angle: " + Properties.p.diveCapBounceAngle);
                        //System.out.println("Type: " + maximizer.ctType);
                        //VectorDisplayWindow.generateData(maximizer.getMotions(), maximizer.getInitialAngle(), maximizer.getTargetAngle());
                        //VectorDisplayWindow.display();
                        //((DiveTurn) maximizer.getMotions()[maximizer.preCapBounceDiveIndex]).getCapBounceFrame(((ComplexVector) maximizer.getMotions()[maximizer.variableCapThrow1Index]).getCappyPosition(0));
                    }
                //}
                // catch (Exception ex) {
                //     System.out.println("Exception on (" + i + ", " + j + ")");
                //     System.out.println(ex);
                // }
            }
        }
        System.out.println("Done");
       /*  System.out.print("{");
        for (int i = 0; i < 40; i++) {
            String end = ", ";
            if (i == 39) {
                end = "}";
            }
            System.out.println(Arrays.toString(possible[i]).replace('[','{').replace(']','}') + end);
        }
        System.out.println();
        System.out.print("{");
        for (int i = 0; i < 40; i++) {
            String end = ", ";
            if (i == 39) {
                end = "}";
            }
            System.out.println(Arrays.toString(firstFrameDecels[i]).replace('[','{').replace(']','}') + end);
        } */
       
         
        //  preset[0][1] = 13;
        //  preset[1][1] = 25;
        //  VectorCalculator.addPreset(preset);
        //  VectorMaximizer maximizer = VectorCalculator.getMaximizer();
        //  //maximizer.neverDiveTurn = true;
        //  maximizer.maximize();
        //  System.out.println(preset[0][1] + " " + preset[1][1] + " " + maximizer.isDiveCapBouncePossible(true, true, true, false) + " " + maximizer.ctType);
        //  VectorDisplayWindow.generateData(maximizer, maximizer.getInitialAngle(), maximizer.getTargetAngle());
        //  VectorDisplayWindow.display();
    }

    public static void fixArray() {
        int[][] arr = null;
        for (int i = 0; i <= 7; i++) {
            for (int j = 0; j <= 40; j++) {
                arr[i][j] = -1;
            }
        }
        for (int i = 0; i <= 39; i++) {
            for (int j = 0; j <= 11; j++) {
                arr[i][j] = -1;
            }
        }
        printArray(arr);
    }

    public static void printArray(int[][] arr) {
        System.out.print("{");
        for (int i = 0; i < arr.length; i++) {
            String end = ", ";
            if (i == arr.length - 1) {
                end = "}";
            }
            System.out.println(Arrays.toString(arr[i]).replace('[','{').replace(']','}') + end);
        }
    }

    public static void printArray(boolean[][] arr) {
        System.out.print("{");
        for (int i = 0; i < arr.length; i++) {
            String end = ", ";
            if (i == arr.length - 1) {
                end = "}";
            }
            System.out.println(Arrays.toString(arr[i]).replace('[','{').replace(']','}') + end);
        }
    }

    public static void printArray(double[][] arr) {
        System.out.print("{");
        for (int i = 0; i < arr.length; i++) {
            String end = ", ";
            if (i == arr.length - 1) {
                end = "}";
            }
            System.out.println(Arrays.toString(arr[i]).replace('[','{').replace(']','}') + end);
        }
    }

    static int[][] basePossible = {{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, 0, 0, 0, 0, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 7, -1, 0, 0, 0, 0, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 6, -1, 0, 0, 0, 0, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 6, 1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 1, 0, 0, 0, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 4, 0, 0, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, 1, 0, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, 1, 0, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, 1, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 4, 1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 4, 1, 1, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 4, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, 6, -1, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, -1, -1, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 0, 6, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 6, -1, -1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, 0, 6, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, 1, 0, -1, 6, 6, 4, 1, 1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 8, 8, 8, 0, 0, 1, 0, 0, 0, 0, 0, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, 6, 6, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}, 
    {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 6, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};
}
