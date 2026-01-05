package com.vectorcalculator;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
    private static final int MAX_HISTORY = 50;
    private static Deque<Properties> undoStack = new ArrayDeque<>();
    private static Deque<Properties> redoStack = new ArrayDeque<>();

    public static synchronized void recordState() {
        if (VectorCalculator.loading) return;
        // Ensure midairs are saved into Properties before snapshot
        try {
            VectorCalculator.saveMidairs();
        } catch (Exception ex) {}
        Properties snapshot = new Properties();
        if (!undoStack.isEmpty() && Properties.getInstance().equals(undoStack.peek())) return; //don't save state if nothing has changed
        Properties.copyAttributes(Properties.getInstance(), snapshot);
        undoStack.push(snapshot);
        // limit size
        while (undoStack.size() > MAX_HISTORY) undoStack.removeLast();
        // new action clears redo
        redoStack.clear();
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
        System.out.println("Recorded State");
    }

    public static synchronized void undo() {
        if (!canUndo()) return;
        // Save current state to redo
        Properties current = new Properties();
        Properties.copyAttributes(Properties.getInstance(), current);
        redoStack.push(current);

        undoStack.pop();
        //Properties prev = undoStack.peek();
        VectorCalculator.loadProperties(undoStack.peek(), true);
        // restore into live properties
        //Properties.copyAttributes(prev, Properties.getInstance());
        // prevent listeners from recording this programmatic restore
        //boolean oldLoading = VectorCalculator.loading;
        // VectorCalculator.loading = true;
        // try {
        //     // update UI
        //     applyRestoredState();
        // } finally {
        //     VectorCalculator.loading = oldLoading;
        // }
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
    }

    public static synchronized void redo() {
        if (!canRedo()) return;
        Properties current = new Properties();
        Properties.copyAttributes(Properties.getInstance(), current);
        undoStack.push(current);

        //Properties next = redoStack.pop();
        VectorCalculator.loadProperties(redoStack.pop(), true);
        //Properties.copyAttributes(next, Properties.getInstance());
        // boolean oldLoading = VectorCalculator.loading;
        // VectorCalculator.loading = true;
        // try {
        //     applyRestoredState();
        // } finally {
        //     VectorCalculator.loading = oldLoading;
        // }
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
    }

    private static void applyRestoredState() {
        // reconstruct UI to reflect Properties
        VectorCalculator.initialMovement = new Movement(Properties.getInstance().initialMovementName, Properties.getInstance().initialHorizontalSpeed, Properties.getInstance().framesJump);
        if (Properties.getInstance().midairPreset.equals("Custom"))
            VectorCalculator.addPreset(Properties.getInstance().midairs);
        else
            VectorCalculator.addPreset(Properties.getInstance().midairPreset, true);
        System.out.println(Properties.getInstance().mode);
        VectorCalculator.refreshPropertiesRows(VectorCalculator.getRowParams(), true);
        VectorCalculator.calculateVector.setText(Properties.getInstance().mode.name);
        MainJMenuBar.updateCalculatorMenuItems();
    }

    public static synchronized boolean canUndo() {
        return undoStack.size() > 1;
    }

    public static synchronized boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public static synchronized void clear() {
        undoStack.clear();
        redoStack.clear();
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
    }
}
