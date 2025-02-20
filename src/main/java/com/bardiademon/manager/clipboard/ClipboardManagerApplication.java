package com.bardiademon.manager.clipboard;

import com.bardiademon.manager.clipboard.controller.ClipboardController;
import com.bardiademon.manager.clipboard.controller.DatabaseConnection;
import com.bardiademon.manager.clipboard.data.entity.ClipboardEntity;
import com.bardiademon.manager.clipboard.data.enums.UILookAndFeelType;
import com.bardiademon.manager.clipboard.data.mapper.config.ConfigMapper;
import com.bardiademon.manager.clipboard.data.mapper.config.ShortcutMapper;
import com.bardiademon.manager.clipboard.data.model.config.ConfigModel;
import com.bardiademon.manager.clipboard.manager.ClipboardManager;
import com.bardiademon.manager.clipboard.service.ClipboardService;
import com.bardiademon.manager.clipboard.util.Paths;
import com.bardiademon.manager.clipboard.view.MainFrame;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public final class ClipboardManagerApplication extends AbstractVerticle implements NativeKeyListener {

    private final static Logger logger = LogManager.getLogger(ClipboardManagerApplication.class);

    private static ConfigModel config;

    private Vertx vertx;

    private static ClipboardManagerApplication app;

    public static void main(String[] args) {
        System.out.println("bardiademon");
        app = new ClipboardManagerApplication();
        addShutdownHook("Clear App", app::clear);
        app.runApp();
    }

    private void runApp() {
        vertx = Vertx.vertx();
        vertx.deployVerticle(this);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        File dbDataPath = new File(Paths.DATA_PATH);
        if (!dbDataPath.exists() && !dbDataPath.mkdirs()) {
            throw new FileNotFoundException(dbDataPath.getAbsolutePath());
        }

        try {
            config = ConfigMapper.getConfig();
            logger.trace("Config: {}", config);
        } catch (Exception e) {
            logger.error("Failed to load config", e);
            return;
        }

        try {
            UIManager.setLookAndFeel(UILookAndFeelType.getLookAndFeel(config.theme()));
        } catch (Exception e) {
            logger.error("Failed to set ui", e);
            return;
        }

        setOnKeyManager();

        logger.trace("Starting database connection");
        DatabaseConnection.connect(vertx).onSuccess(successConnection -> {
            logger.trace("Successfully connection, Config: {}", config);
            new ClipboardController();
            startPromise.complete();
        }).onFailure(failedConnection -> {
            logger.error("Failed to connection, Config: {}", config, failedConnection);
            startPromise.fail(failedConnection);
            vertx.close();
            System.exit(-1);
        });
    }

    public static ConfigModel getConfig() {
        return config;
    }

    private void setOnKeyManager() {
        logger.trace("Registering GlobalScreen...");
        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
                addShutdownHook("Unregister native hook", () -> {
                    try {
                        GlobalScreen.unregisterNativeHook();
                    } catch (NativeHookException e) {
                        e.printStackTrace(System.err);
                    }
                });
            }
            GlobalScreen.addNativeKeyListener(this);
            logger.trace("Successfully registered GlobalScreen");
        } catch (Exception e) {
            logger.trace("Error initializing GlobalScreen", e);
        }
    }

    private boolean checkPressShortcut(int pressKey, int modifiers, int[] shortcut) {
        int last = shortcut[shortcut.length - 1];
        if (pressKey == last) {
            if (shortcut.length > 1) {
                int i, len;
                for (i = 0, len = shortcut.length - 1; i < len; i++) {
                    if ((modifiers & shortcut[i]) == 0) {
                        return false;
                    }
                }
                return i == len;
            }
            return true;
        }
        return false;
    }

    public static InputStream getResource(String path) {
        return ClipboardManagerApplication.class.getResourceAsStream(path);
    }

    private void closeApp() {
        vertx.close();
        System.exit(0);
    }

    private void clearAllClipboard() {
        ClipboardService.repository().deleteAllClipboard().onComplete(deleteAllHandler -> {
            if (deleteAllHandler.failed()) {
                logger.error("Failed to delete all clipboards");
                return;
            }
            logger.trace("Successfully delete all clipboards");
            MainFrame.update(false);
        });
    }

    private void restart() {
        logger.trace("Restarting...");
        clear();
        GlobalScreen.removeNativeKeyListener(this);
        app = new ClipboardManagerApplication();
        app.runApp();
    }

    private void clear() {
        MainFrame.dispose();
        ClipboardManager.removeManager();
        DatabaseConnection.close();
        vertx.close();
        System.gc();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {

        boolean isPress = checkPressShortcut(e.getKeyCode(), e.getModifiers(), getConfig().shortcuts().openUI());
        if (isPress) {
            logger.trace("Pressed: {}", ShortcutMapper.toString(getConfig().shortcuts().openUI()));
            MainFrame.update(true);
            return;
        }

        isPress = checkPressShortcut(e.getKeyCode(), e.getModifiers(), getConfig().shortcuts().closeApp());
        if (isPress) {
            logger.trace("Pressed: {}", ShortcutMapper.toString(getConfig().shortcuts().closeApp()));
            closeApp();
            return;
        }

        isPress = checkPressShortcut(e.getKeyCode(), e.getModifiers(), getConfig().shortcuts().clearAllClipboard());
        if (isPress) {
            logger.trace("Pressed: {}", ShortcutMapper.toString(getConfig().shortcuts().clearAllClipboard()));
            clearAllClipboard();
            return;
        }

        isPress = checkPressShortcut(e.getKeyCode(), e.getModifiers(), getConfig().shortcuts().deleteLastData());
        if (isPress) {
            logger.trace("Pressed: {}", ShortcutMapper.toString(getConfig().shortcuts().deleteLastData()));
            ClipboardManager.manager().clearLastData();
            return;
        }

        isPress = checkPressShortcut(e.getKeyCode(), e.getModifiers(), getConfig().shortcuts().clearSystemClipboard());
        if (isPress) {
            logger.trace("Pressed: {}", ShortcutMapper.toString(getConfig().shortcuts().clearSystemClipboard()));
            ClipboardManager.setClipboard(ClipboardEntity.emptyStringEntity(), true);
            return;
        }

        isPress = checkPressShortcut(e.getKeyCode(), e.getModifiers(), getConfig().shortcuts().restart());
        if (isPress) {
            logger.trace("Pressed: {}", ShortcutMapper.toString(getConfig().shortcuts().restart()));
            restart();
        }

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {

    }

    public static ClipboardManagerApplication getApp() {
        return app;
    }

    public static void addShutdownHook(String name, Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Starting shutdown -> " + name);
            runnable.run();
            System.out.println("Successfully shutdown -> " + name);
        }));
    }
}