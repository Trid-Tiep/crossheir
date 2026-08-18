package com.trid.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.awt.Window;

public class NativeWindowUtils {

    private static final int WS_EX_TRANSPARENT = 0x00000020;

    public static void enableClickThrough(Window window) {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) {
            System.out.println("Click-through is only supported on Windows. Ignoring.");
            return;
        }

        try {
            if (!window.isDisplayable()) {
                System.err.println("Window is not displayable. Cannot set click-through.");
                return;
            }

            Pointer hwndPointer = Native.getComponentPointer(window);
            if (hwndPointer == null) {
                System.err.println("Failed to retrieve HWND via JNA. Click-through disabled.");
                return;
            }

            WinDef.HWND hWnd = new WinDef.HWND(hwndPointer);
            int currentExStyle = User32.INSTANCE.GetWindowLong(hWnd, WinUser.GWL_EXSTYLE);
            int newExStyle = currentExStyle | WS_EX_TRANSPARENT;
            User32.INSTANCE.SetWindowLong(hWnd, WinUser.GWL_EXSTYLE, newExStyle);

            System.out.println("Click-through successfully enabled for overlay.");

        } catch (Exception e) {
            System.err.println("Failed to set click-through: " + e.getMessage());
            e.printStackTrace();
        }
    }
}