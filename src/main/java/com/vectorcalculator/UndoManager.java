package com.vectorcalculator;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoManager {
    private static final int MAX_HISTORY = 50;
    private static Deque<Properties> undoStack = new ArrayDeque<>();
    private static Deque<Properties> redoStack = new ArrayDeque<>();

    private static boolean redoing = false;

    public static synchronized void recordState() {
        if (VectorCalculator.loading) return;
        // Ensure midairs are saved into Properties before snapshot
        try {
            VectorCalculator.saveMidairs();
        } catch (Exception ex) {}
        // Capture current selected cells
        //Properties p = Properties.getInstance();
        updateSelectionState();
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

    //updates current state with what tab is selected and which cell is selected
    public static synchronized void updateSelectionState(Properties properties) {
        if (redoing)
            return;
        System.out.println("Selection state updated");
        properties.currentTab = Properties.getInstance().currentTab;
        properties.genPropertiesSelectedRow = VectorCalculator.genPropertiesTable.getSelectedRow();
        properties.genPropertiesSelectedCol = VectorCalculator.genPropertiesTable.getSelectedColumn();
        properties.movementSelectedRow = VectorCalculator.movementTable.getSelectedRow();
        properties.movementSelectedCol = VectorCalculator.movementTable.getSelectedColumn();
    }

    public static synchronized void updateSelectionState() {
        //if (!undoStack.isEmpty())
        //    updateSelectionState(undoStack.peek());
        Properties p = Properties.getInstance();
        p.lastEditTab = VectorCalculator.tabbedPane.getSelectedIndex();
        p.genPropertiesSelectedRow = VectorCalculator.genPropertiesTable.getSelectedRow();
        p.genPropertiesSelectedCol = VectorCalculator.genPropertiesTable.getSelectedColumn();
        p.movementSelectedRow = VectorCalculator.movementTable.getSelectedRow();
        p.movementSelectedCol = VectorCalculator.movementTable.getSelectedColumn();
    }

    public static synchronized void undo() {
        System.out.println(Properties.p_saved.file);
        if (!canUndo()) return;
        // Save current state to redo
        Properties current = new Properties();
        Properties.copyAttributes(Properties.getInstance(), current);
        System.out.println(current.genPropertiesSelectedRow);
        

        Properties recent = undoStack.pop();
        Properties prev = undoStack.peek();
        int currentTab = current.currentTab;
        int genPropertiesSelectedRow = current.genPropertiesSelectedRow;
        int genPropertiesSelectedCol = current.genPropertiesSelectedCol;
        int movementSelectedRow = current.movementSelectedRow;
        int movementSelectedCol = current.movementSelectedCol;
        VectorCalculator.loadProperties(prev, true);
        System.out.println("Switching to tab " + recent.currentTab);
        VectorCalculator.tabbedPane.setSelectedIndex(recent.currentTab);
        if (genPropertiesSelectedRow >= 0 && genPropertiesSelectedCol >= 0 && genPropertiesSelectedRow < VectorCalculator.genPropertiesTable.getRowCount() && genPropertiesSelectedCol < VectorCalculator.genPropertiesTable.getColumnCount()) {
            VectorCalculator.genPropertiesTable.changeSelection(genPropertiesSelectedRow, genPropertiesSelectedCol, false, false);
        }
        if (movementSelectedRow >= 0 && movementSelectedCol >= 0 && movementSelectedRow < VectorCalculator.movementTable.getRowCount() && movementSelectedCol < VectorCalculator.movementTable.getColumnCount()) {
            VectorCalculator.movementTable.changeSelection(movementSelectedRow, movementSelectedCol, false, false);
        }
        System.out.println("Now it is " + Properties.getInstance().genPropertiesSelectedRow);
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
        // current.currentTab = prev.currentTab;
        // current.genPropertiesSelectedRow = prev.genPropertiesSelectedRow;
        // current.genPropertiesSelectedCol = prev.genPropertiesSelectedCol;
        // current.movementSelectedRow = prev.movementSelectedRow;
        // current.movementSelectedCol = prev.movementSelectedCol;
        // System.out.println(prev.genPropertiesSelectedRow);
        redoStack.push(recent);
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
        System.out.println("Undo size: " + undoStack.size());
        System.out.println("Redo size: " + redoStack.size());
    }

    public static synchronized void redo() {
        if (!canRedo()) return;
        Properties current = new Properties();
        Properties.copyAttributes(Properties.getInstance(), current);
        //undoStack.push(current);

        //Properties next = redoStack.pop();
        VectorCalculator.loadProperties(redoStack.pop(), true);
        Properties snapshot = new Properties();
        Properties.copyAttributes(Properties.getInstance(), snapshot);
        undoStack.push(snapshot);
        //Properties.copyAttributes(next, Properties.getInstance());
        // boolean oldLoading = VectorCalculator.loading;
        // VectorCalculator.loading = true;
        // try {
        //     applyRestoredState();
        // } finally {
        //     VectorCalculator.loading = oldLoading;
        // }
        if (MainJMenuBar.instance != null) MainJMenuBar.instance.updateUndoRedoItems();
        System.out.println("Undo size: " + undoStack.size());
        System.out.println("Redo size: " + redoStack.size());
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
