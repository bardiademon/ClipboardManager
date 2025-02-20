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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.bardiademon.manager.clipboard.ClipboardManagerApplication.getConfig;

public final class ClipboardManager {

    private static ClipboardManager CLIPBOARD_MANAGER;

    private final static Logger logger = LogManager.getLogger(ClipboardManager.class);

    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private final OnClipboardListener onClipboardListener;
    private final ClipboardDataModel<String> lastData = new ClipboardDataModel<>();
    private final Queue<ClipboardDataModel<Object>> clipboardQueue = new ArrayDeque<>();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private boolean doingQueue = false;

    private ClipboardManager(OnClipboardListener onClipboardListener) {
        this.onClipboardListener = onClipboardListener;
        setListener();
    }

    public static ClipboardManager manager(OnClipboardListener onClipboardListener) {
        if (CLIPBOARD_MANAGER == null) {
            CLIPBOARD_MANAGER = new ClipboardManager(onClipboardListener);
        }
        return CLIPBOARD_MANAGER;
    }

    public static ClipboardManager manager() {
        return CLIPBOARD_MANAGER;
    }

    public static void removeManager() {
        if (CLIPBOARD_MANAGER != null) {
            CLIPBOARD_MANAGER.executorService.close();
            CLIPBOARD_MANAGER.clipboardQueue.clear();
            CLIPBOARD_MANAGER.lastData.clear();
            CLIPBOARD_MANAGER = null;
        }
    }

    private void setListener() {
        if (getConfig().clipboardTypes().contains(ClipboardType.STRING) || getConfig().clipboardTypes().contains(ClipboardType.FILE)) {
            executorService.scheduleAtFixedRate(this::handlerClipboard, 0, getConfig().clipboardHandlerPeriod().clipboardHandlerMills(), TimeUnit.MILLISECONDS);
        }

        if (getConfig().clipboardTypes().contains(ClipboardType.IMAGE)) {
            executorService.scheduleAtFixedRate(this::handlerImageClipboard, 0, getConfig().clipboardHandlerPeriod().clipboardImageHandlerSec(), TimeUnit.SECONDS);
        }
    }

    public void handlerClipboard() {
        try {
            Transferable newContents = clipboard.getContents(null);
            if (newContents != null) {
                if (newContents.isDataFlavorSupported(DataFlavor.stringFlavor) && getConfig().clipboardTypes().contains(ClipboardType.STRING)) {
                    String data = (String) newContents.getTransferData(DataFlavor.stringFlavor);
                    clipboardQueue.add(new ClipboardDataModel<>(data, ClipboardType.STRING));
                    doingQueue();
                } else if (newContents.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && getConfig().clipboardTypes().contains(ClipboardType.FILE)) {
                    Object transferData = newContents.getTransferData(DataFlavor.javaFileListFlavor);
                    clipboardQueue.add(new ClipboardDataModel<>(transferData, ClipboardType.FILE));
                    doingQueue();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to handler clipboard", e);
            lastData.clear();
        }
    }

    public void handlerImageClipboard() {
        try {
            Transferable newContents = clipboard.getContents(null);
            if (newContents != null) {
                if (newContents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    BufferedImage data = (BufferedImage) newContents.getTransferData(DataFlavor.imageFlavor);
                    clipboardQueue.add(new ClipboardDataModel<>(data, ClipboardType.IMAGE));
                    doingQueue();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to handler clipboard", e);
            lastData.clear();
        }
    }

    private File writeImage(BufferedImage image) {
        try {

            File imagesPath = new File(Paths.IMAGES_PATH + UUID.randomUUID() + ".png");
            if (imagesPath.getParentFile().exists() || imagesPath.getParentFile().mkdirs()) {

                String newImageHash = generateImageHash(image);

                if (lastData.equalsIfNotDoSet(newImageHash, ClipboardType.IMAGE)) {
                    System.gc();
                    return null;
                }

                ImageIO.write(image, "png", imagesPath);

                return imagesPath;
            }
        } catch (Exception e) {
            logger.error("Failed to write image", e);
        }
        return null;
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

    private static String generateImageHash(BufferedImage image) throws NoSuchAlgorithmException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return bytesToHex(MessageDigest.getInstance("SHA-256").digest(byteArray));
    }

    private static String bytesToHex(byte[] hashBytes) {
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

    public static void setClipboard(ClipboardEntity clipboardEntity, boolean setLastData) {
        ClipboardManagerApplication.getApp().getVertx().executeBlocking(() -> {

            try {
                if (clipboardEntity == null) {
                    return null;
                }

                switch (clipboardEntity.getType()) {
                    case STRING -> {
                        if (setLastData) ClipboardManager.manager().lastData.setLast(clipboardEntity.getData(), ClipboardType.STRING);
                        clipboard.setContents(new StringSelection(clipboardEntity.getData()), null);
                    }
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
                                List<File> data = JjsonArray.ofString(clipboardEntity.getData()).stream()
                                        .map(item -> (JjsonString) item)
                                        .map(item -> new File(item.original()))
                                        .toList();
                                if (setLastData) ClipboardManager.manager().lastData.setLast(JjsonArray.ofCollection(data.stream().map(File::getAbsolutePath).toList()).encode(), ClipboardType.FILE);
                                return data;
                            } catch (JjsonException e) {
                                logger.error("Failed file handler", e);
                                if (setLastData) ClipboardManager.manager().lastData.setLast("", ClipboardType.STRING);
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
                                BufferedImage read = ImageIO.read(new File(clipboardEntity.getData()));
                                try {
                                    if (setLastData) ClipboardManager.manager().lastData.setLast(generateImageHash(read), ClipboardType.IMAGE);
                                } catch (NoSuchAlgorithmException ignored) {
                                }
                                return read;
                            } catch (IOException e) {
                                logger.error("Failed read image", e);
                                if (setLastData) ClipboardManager.manager().lastData.setLast("", ClipboardType.STRING);
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

    public void clearLastData() {
        lastData.clear();
    }

}
