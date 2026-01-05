package com.vectorcalculator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

public class MainJMenuBar extends JMenuBar {
    private static final int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    Properties p = Properties.p;
    
    private JMenuItem open, save, saveAs, saveCopy, saveAsDefaults, resetToDefaults, resetToFactory, exit, newItem;
    private JMenuItem calculate, addRow, insertRow, removeRow, clearAll;
    private JMenuItem generalTab, midairTab;

    private static MainJMenuBar instance;

    public MainJMenuBar() {
		instance = this;
		JMenu fileMenu = createFileMenu();
		add(fileMenu);
		JMenu calculatorMenu = createCalculatorMenu();
		add(calculatorMenu);
		JMenu viewMenu = createViewMenu();
		add(viewMenu);
    }

    private JMenu createFileMenu(){
		JMenu fileJMenu = new JMenu("File");

        newItem = fileJMenu.add("New");
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));
		newItem.addActionListener(e -> {
			if (!VectorCalculator.saved && saveBeforeClosing()) {
				if (p.file == null) {
					File file = saveAsDialog();
					if (file != null && (!file.exists() || overwrite(file))) {
						p.file = file;
						VectorCalculator.saveProperties(p.file, true);
					}
				}
				else {
					VectorCalculator.saveProperties(p.file, true);
				}
			}
			VectorCalculator.loadProperties(VectorCalculator.userDefaults, true);
			p.file = null;
			VectorCalculator.f.setTitle("Untitled Project");
		});

		open = fileJMenu.add("Open...");
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
		open.addActionListener(e -> {
			JFileChooser j;
            if (p.file != null) {
                j = new JFileChooser(p.file.getParent());
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
                    if (p.file == null) {
                        File save_file = saveAsDialog();
                        if (save_file != null && (!save_file.exists() || overwrite(save_file))) {
                            p.file = save_file;
                            VectorCalculator.saveProperties(save_file, true);
                        }
                    }
                    else {
                        VectorCalculator.saveProperties(p.file, true);
                    }
                }
                VectorCalculator.loadProperties(file, false);
                p.file = file;
            }
		});

        fileJMenu.addSeparator();

		save = fileJMenu.add("Save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
		save.addActionListener(e -> {
			if (p.file == null) {
                File file = saveAsDialog();
                if (file != null && (!file.exists() || overwrite(file))) {
                    p.file = file;
                    VectorCalculator.saveProperties(p.file, true);
                }
            }
            else {
                VectorCalculator.saveProperties(p.file, true);
            }
		});

		saveAs = fileJMenu.add("Save As...");
		saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut | InputEvent.SHIFT_DOWN_MASK));
		saveAs.addActionListener(e -> {
			File file = saveAsDialog();
            if (file != null && (!file.exists() || overwrite(file))) {
                p.file = file;
                VectorCalculator.saveProperties(file, true);
            }
		});

		saveCopy = fileJMenu.add("Save Copy to...");
		saveCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut | InputEvent.ALT_DOWN_MASK));
		saveCopy.addActionListener(e -> {
            VectorCalculator.saveProperties(saveAsDialog(), false);
		});

        fileJMenu.addSeparator();

        saveAsDefaults = fileJMenu.add("Save As User Defaults");
		saveAsDefaults.addActionListener(e -> {
			VectorCalculator.saveProperties(VectorCalculator.userDefaults, false);
		});

        resetToDefaults = fileJMenu.add("Reset to User Defaults");
		resetToDefaults.addActionListener(e -> {
			VectorCalculator.loadProperties(VectorCalculator.userDefaults, true);
		});

        resetToFactory = fileJMenu.add("Reset to Factory Defaults");
		resetToFactory.addActionListener(e -> {
			VectorCalculator.loadProperties(VectorCalculator.factoryDefaults, true);
		});

		fileJMenu.addSeparator();

		exit = fileJMenu.add("Exit");
		exit.addActionListener(e -> {
            VectorCalculator.f.dispatchEvent(new WindowEvent(VectorCalculator.f, WindowEvent.WINDOW_CLOSING));
        });

		return fileJMenu;
	}

	private JMenu createCalculatorMenu() {
		JMenu calculatorJMenu = new JMenu("Calculator");

		calculate = calculatorJMenu.add(VectorCalculator.calculateVector.getText());
		calculate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));
		calculate.addActionListener(e -> {
			// Trigger the same action as the calculate button
			VectorCalculator.calculateVector.doClick();
		});

		calculatorJMenu.addSeparator();

		addRow = calculatorJMenu.add("Add Midair");
		addRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut)); // Ctrl+= for add
		addRow.addActionListener(e -> {
			if (p.midairPreset.equals("Custom")) {
				VectorCalculator.movementModel.addRow(VectorCalculator.movementRows);
			}
		});

		insertRow = calculatorJMenu.add("Insert Midair");
		insertRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut | InputEvent.SHIFT_DOWN_MASK)); // Ctrl+Shift+= for insert
		insertRow.addActionListener(e -> {
			if (p.midairPreset.equals("Custom")) {
				int[] selectedRows = VectorCalculator.movementTable.getSelectedRows();
				if (selectedRows.length > 0) {
					int insertIndex = selectedRows[0];
					VectorCalculator.movementModel.insertRow(insertIndex, VectorCalculator.movementRows);
					VectorCalculator.movementTable.setRowSelectionInterval(insertIndex, insertIndex);
				}
			}
		});

		removeRow = calculatorJMenu.add("Remove Midair");
		removeRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, shortcut)); // Ctrl+- for remove
		removeRow.addActionListener(e -> {
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
		});

		clearAll = calculatorJMenu.add("Clear All Midairs");
		clearAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, shortcut)); // Ctrl+0 for clear
		clearAll.addActionListener(e -> {
			if (p.midairPreset.equals("Custom")) {
				while (VectorCalculator.movementModel.getRowCount() > 0) {
					VectorCalculator.movementModel.removeRow(0);
				}
			}
		});

		// Initially disable if not custom
		updateCalculatorMenu();

		return calculatorJMenu;
	}

	private JMenu createViewMenu() {
		JMenu viewJMenu = new JMenu("View");

		generalTab = viewJMenu.add("General Properties Tab");
		generalTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, shortcut));
		generalTab.addActionListener(e -> {
			VectorCalculator.tabbedPane.setSelectedIndex(0);
		});

		midairTab = viewJMenu.add("Midair Properties Tab");
		midairTab.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, shortcut));
		midairTab.addActionListener(e -> {
			VectorCalculator.tabbedPane.setSelectedIndex(1);
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
        if (p.file != null)
            j.setSelectedFile(p.file);
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
        if (p.file != null)
            messageStr = "\"" + p.file.getName() + "\" is unsaved. Save before opening \"" + f.getName() + "\"?";
        return (JOptionPane.showOptionDialog(VectorCalculator.f, messageStr,
            "Save?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"No", "Yes"}, 1) == 1);
    }

    private boolean saveBeforeClosing() {
        String messageStr = "Save the current project before closing?";
        if (p.file != null)
            messageStr = "Save \"" + p.file.getName() + "\" before closing?";
        return (JOptionPane.showOptionDialog(VectorCalculator.f, messageStr,
            "Save?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"No", "Yes"}, 1) == 1);
    }

    public void promptSaveAndClose() {
        if (!VectorCalculator.saved && saveBeforeClosing()) {
            if (p.file == null) {
                File file = saveAsDialog();
                if (file != null && (!file.exists() || overwrite(file))) {
                    p.file = file;
                    VectorCalculator.saveProperties(p.file, true);
                }
            }
            else {
                VectorCalculator.saveProperties(p.file, true);
            }
        }
        VectorCalculator.f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}