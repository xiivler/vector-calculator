package com.vectorcalculator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainJMenuBar extends JMenuBar {
    private static final int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    Properties p = Properties.p;
    
    private JMenuItem open, save, saveAs, saveCopy, saveAsDefaults, resetToDefaults, resetToFactory, exit, newItem;
    private JMenuItem calculate, addRow, insertRow, removeRow, clearAll;
    private JMenuItem generalTab, midairTab;

	public static MainJMenuBar instance;
	private JMenuItem undoItem, redoItem;

    public MainJMenuBar() {
		instance = this;
		JMenu fileMenu = createFileMenu();
		add(fileMenu);
			JMenu editMenu = createEditMenu();
			add(editMenu);
		JMenu viewMenu = createViewMenu();
		add(viewMenu);
    }

	// Check whether any enabled menu item uses the given accelerator keystroke
	public boolean hasEnabledAccelerator(KeyStroke ks) {
		if (ks == null) return false;
		Component[] comps = this.getComponents();
		for (Component c : comps) {
			if (c instanceof JMenu) {
				JMenu menu = (JMenu) c;
				for (Component mc : menu.getMenuComponents()) {
					if (mc instanceof JMenuItem) {
						JMenuItem mi = (JMenuItem) mc;
						KeyStroke acc = mi.getAccelerator();
						if (acc != null && acc.getKeyCode() == ks.getKeyCode() && mi.isEnabled()) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private JMenu createEditMenu() {
		JMenu editJMenu = new JMenu("Edit");
		undoItem = editJMenu.add("Undo");
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut));
		undoItem.addActionListener(e -> {
			try {
				UndoManager.undo();
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});
		redoItem = editJMenu.add("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut));
		redoItem.addActionListener(e -> {
			try {
				UndoManager.redo();
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});
		updateUndoRedoItems();
		
		editJMenu.addSeparator();
		
		addRow = editJMenu.add("Add Midair");
		addRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut)); // Ctrl+= for add
		addRow.addActionListener(e -> {
			try {
				if (p.midairPreset.equals("Custom")) {
					VectorCalculator.movementModel.addRow(VectorCalculator.movementRows);
				}
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

		insertRow = editJMenu.add("Insert Midair");
		insertRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut | InputEvent.SHIFT_DOWN_MASK)); // Ctrl+Shift+= for insert
		insertRow.addActionListener(e -> {
			try {
				if (p.midairPreset.equals("Custom")) {
					int[] selectedRows = VectorCalculator.movementTable.getSelectedRows();
					if (selectedRows.length > 0) {
						int insertIndex = selectedRows[0];
						VectorCalculator.movementModel.insertRow(insertIndex, VectorCalculator.movementRows);
						VectorCalculator.movementTable.setRowSelectionInterval(insertIndex, insertIndex);
					}
				}
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

		removeRow = editJMenu.add("Remove Midair");
		removeRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, shortcut)); // Ctrl+- for remove
		removeRow.addActionListener(e -> {
			try {
				if (p.midairPreset.equals("Custom")) {
					VectorCalculator.movementTable.removeEditor();
					int[] rowsRemove = VectorCalculator.movementTable.getSelectedRows();
					if (rowsRemove.length > 0)
						for (int i = rowsRemove.length - 1; i >= 0; i--) {
							int removeRowIndex = rowsRemove[i];
							VectorCalculator.movementModel.removeRow(removeRowIndex);
							if (VectorCalculator.movementModel.getRowCount() > 0)
								if (removeRowIndex == VectorCalculator.movementModel.getRowCount())
									VectorCalculator.movementTable.setRowSelectionInterval(removeRowIndex - 1, removeRowIndex - 1);
								else
									VectorCalculator.movementTable.setRowSelectionInterval(removeRowIndex, removeRowIndex);
						}
				}
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

		clearAll = editJMenu.add("Clear All Midairs");
		clearAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, shortcut)); // Ctrl+0 for clear
		clearAll.addActionListener(e -> {
			try {
				if (p.midairPreset.equals("Custom")) {
					VectorCalculator.movementModel.setRowCount(0);
				}
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});
		
		// Initially disable if not custom
		updateCalculatorMenu();
		
		return editJMenu;
	}

	void updateUndoRedoItems() {
		if (undoItem != null) undoItem.setEnabled(UndoManager.canUndo());
		if (redoItem != null) redoItem.setEnabled(UndoManager.canRedo());
	}

    private JMenu createFileMenu(){
		JMenu fileJMenu = new JMenu("File");

		// calculate = fileJMenu.add(VectorCalculator.calculateVector.getText());
		// calculate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));
		// calculate.addActionListener(e -> {
		//     try {
		//         // Trigger the same action as the calculate button
		//         VectorCalculator.calculateVector.doClick();
		//     } finally {
		//         VectorCalculator.cellsEditable = true;
		//     }
		// });
		// 
		//fileJMenu.addSeparator();

        newItem = fileJMenu.add("New");
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
		newItem.addActionListener(e -> {
			try {
				if (!VectorCalculator.saved && saveBeforeClosing()) {
					if (VectorCalculator.file == null) {
						File file = saveAsDialog();
						if (file != null && (!file.exists() || overwrite(file))) {
							VectorCalculator.file = file;
							VectorCalculator.saveProperties(VectorCalculator.file, true, false);
						}
					}
					else {
						VectorCalculator.saveProperties(VectorCalculator.file, true, false);
					}
				}
				VectorCalculator.loadProperties(VectorCalculator.userDefaults, true);
				VectorCalculator.file = null;
				VectorCalculator.projectName = "Untitled Project";
				VectorCalculator.f.setTitle("Untitled Project");
				Properties.p_saved = new Properties();
				Properties.copyAttributes(p, Properties.p_saved);
				VectorCalculator.saved = true;
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

		open = fileJMenu.add("Open...");
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
		open.addActionListener(e -> {
			try {
				JFileChooser j;
                if (VectorCalculator.file != null) {
                    j = new JFileChooser(VectorCalculator.file.getParent());
                }
                else {
                    j = new JFileChooser(VectorCalculator.jarParentFolder);
                }
                j.setDialogTitle("Choose XML File");
                j.setDialogType(JFileChooser.OPEN_DIALOG);
                j.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
                if (j.showDialog(null, "OK") == JFileChooser.APPROVE_OPTION) {
                    File file = j.getSelectedFile();
                    if (!file.exists()) {
                        JOptionPane.showMessageDialog(VectorCalculator.f, "The selected file does not exist.", "File Not Found", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!VectorCalculator.saved && saveBeforeLoading(file)) {
                        if (VectorCalculator.file == null) {
                            File save_file = saveAsDialog();
                            if (save_file != null && (!save_file.exists() || overwrite(save_file))) {
                                VectorCalculator.file = save_file;
                                VectorCalculator.saveProperties(save_file, true, false);
                            }
                        }
                        else {
                            VectorCalculator.saveProperties(VectorCalculator.file, true, false);
                        }
                    }
                    VectorCalculator.loadProperties(file, false);
                    VectorCalculator.file = file;
                }
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

        fileJMenu.addSeparator();

		save = fileJMenu.add("Save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
		save.addActionListener(e -> {
			try {
				if (VectorCalculator.file == null) {
                    File file = saveAsDialog();
                    if (file != null && (!file.exists() || overwrite(file))) {
                        VectorCalculator.file = file;
                        VectorCalculator.saveProperties(VectorCalculator.file, true, false);
                    }
                }
                else {
                    VectorCalculator.saveProperties(VectorCalculator.file, true, false);
                }
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

		saveAs = fileJMenu.add("Save As...");
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut | InputEvent.SHIFT_DOWN_MASK));
		saveAs.addActionListener(e -> {
			try {
				File file = saveAsDialog();
                if (file != null && (!file.exists() || overwrite(file))) {
                    VectorCalculator.file = file;
                    VectorCalculator.saveProperties(file, true, false);
                }
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

		saveCopy = fileJMenu.add("Save Copy to...");
		saveCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut | InputEvent.ALT_DOWN_MASK));
		saveCopy.addActionListener(e -> {
			try {
				File file = saveAsDialog();
                if (file != null && (!file.exists() || overwrite(file))) {
                    VectorCalculator.saveProperties(file, false, false);
                }
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

        fileJMenu.addSeparator();

        saveAsDefaults = fileJMenu.add("Save As User Defaults");
		saveAsDefaults.addActionListener(e -> {
			try {
				VectorCalculator.saveProperties(VectorCalculator.userDefaults, false, true);
				VectorCalculator.setProgressText("Saved as user defaults");
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

        resetToDefaults = fileJMenu.add("Reset to User Defaults");
		resetToDefaults.addActionListener(e -> {
			try {
				VectorCalculator.loadUserDefaults();
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

        resetToFactory = fileJMenu.add("Reset to Program Defaults");
		resetToFactory.addActionListener(e -> {
			try {
				VectorCalculator.loadProperties(new Properties(), false);
				UndoManager.recordState(true);
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

		fileJMenu.addSeparator();

		exit = fileJMenu.add("Exit");
		exit.addActionListener(e -> {
			try {
                VectorCalculator.f.dispatchEvent(new WindowEvent(VectorCalculator.f, WindowEvent.WINDOW_CLOSING));
            } finally {
				VectorCalculator.cellsEditable = true;
			}
        });

		return fileJMenu;
	}



	private JMenu createViewMenu() {
		JMenu viewJMenu = new JMenu("View");

		generalTab = viewJMenu.add("General Properties Tab");
		generalTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, shortcut));
		generalTab.addActionListener(e -> {
			try {
				VectorCalculator.tabbedPane.setSelectedIndex(0);
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});

		midairTab = viewJMenu.add("Midair Properties Tab");
		midairTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, shortcut));
		midairTab.addActionListener(e -> {
			try {
				VectorCalculator.tabbedPane.setSelectedIndex(1);
			} finally {
				VectorCalculator.cellsEditable = true;
			}
		});
		return viewJMenu;
	}

	private void updateCalculatorMenu() {
		boolean isCustom = p.midairPreset.equals("Custom");
		addRow.setEnabled(isCustom);
		boolean hasSelectedRow = isCustom && VectorCalculator.movementTable.getSelectedRow() != -1;
		insertRow.setEnabled(hasSelectedRow);
		removeRow.setEnabled(isCustom);
		clearAll.setEnabled(isCustom);
	}

	public static void updateCalculatorMenuItems() {
		if (instance != null) {
			instance.updateCalculatorMenu();
			instance.updateCalculateText();
			instance.updateUndoRedoItems();
		}
	}

	private void updateCalculateText() {
		if (calculate != null) {
			calculate.setText(VectorCalculator.calculateVector.getText());
		}
	}

    private File saveAsDialog() {
        JFileChooser j = new JFileChooser(VectorCalculator.jarParentFolder);
        j.setDialogTitle("Choose Save Location");
        j.setDialogType(JFileChooser.SAVE_DIALOG);
        if (VectorCalculator.file != null)
            j.setSelectedFile(VectorCalculator.file);
        else {
            j.setSelectedFile(new File("untitled-project.xml"));
        }
        j.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        if (j.showDialog(null, "OK") == JFileChooser.APPROVE_OPTION) {
            File selectedFile = j.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".xml")) {
                int option = JOptionPane.showConfirmDialog(VectorCalculator.f, 
                    "The file does not have a .xml extension. Do you want to append .xml?", 
                    "Append .xml?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    String path = selectedFile.getAbsolutePath() + ".xml";
                    selectedFile = new File(path);
                }
            }
            return selectedFile;
        }
        else {
            return null;
        }
    }

    private boolean overwrite(File f) {
        return (JOptionPane.showOptionDialog(VectorCalculator.f, "\"" + f.getName() + "\" already exists. Do you want to replace it?",
            "Warning: Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Cancel", "Replace"}, null) == 1);
    }

    private boolean saveBeforeLoading(File f) {
        String messageStr = "Save the current project before opening \"" + f.getName() + "\"?";
        if (VectorCalculator.file != null)
            messageStr = "\"" + VectorCalculator.file.getName() + "\" is unsaved. Save before opening \"" + f.getName() + "\"?";
        return (JOptionPane.showOptionDialog(VectorCalculator.f, messageStr,
            "Save?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"No", "Yes"}, 1) == 1);
    }

    private boolean saveBeforeClosing() {
        String messageStr = "Save the current project before closing?";
        if (VectorCalculator.file != null)
            messageStr = "Save \"" + VectorCalculator.file.getName() + "\" before closing?";
        return (JOptionPane.showOptionDialog(VectorCalculator.f, messageStr,
            "Save?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"No", "Yes"}, 1) == 1);
    }

    public void promptSaveAndClose() {
        if (!VectorCalculator.saved && saveBeforeClosing()) {
            if (VectorCalculator.file == null) {
                File file = saveAsDialog();
                if (file != null && (!file.exists() || overwrite(file))) {
                    VectorCalculator.file = file;
                    VectorCalculator.saveProperties(VectorCalculator.file, true, false);
                }
            }
            else {
                VectorCalculator.saveProperties(VectorCalculator.file, true, false);
            }
        }
        VectorCalculator.f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}