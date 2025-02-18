package com.bardiademon.manager.clipboard.manager;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.Jjson.data.model.JjsonString;
import com.bardiademon.Jjson.exception.JjsonException;
import com.bardiademon.manager.clipboard.ClipboardManagerApplication;
import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.data.model.ClipboardDataModel;
import com.bardiademon.manager.clipboard.listener.OnClipboardListener;
import com.bardiademon.manager.clipboard.util.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;

import static com.bardiademon.manager.clipboard.ClipboardManagerApplication.getConfig;

public final class ClipboardManager implements ClipboardOwner {

    private static ClipboardManager CLIPBOARD_MANAGER;

    private final static Logger logger = LogManager.getLogger(ClipboardManager.class);

    private static Clipboard clipboard;

    private final OnClipboardListener onClipboardListener;
    private final ClipboardDataModel<String> lastData = new ClipboardDataModel<>();
    private final Queue<ClipboardDataModel<Object>> clipboardQueue = new ArrayDeque<>();

    private boolean doingQueue = false;

    private static final int DEFAULT_MONITOR_SLEEP = 100;

    private ClipboardManager(OnClipboardListener onClipboardListener) {
        this.onClipboardListener = onClipboardListener;
        monitorClipboard();
    }

    public static ClipboardManager manager(OnClipboardListener onClipboardListener) {
        if (CLIPBOARD_MANAGER == null) {
            CLIPBOARD_MANAGER = new ClipboardManager(onClipboardListener);
        }
        return CLIPBOARD_MANAGER;
    }

    private void monitorClipboard() {
        try {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(this);
            clipboard.setContents(contents, this);
        } catch (Exception e) {
            logger.error("Failed to set monitor clipboard", e);
            try {
                Thread.sleep(DEFAULT_MONITOR_SLEEP);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            monitorClipboard();
        }
    }


    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        try {
            Thread.sleep(DEFAULT_MONITOR_SLEEP);
            Transferable newContents = clipboard.getContents(this);
            if (newContents != null) {
                if (newContents.isDataFlavorSupported(DataFlavor.stringFlavor) && getConfig().clipboardTypes().contains(ClipboardType.STRING)) {
                    String data = (String) newContents.getTransferData(DataFlavor.stringFlavor);
                    clipboardQueue.add(new ClipboardDataModel<>(data, ClipboardType.STRING));
                    doingQueue();
                } else if (newContents.isDataFlavorSupported(DataFlavor.imageFlavor) && getConfig().clipboardTypes().contains(ClipboardType.IMAGE)) {
                    BufferedImage data = (BufferedImage) newContents.getTransferData(DataFlavor.imageFlavor);
                    clipboardQueue.add(new ClipboardDataModel<>(data, ClipboardType.IMAGE));
                    doingQueue();
                } else if (newContents.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && getConfig().clipboardTypes().contains(ClipboardType.FILE)) {
                    Object transferData = newContents.getTransferData(DataFlavor.javaFileListFlavor);
                    clipboardQueue.add(new ClipboardDataModel<>(transferData, ClipboardType.FILE));
                    doingQueue();
                }
                clipboard.setContents(newContents, this);
            }
        } catch (Exception e) {
            logger.error("Failed to handler clipboard", e);
            lastData.clear();
            monitorClipboard();
        }
    }

    private File writeImage(BufferedImage image) {
        try {
            File imagesPath = new File(Paths.IMAGES_PATH + UUID.randomUUID() + ".png");
            if (imagesPath.getParentFile().exists() || imagesPath.getParentFile().mkdirs()) {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", outputStream);
                byte[] byteArray = outputStream.toByteArray();

                String newImageHash = generateImageHash(byteArray);

                if (lastData.equalsIfNotDoSet(newImageHash, ClipboardType.IMAGE)) {
                    System.gc();
                    return null;
                }

                ImageIO.write(image, "png", imagesPath);

                System.gc();

                return imagesPath;
            }
        } catch (Exception e) {
            logger.error("Failed to write image", e);
        }
        return null;
    }

