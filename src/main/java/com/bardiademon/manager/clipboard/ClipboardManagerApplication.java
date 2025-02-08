package com.bardiademon.manager.clipboard;

import com.bardiademon.manager.clipboard.controller.ClipboardController;
import com.bardiademon.manager.clipboard.controller.DataSourceProvider;
import com.bardiademon.manager.clipboard.data.mapper.ConfigMapper;
import com.bardiademon.manager.clipboard.data.model.ConfigModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClipboardManagerApplication {

    private static final ExecutorService MAIN_EXECUTORS = Executors.newSingleThreadExecutor();

    private static ConfigModel config;

    public static void main(String[] args) {
        System.out.println("bardiademon");

        config = ConfigMapper.getConfig();
        System.out.println("Config: " + config);

        DataSourceProvider.setDataSource();

        MAIN_EXECUTORS.execute(ClipboardController::new);
        Runtime.getRuntime().addShutdownHook(new Thread(MAIN_EXECUTORS::close));
    }

    public static ConfigModel getConfig() {
        return config;
    }
}