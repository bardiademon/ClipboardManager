package com.bardiademon.manager.clipboard.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class ImageResizer {

    private ImageResizer() {
    }

    public static BufferedImage resizeWithoutCrop(BufferedImage originalImage, int targetWidth, int targetHeight) {
        double scale = Math.min((double) targetWidth / originalImage.getWidth(), (double) targetHeight / originalImage.getHeight());

        int newWidth = (int) (originalImage.getWidth() * scale);
        int newHeight = (int) (originalImage.getHeight() * scale);

        BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.SCALE_FAST);
        Graphics2D g2d = newImage.createGraphics();

        int x = (targetWidth - newWidth) / 2;
        int y = (targetHeight - newHeight) / 2;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.drawImage(originalImage, x, y, newWidth, newHeight, null);
        g2d.dispose();

        System.gc();

        return newImage;
    }

}
