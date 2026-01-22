package com.vectorcalculator;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
    private static final int MAX_HISTORY = 100;
    private static Deque<Properties> undoStack = new ArrayDeque<>();
    private static Deque<Properties> redoStack = new ArrayDeque<>();

    public static synchronized void recordState(boolean clearMessage) {
        if (VectorCalculator.loading) return;
        // Capture current selected cells
        updateSelectionState();
        Properties snapshot = new Properties();
        if (!undoStack.isEmpty() && Properties.getInstance().equals(undoStack.peek())) return; //don't save state if nothing has changed
        if (clearMessage)
            VectorCalculator.clearMessage();
        Properties.copyAttributes(Properties.getInstance(), snapshot);
        undoStack.push(snapshot);
        // limit size
        while (undoStack.size() > MAX_HISTORY) undoStack.removeLast();
        // new action clears redo
        redoStack.clear();
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
        Debug.println("Recorded State");
    }

    public static synchronized void updateSelectionState() {
        Properties p = Properties.getInstance();
        p.lastEditTab = VectorCalculator.tabbedPane.getSelectedIndex();
        p.movementSelectedRow = VectorCalculator.movementTable.getSelectedRow();
        p.movementSelectedCol = VectorCalculator.movementTable.getSelectedColumn();
    }

    public static synchronized Properties currentState() {
        return undoStack.peek();
    }

    public static synchronized void undo() {

        if (!canUndo()) return;
        // Save current state to redo
        Properties current = new Properties();
        Properties.copyAttributes(Properties.getInstance(), current);
        //Debug.println(current.genPropertiesSelectedRow);
        
        VectorCalculator.clearMessage();

        Properties recent = undoStack.pop();
        Properties prev = undoStack.peek();
        int movementSelectedRow = current.movementSelectedRow;
        int movementSelectedCol = current.movementSelectedCol;
        VectorCalculator.loadProperties(prev, true);
        Debug.println("Switching to tab " + recent.currentTab);
        VectorCalculator.tabbedPane.setSelectedIndex(recent.currentTab);
        if (movementSelectedRow >= 0 && movementSelectedCol >= 0 && movementSelectedRow < VectorCalculator.movementTable.getRowCount() && movementSelectedCol < VectorCalculator.movementTable.getColumnCount()) {
            VectorCalculator.movementTable.changeSelection(movementSelectedRow, movementSelectedCol, false, false);
        }
        VectorCalculator.selectParamRow(current.selectedParam);
        redoStack.push(recent);
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
        Debug.println("Undo size: " + undoStack.size());
        Debug.println("Redo size: " + redoStack.size());
    }

    public static synchronized void redo() {
        if (!canRedo()) return;
        Properties current = new Properties();
        Properties.copyAttributes(Properties.getInstance(), current);

        VectorCalculator.clearMessage();

        VectorCalculator.loadProperties(redoStack.pop(), true);
        Properties snapshot = new Properties();
        Properties.copyAttributes(Properties.getInstance(), snapshot);
        undoStack.push(snapshot);
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
        Debug.println("Undo size: " + undoStack.size());
        Debug.println("Redo size: " + redoStack.size());
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
