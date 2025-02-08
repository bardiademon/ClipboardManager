package com.bardiademon.manager.clipboard.util;

import java.io.File;

public final class Paths {

    public static final String ROOT_PATH = System.getProperty("user.dir");
    public static final String IMAGES_PATH = ROOT_PATH + File.separator + "images" + File.separator;
    public static final String CONFIG_PATH = ROOT_PATH + File.separator + "data" + File.separator + "config.json";

    private Paths() {
    }

}
