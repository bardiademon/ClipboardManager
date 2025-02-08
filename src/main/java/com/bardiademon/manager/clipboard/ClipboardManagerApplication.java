package com.bardiademon.manager.clipboard;

import com.bardiademon.manager.clipboard.controller.ClipboardController;
import com.bardiademon.manager.clipboard.controller.DataSourceProvider;
import com.bardiademon.manager.clipboard.data.mapper.ConfigMapper;
import com.bardiademon.manager.clipboard.data.model.ConfigModel;
import com.bardiademon.manager.clipboard.view.MainFrame;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClipboardManagerApplication {

    private static final ExecutorService MAIN_EXECUTORS = Executors.newSingleThreadExecutor();

    private static ConfigModel config;

    public static void main(String[] args) {
        System.out.println("bardiademon");

        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return;
        }

        setOnKeyManager();

        config = ConfigMapper.getConfig();
        System.out.println("Config: " + config);

        DataSourceProvider.setDataSource();

        MAIN_EXECUTORS.execute(ClipboardController::new);
        Runtime.getRuntime().addShutdownHook(new Thread(MAIN_EXECUTORS::close));
    }

    public static ConfigModel getConfig() {
        return config;
    }

    private static void setOnKeyManager() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            System.out.println("Error initializing GlobalScreen. Exception: " + e.getMessage());
            e.printStackTrace(System.out);
            return;
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_C && (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0) {
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
}