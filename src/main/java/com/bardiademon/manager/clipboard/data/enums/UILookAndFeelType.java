package com.bardiademon.manager.clipboard.data.enums;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.plaf.basic.BasicLookAndFeel;

public enum UILookAndFeelType {
    INTELLIJ,
    DARK,
    LIGHT,
    MAC_DARK,
    MAC_LIGHT,
    DARCULA,
    ;

    public static BasicLookAndFeel getLookAndFeel(UILookAndFeelType type) {
        return switch (type) {
            case INTELLIJ -> new FlatIntelliJLaf();
            case DARK -> new FlatDarkLaf();
            case LIGHT -> new FlatLightLaf();
            case MAC_DARK -> new FlatMacDarkLaf();
            case MAC_LIGHT -> new FlatMacLightLaf();
            case DARCULA -> new FlatDarculaLaf();
        };
    }

}
