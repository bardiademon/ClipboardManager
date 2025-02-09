package com.bardiademon.manager.clipboard.util;

import java.io.File;

public final class Paths {

    public static final String ROOT_PATH = System.getProperty("user.dir");
    public static final String DATA_PATH = ROOT_PATH + File.separator + "data";

    public static final String IMAGES_PATH = DATA_PATH + File.separator + "images" + File.separator;
    public static final String CONFIG_PATH = DATA_PATH + File.separator + "config.json";

    public static final String DB_NAME = "clipboard_manager.sqlite3";
    public static final String DB_PATH = DATA_PATH + File.separator + DB_NAME;

    public static final String RESOURCE_PERSIAN_FONT = "/font/MjVanilla0.ttf";
    public static final String RESOURCE_ENGLISH_FONT = "/font/NotoSansMedium.ttf";

    public static final String RESOURCE_INITIAL_SQL = "/patch/initial.sql";
    public static final String RESOURCE_DB_SQLITE3 = "/db/clipboard_manager.sqlite3";

    private Paths() {
    }

}
