package com.bardiademon.manager.clipboard.manager;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.listener.OnClipboardListener;
import com.bardiademon.manager.clipboard.util.Paths;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.bardiademon.manager.clipboard.ClipboardManagerApplication.getConfig;

public final class ClipboardManager {

    private final AtomicReference<String> lastFileData = new AtomicReference<>("");
    private final AtomicReference<String> lastStringData = new AtomicReference<>("");
    private final AtomicReference<String> lastImageData = new AtomicReference<>("");

    private static ClipboardManager CLIPBOARD_MANAGER;

    private final ScheduledExecutorService clipboardListenerExecutor = Executors.newSingleThreadScheduledExecutor();

    private final OnClipboardListener onClipboardListener;

    private ClipboardManager(OnClipboardListener onClipboardListener) {
        this.onClipboardListener = onClipboardListener;
        listener();
    }

    public static ClipboardManager manager(OnClipboardListener onClipboardListener) {
        if (CLIPBOARD_MANAGER == null) {
            CLIPBOARD_MANAGER = new ClipboardManager(onClipboardListener);
        }
        return CLIPBOARD_MANAGER;
    }

    private void listener() {
        clipboardListenerExecutor.scheduleAtFixedRate(() -> {
            try {
                Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable contents = systemClipboard.getContents(null);
                if (contents != null) {
                    if (contents.isDataFlavorSupported(DataFlavor.stringFlavor) && getConfig().clipboardTypes().contains(ClipboardType.STRING)) {
                        String data = (String) contents.getTransferData(DataFlavor.stringFlavor);
                        if (!data.equals(lastStringData.get())) {
                            lastStringData.set(data);
                            onClipboardListener.onString(data);
                        }
                    } else if (contents.isDataFlavorSupported(DataFlavor.imageFlavor) && getConfig().clipboardTypes().contains(ClipboardType.IMAGE)) {
                        Image data = (Image) contents.getTransferData(DataFlavor.imageFlavor);

                        File imagePath = writeImage(data);
                        if (imagePath != null) {
                            onClipboardListener.onImage(imagePath);
                        }
                    } else if (contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && getConfig().clipboardTypes().contains(ClipboardType.FILE)) {
                        List<File> data = (List<File>) contents.getTransferData(DataFlavor.javaFileListFlavor);
                        String stringData = JjsonArray.ofCollection(data.stream().map(File::getAbsolutePath).toList()).encode();
                        if (!stringData.equals(lastFileData.get())) {
                            lastFileData.set(stringData);
                            onClipboardListener.onFile(data);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to handler clipboard, Exception: " + e.getMessage());
                e.printStackTrace(System.out);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private File writeImage(Image image) {
        try {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            File imagesPath = new File(Paths.IMAGES_PATH + UUID.randomUUID() + ".png");
            if (imagesPath.getParentFile().exists() || imagesPath.getParentFile().mkdirs()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", outputStream);
                byte[] byteArray = outputStream.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(byteArray);

                if (base64Image.equals(lastImageData.get())) {
                    return null;
                }

                lastImageData.set(base64Image);

                Files.write(imagesPath.toPath(), byteArray);

                System.gc();

                return imagesPath;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

}
