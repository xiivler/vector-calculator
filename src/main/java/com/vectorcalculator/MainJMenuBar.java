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
    
    private JMenuItem open, save, saveAs, saveCopy, saveAsDefaults, resetToDefaults, resetToFactory, exit;

    public MainJMenuBar() {
		JMenu fileMenu = createFileMenu();
		add(fileMenu);
    }

    private JMenu createFileMenu(){
		JMenu fileJMenu = new JMenu("File");

		open = fileJMenu.add("Open...");
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
		open.addActionListener(e -> {
			JFileChooser j;
            if (p.file != null) {
                j = new JFileChooser(p.file.getParent());
            }
            else {
                j = new JFileChooser(".");
            }
            j.setDialogTitle("Choose XML File");
            j.setDialogType(JFileChooser.OPEN_DIALOG);
            j.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
            if (j.showDialog(null, "OK") == JFileChooser.APPROVE_OPTION) {
                File file = j.getSelectedFile();
                if (!VectorCalculator.saved && saveBeforeLoading(file)) {
                    VectorCalculator.saveProperties(p.file, true);
                }
                VectorCalculator.loadProperties(file, false);
                p.file = file;
            }
		});

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
			VectorCalculator.saveProperties(new File("user-defaults.xml"), false);
		});

        resetToDefaults = fileJMenu.add("Reset to User Defaults");
		resetToDefaults.addActionListener(e -> {
			VectorCalculator.loadProperties(new File("user-defaults.xml"), true);
		});

        resetToFactory = fileJMenu.add("Reset to Factory Defaults");
		resetToFactory.addActionListener(e -> {
			VectorCalculator.loadProperties(new File("factory-defaults.xml"), true);
		});

		fileJMenu.addSeparator();

		exit = fileJMenu.add("Exit");
		exit.addActionListener(e -> {
            VectorCalculator.f.dispatchEvent(new WindowEvent(VectorCalculator.f, WindowEvent.WINDOW_CLOSING));
        });

		return fileJMenu;
	}

    private File saveAsDialog() {
        JFileChooser j = new JFileChooser(".");
        j.setDialogTitle("Choose Save Location");
        j.setDialogType(JFileChooser.SAVE_DIALOG);
        if (p.file != null)
            j.setSelectedFile(p.file);
        else {
            j.setSelectedFile(new File("untitled-project.xml"));
        }
        j.setFileFilter(new FileNameExtensionFilter("XML Files", "xml"));
        if (j.showDialog(null, "OK") == JFileChooser.APPROVE_OPTION) {
            return j.getSelectedFile();
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
        return (JOptionPane.showOptionDialog(VectorCalculator.f, "\"" + p.file.getName() + "\" is unsaved. Save before opening \"" + f.getName() + "\"?",
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