package com.trid.service;

import com.trid.model.CrosshairSettings;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageLoader {

    public static BufferedImage loadScaled(CrosshairSettings settings) throws IOException {
        File imageFile = new File(settings.getImagePath());
        if (!imageFile.exists()) {
            throw new IOException("File not found: " + settings.getImagePath());
        }

        BufferedImage original = ImageIO.read(imageFile);
        if (original == null) {
            throw new IOException("Unsupported image format or corrupted file.");
        }

        int targetWidth = settings.getWidth();
        int targetHeight = settings.getHeight();

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return scaled;
    }
}