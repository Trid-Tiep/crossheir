package com.trid.ui;

import com.trid.model.CrosshairSettings;
import com.trid.util.NativeWindowUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class OverlayWindow extends JWindow {
    private final BufferedImage crosshairImage;
    private final int width;
    private final int height;
    private final float opacity;

    public OverlayWindow(CrosshairSettings settings, BufferedImage image) {
        this.crosshairImage = image;
        this.width = settings.getWidth();
        this.height = settings.getHeight();
        this.opacity = settings.getOpacity();

        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setBackground(new Color(0, 0, 0, 0));

        JPanel content = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (crosshairImage != null) {
                    g.drawImage(crosshairImage, 0, 0, width, height, this);
                } else {
                    g.setColor(Color.RED);
                    g.fillOval(width/2 - 5, height/2 - 5, 10, 10);
                }
            }
        };
        content.setOpaque(false);
        setContentPane(content);
        try {
            setOpacity(opacity);
        } catch (IllegalComponentStateException e) {
            System.err.println("Opacity not supported: " + e.getMessage());
        }
        setSize(width, height);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int screenWidth = gd.getDisplayMode().getWidth();
        int screenHeight = gd.getDisplayMode().getHeight();
        int posX = (screenWidth - width) / 2 + settings.getOffsetX();
        int posY = (screenHeight - height) / 2 + settings.getOffsetY();
        setLocation(posX, posY);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> NativeWindowUtils.enableClickThrough(OverlayWindow.this));
            }
        });
    }
}