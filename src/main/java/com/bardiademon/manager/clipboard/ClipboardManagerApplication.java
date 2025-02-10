package com.bardiademon.manager.clipboard;

import com.bardiademon.manager.clipboard.controller.ClipboardController;
import com.bardiademon.manager.clipboard.controller.DatabaseConnection;
import com.bardiademon.manager.clipboard.data.mapper.ConfigMapper;
import com.bardiademon.manager.clipboard.data.model.ConfigModel;
import com.bardiademon.manager.clipboard.util.Paths;
import com.bardiademon.manager.clipboard.view.MainFrame;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.github.kwhat.jnativehook.GlobalScreen;
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

public class ClipboardManagerApplication extends AbstractVerticle {

    private final static Logger logger = LogManager.getLogger(ClipboardManagerApplication.class);

    private static ConfigModel config;

    public static Vertx vertx;

    public static void main(String[] args) {
        System.out.println("bardiademon");
        vertx = Vertx.vertx();
        vertx.deployVerticle(new ClipboardManagerApplication());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        File dbDataPath = new File(Paths.DATA_PATH);
        if (!dbDataPath.exists() && !dbDataPath.mkdirs()) {
            throw new FileNotFoundException(dbDataPath.getAbsolutePath());
        }

        config = ConfigMapper.getConfig();
        logger.trace("Config: {}", config);

        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (Exception e) {
            e.printStackTrace(System.out);
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

    private static void setOnKeyManager() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            logger.trace("Error initializing GlobalScreen", e);
            return;
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_X && (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) {
                    MainFrame.update(true);
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {

            }
        });
    }

    public static InputStream getResource(String path) {
        return ClipboardManagerApplication.class.getResourceAsStream(path);
    }

}