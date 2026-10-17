package com.trid.ui;

import com.trid.model.CrosshairSettings;
import com.trid.service.ImageLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

public class ConfigWindow extends JFrame {
    private static final String TRAY_ICON_PATH = "/tray_icon.png";

    private static final Preferences PREFS = Preferences.userNodeForPackage(ConfigWindow.class);
    private static final String PREF_IMAGE_PATH = "lastImagePath";
    private static final String PREF_WIDTH = "lastWidth";
    private static final String PREF_HEIGHT = "lastHeight";
    private static final String PREF_OFFSET_X = "lastOffsetX";
    private static final String PREF_OFFSET_Y = "lastOffsetY";
    private static final String PREF_OPACITY = "lastOpacity";

    private File selectedImageFile;
    private OverlayWindow currentOverlay;
    private TrayIcon trayIcon;

    public ConfigWindow() {
        String initialImagePath = PREFS.get(PREF_IMAGE_PATH, "");
        int initialWidth = clamp(PREFS.getInt(PREF_WIDTH, 100), 10, 1920);
        int initialHeight = clamp(PREFS.getInt(PREF_HEIGHT, 100), 10, 1080);
        int initialOffsetX = clamp(PREFS.getInt(PREF_OFFSET_X, 0), -500, 500);
        int initialOffsetY = clamp(PREFS.getInt(PREF_OFFSET_Y, 0), -500, 500);
        int initialOpacity = clamp(PREFS.getInt(PREF_OPACITY, 100), 10, 100);

        setTitle("Crosshair Overlay - Config");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel pathLabel = new JLabel("Image Path:");
        JTextField pathField = new JTextField();
        pathField.setColumns(30);
        pathField.setToolTipText("Enter a full file path or browse");
        // Pre-fill from previous run
        if (!initialImagePath.isEmpty()) {
            pathField.setText(initialImagePath);
            File f = new File(initialImagePath);
            if (f.exists() && f.isFile()) {
                selectedImageFile = f;
            }
        }
        JButton browseBtn = new JButton("Browse...");

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 1;
        add(pathLabel, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        add(pathField, gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        add(browseBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("Width (px):"), gbc);
        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(initialWidth, 10, 1920, 1));
        gbc.gridx = 1;
        add(widthSpinner, gbc);

        gbc.gridx = 2;
        add(new JLabel("X Offset:"), gbc);
        JSpinner xOffsetSpinner = new JSpinner(new SpinnerNumberModel(initialOffsetX, -500, 500, 1));
        gbc.gridx = 3;
        add(xOffsetSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Height (px):"), gbc);
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(initialHeight, 10, 1080, 1));
        gbc.gridx = 1;
        add(heightSpinner, gbc);

        gbc.gridx = 2;
        add(new JLabel("Y Offset:"), gbc);
        JSpinner yOffsetSpinner = new JSpinner(new SpinnerNumberModel(initialOffsetY, -500, 500, 1));
        gbc.gridx = 3;
        add(yOffsetSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 1;
        add(new JLabel("Opacity:"), gbc);

        JSlider opacitySlider = new JSlider(10, 100, initialOpacity);
        opacitySlider.setMajorTickSpacing(10);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setToolTipText("Crosshair transparency");
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(opacitySlider, gbc);

        JLabel opacityLabel = new JLabel(initialOpacity + "%");
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        add(opacityLabel, gbc);

        opacitySlider.addChangeListener(e -> {
            int value = opacitySlider.getValue();
            opacityLabel.setText(value + "%");
        });

        JButton saveBtn = new JButton("Apply Overlay");
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        add(saveBtn, gbc);

        browseBtn.addActionListener(e -> {
            String currentText = pathField.getText().trim();
            File startDir = null;

            if (!currentText.isEmpty()) {
                File potentialDir = new File(currentText);
                if (potentialDir.exists() && potentialDir.isDirectory()) {
                    startDir = potentialDir;
                } else {
                    File parent = potentialDir.getParentFile();
                    if (parent != null && parent.exists() && parent.isDirectory()) {
                        startDir = parent;
                    }
                }
            }

            if (startDir == null) {
                startDir = new File(System.getProperty("user.home"), "Pictures");
                if (!startDir.exists() || !startDir.isDirectory()) {
                    startDir = new File(System.getProperty("user.home"));
                }
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(startDir);
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Images (PNG, GIF, BMP, JPG)", "png", "gif", "bmp", "jpg", "jpeg"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = chooser.getSelectedFile();
                pathField.setText(selectedImageFile.getAbsolutePath());
            }
        });

        saveBtn.addActionListener(e -> {
            String path = pathField.getText().trim();
            File fileToUse = null;

            if (!path.isEmpty()) {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    fileToUse = f;
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid file path: " + path,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (selectedImageFile != null && selectedImageFile.exists()) {
                fileToUse = selectedImageFile;
            }

            if (fileToUse == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select an image or enter a valid file path.",
                        "No Image",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            selectedImageFile = fileToUse;
            pathField.setText(selectedImageFile.getAbsolutePath());

            int width = (int) widthSpinner.getValue();
            int height = (int) heightSpinner.getValue();
            float opacity = opacitySlider.getValue() / 100.0f;
            int offsetX = (int) xOffsetSpinner.getValue();
            int offsetY = (int) yOffsetSpinner.getValue();

            CrosshairSettings settings = new CrosshairSettings(
                    selectedImageFile.getAbsolutePath(),
                    width,
                    height,
                    opacity,
                    offsetX,
                    offsetY
            );

            try {
                BufferedImage img = ImageLoader.loadScaled(settings);

                if (currentOverlay != null) {
                    currentOverlay.dispose();
                }

                currentOverlay = new OverlayWindow(settings, img);
                currentOverlay.setVisible(true);

                saveSettings(settings);

                JOptionPane.showMessageDialog(this,
                        "Overlay applied! It will stay on top of games.\n" +
                                "You can now close this window (keep overlay) or exit the app.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to load image: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Window closing handler ---
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleClose();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void saveSettings(CrosshairSettings settings) {
        PREFS.put(PREF_IMAGE_PATH, settings.getImagePath());
        PREFS.putInt(PREF_WIDTH, settings.getWidth());
        PREFS.putInt(PREF_HEIGHT, settings.getHeight());
        PREFS.putInt(PREF_OFFSET_X, settings.getOffsetX());
        PREFS.putInt(PREF_OFFSET_Y, settings.getOffsetY());
        PREFS.putInt(PREF_OPACITY, Math.round(settings.getOpacity() * 100));
        try {
            PREFS.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void handleClose() {
        JDialog confirmDialog = new JDialog(this, "Exit Options", true);
        confirmDialog.setLayout(new BorderLayout());
        confirmDialog.setSize(400, 150);
        confirmDialog.setLocationRelativeTo(this);

        JPanel messagePanel = new JPanel();
        messagePanel.add(new JLabel("What would you like to do?"));
        confirmDialog.add(messagePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton exitAppBtn = new JButton("Exit App (Close Overlay)");
        JButton hideConfigBtn = new JButton("Hide Config (Keep Overlay)");
        JButton cancelBtn = new JButton("Cancel");

        exitAppBtn.addActionListener(e -> {
            confirmDialog.dispose();
            exitApplication();
        });

        hideConfigBtn.addActionListener(e -> {
            confirmDialog.dispose();
            hideConfigAndShowTray();
        });

        cancelBtn.addActionListener(e -> confirmDialog.dispose());

        buttonPanel.add(exitAppBtn);
        buttonPanel.add(hideConfigBtn);
        buttonPanel.add(cancelBtn);
        confirmDialog.add(buttonPanel, BorderLayout.CENTER);

        confirmDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        confirmDialog.setVisible(true);
    }

    private void exitApplication() {
        if (currentOverlay != null) {
            currentOverlay.dispose();
        }
        removeTrayIcon();
        dispose();
        System.exit(0);
    }

    private void hideConfigAndShowTray() {
        setVisible(false);

        if (currentOverlay == null) {
            JOptionPane.showMessageDialog(this,
                    "No overlay is currently running. The config window will be hidden.",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        try {
            if (!SystemTray.isSupported()) {
                JOptionPane.showMessageDialog(this,
                        "System tray is not supported on this platform.",
                        "Tray Error",
                        JOptionPane.ERROR_MESSAGE);
                setVisible(true);
                return;
            }

            SystemTray tray = SystemTray.getSystemTray();

            removeTrayIcon();

            Image trayImage = loadTrayIconImage();

            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("Show Config");
            MenuItem exitItem = new MenuItem("Exit App");

            showItem.addActionListener(e -> showConfigWindow());
            exitItem.addActionListener(e -> {
                removeTrayIcon();
                exitApplication();
            });

            popup.add(showItem);
            popup.add(exitItem);

            trayIcon = new TrayIcon(trayImage, "Crosshair Overlay", popup);
            trayIcon.setImageAutoSize(true);

            trayIcon.addActionListener(e -> showConfigWindow());

            tray.add(trayIcon);

            trayIcon.displayMessage("Crosshair Overlay",
                    "Config window is hidden. Click this icon to show it again.",
                    TrayIcon.MessageType.INFO);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to create system tray icon: " + e.getMessage(),
                    "Tray Error",
                    JOptionPane.ERROR_MESSAGE);
            setVisible(true);
        }
    }

    private void removeTrayIcon() {
        if (trayIcon != null && SystemTray.isSupported()) {
            try {
                SystemTray.getSystemTray().remove(trayIcon);
            } catch (Exception ignored) {}
            trayIcon = null;
        }
    }

    private void showConfigWindow() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            toFront();
            removeTrayIcon();
        });
    }

    private Image loadTrayIconImage() {
        try (InputStream is = getClass().getResourceAsStream(TRAY_ICON_PATH)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) {
                    return img.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cant load the tray icon.");
        }

        BufferedImage defaultIcon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = defaultIcon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, 32, 32);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(14, 6, 4, 20);
        g2d.fillRect(6, 14, 20, 4);
        g2d.fillOval(13, 13, 6, 6);
        g2d.dispose();
        return defaultIcon;
    }

    @Override
    public void dispose() {
        removeTrayIcon();
        super.dispose();
    }
}