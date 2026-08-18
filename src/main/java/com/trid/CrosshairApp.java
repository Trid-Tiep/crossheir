package com.trid;
import com.trid.ui.ConfigWindow;
import javax.swing.*;

public class CrosshairApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("An error occur: " + e.getMessage());
            }
            new ConfigWindow();
        });
    }
}