    public static void setClipboard(ClipboardEntity clipboardEntity) {
        ClipboardManagerApplication.vertx.executeBlocking(() -> {

            try {
                if (clipboardEntity == null) {
                    return null;
                }

                switch (clipboardEntity.getType()) {
                    case STRING -> clipboard.setContents(new StringSelection(clipboardEntity.getData()), null);
                    case FILE -> clipboard.setContents(new Transferable() {
                        @Override
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
                        }

                        @Override
                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            return flavor.equals(DataFlavor.javaFileListFlavor);
                        }

                        @Override
                        public Object getTransferData(DataFlavor flavor) {
                            try {
                                return JjsonArray.ofString(clipboardEntity.getData()).stream()
                                        .map(item -> (JjsonString) item)
                                        .map(item -> new File(item.original()))
                                        .toList();
                            } catch (JjsonException e) {
                                logger.error("Failed file handler", e);
                                return "";
                            }
                        }
                    }, null);
                    case IMAGE -> clipboard.setContents(new Transferable() {
                        @Override
                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[]{DataFlavor.imageFlavor};
                        }

                        @Override
                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            return flavor.equals(DataFlavor.imageFlavor) && new File(clipboardEntity.getData()).exists();
                        }

                        @Override
                        public Object getTransferData(DataFlavor flavor) {
                            try {
                                return ImageIO.read(new File(clipboardEntity.getData()));
                            } catch (IOException e) {
                                logger.error("Failed read image", e);
                                return "";
                            }
                        }
                    }, null);
                }
            } catch (Exception e) {
                logger.error("Failed handler clipboard", e);
            }

            return null;
        });
    }

    private synchronized void doingQueue() {

        if (doingQueue) {
            return;
        }

        doingQueue = true;

        ClipboardDataModel<Object> clipboardData = clipboardQueue.poll();
        if (clipboardData == null || clipboardData.getData() == null || clipboardData.getType() == null) {
            nextQueue();
            return;
        }

        if (clipboardData.getType().equals(ClipboardType.STRING)) {

            String data = (String) clipboardData.getData();

            if (lastData.equalsIfNotDoSet(data, ClipboardType.STRING)) {
                nextQueue();
                return;
            }

            onClipboardListener.onData(ClipboardType.STRING, data).onComplete(onDataHandler -> {
                nextQueue();
                onClipboardListener.onString(data);
                if (onDataHandler.failed()) {
                    logger.error("Failed to on data, Data: {}", data, onDataHandler.cause());
                } else {
                    logger.trace("Successfully on data, Data: {}", data);
                }
            });

        } else if (clipboardData.getType().equals(ClipboardType.IMAGE)) {

            BufferedImage data = (BufferedImage) clipboardData.getData();

            File imagePath = writeImage(data);
            if (imagePath == null) {
                nextQueue();
                return;
            }

            onClipboardListener.onData(ClipboardType.IMAGE, data).onComplete(onDataHandler -> {
                nextQueue();
                onClipboardListener.onImage(imagePath);
                if (onDataHandler.failed()) {
                    logger.error("Failed to on data, Data: {}", data, onDataHandler.cause());
                } else {
                    logger.trace("Successfully on data, Data: {}", data);
                }
            });

        } else if (clipboardData.getType().equals(ClipboardType.FILE)) {
            Object transferData = clipboardData.getData();
            if (!(transferData instanceof List<?> transferListData && !transferListData.isEmpty() && transferListData.getFirst() instanceof File)) {
                nextQueue();
                return;
            }

            @SuppressWarnings("unchecked")
            List<File> data = (List<File>) transferData;

            String stringData = JjsonArray.ofCollection(data.stream().map(File::getAbsolutePath).toList()).encode();
            if (lastData.equalsIfNotDoSet(stringData, ClipboardType.FILE)) {
                nextQueue();
                return;
            }

            onClipboardListener.onData(ClipboardType.FILE, data).onComplete(onDataHandler -> {
                nextQueue();
                onClipboardListener.onFile(data);
                if (onDataHandler.failed()) {
                    logger.error("Failed to on data, Data: {}", data, onDataHandler.cause());
                } else {
                    logger.trace("Successfully on data, Data: {}", data);
                }
            });

        } else {
            nextQueue();
        }
    }

    private String generateImageHash(byte[] imageBytes) throws NoSuchAlgorithmException {
        return bytesToHex(MessageDigest.getInstance("SHA-256").digest(imageBytes));
    }

    private String bytesToHex(byte[] hashBytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private synchronized void nextQueue() {
        doingQueue = false;
        if (!clipboardQueue.isEmpty()) {
            doingQueue();
        }
    }

}
