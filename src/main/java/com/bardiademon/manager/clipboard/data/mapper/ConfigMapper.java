package com.bardiademon.manager.clipboard.data.mapper;

import com.bardiademon.Jjson.exception.JjsonException;
import com.bardiademon.Jjson.object.JjsonObject;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.data.model.ConfigModel;
import com.bardiademon.manager.clipboard.util.Paths;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import java.io.IOException;
import java.util.*;

public class ConfigMapper {

    private static final String DEFAULT_CONFIG = """
            {
              "ui_shortcut": "F7",
              "clipboard_types": [
            	"FILE", "STRING", "IMAGE"
              ],
              "clipboard_save_count": 50
            }
            """;

    private ConfigMapper() {
    }

    public static ConfigModel getConfig() {
        JjsonObject joConfig;
        try {
            joConfig = JjsonObject.ofFile(Paths.CONFIG_PATH);
            System.out.println("JoConfig: " + joConfig);
        } catch (NullPointerException | JjsonException e) {
            System.out.printf("Failed read config, Exception: %s\n", e.getMessage());
            e.printStackTrace(System.out);
            try {
                joConfig = JjsonObject.ofString(DEFAULT_CONFIG);

                joConfig.write(Paths.CONFIG_PATH, false, true);

                System.out.println("JoConfig: " + joConfig);
            } catch (JjsonException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        try {
            return new ConfigModel(
                    mapUiShortcut(joConfig.getString("ui_shortcut")),
                    joConfig.getJjsonArray("clipboard_types").stream().map(item -> ClipboardType.valueOf(item.toString())).toList(),
                    joConfig.getInteger("clipboard_save_count")
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static int[] mapUiShortcut(String uiShortcut) {
        String[] uiShortcuts = uiShortcut.split("\\+");

        if (uiShortcuts.length == 0) {
            throw new RuntimeException("Invalid ui shortcut, UiShortcut: " + uiShortcut);
        }

        int[] uiShortcutCodes = new int[uiShortcuts.length];
        for (int i = 0; i < uiShortcuts.length; i++) {
            uiShortcutCodes[i] = mapKeyEvent(uiShortcuts[i]);
        }

        return uiShortcutCodes;
    }

    private static int mapKeyEvent(String text) {
        return switch (text.toLowerCase(Locale.ROOT).trim()) {
            case "ctrl", "control" -> NativeKeyEvent.VC_CONTROL;
            case "shift" -> NativeKeyEvent.VC_SHIFT;
            case "alt" -> NativeKeyEvent.VC_ALT;
            case "enter" -> NativeKeyEvent.VC_ENTER;
            case "space" -> NativeKeyEvent.VC_SPACE;
            case "a" -> NativeKeyEvent.VC_A;
            case "b" -> NativeKeyEvent.VC_B;
            case "c" -> NativeKeyEvent.VC_C;
            case "d" -> NativeKeyEvent.VC_D;
            case "e" -> NativeKeyEvent.VC_E;
            case "f" -> NativeKeyEvent.VC_F;
            case "g" -> NativeKeyEvent.VC_G;
            case "h" -> NativeKeyEvent.VC_H;
            case "i" -> NativeKeyEvent.VC_I;
            case "j" -> NativeKeyEvent.VC_J;
            case "k" -> NativeKeyEvent.VC_K;
            case "l" -> NativeKeyEvent.VC_L;
            case "m" -> NativeKeyEvent.VC_M;
            case "n" -> NativeKeyEvent.VC_N;
            case "o" -> NativeKeyEvent.VC_O;
            case "p" -> NativeKeyEvent.VC_P;
            case "q" -> NativeKeyEvent.VC_Q;
            case "r" -> NativeKeyEvent.VC_R;
            case "s" -> NativeKeyEvent.VC_S;
            case "t" -> NativeKeyEvent.VC_T;
            case "u" -> NativeKeyEvent.VC_U;
            case "v" -> NativeKeyEvent.VC_V;
            case "w" -> NativeKeyEvent.VC_W;
            case "x" -> NativeKeyEvent.VC_X;
            case "y" -> NativeKeyEvent.VC_Y;
            case "z" -> NativeKeyEvent.VC_Z;
            case "1" -> NativeKeyEvent.VC_1;
            case "2" -> NativeKeyEvent.VC_2;
            case "3" -> NativeKeyEvent.VC_3;
            case "4" -> NativeKeyEvent.VC_4;
            case "5" -> NativeKeyEvent.VC_5;
            case "6" -> NativeKeyEvent.VC_6;
            case "7" -> NativeKeyEvent.VC_7;
            case "8" -> NativeKeyEvent.VC_8;
            case "9" -> NativeKeyEvent.VC_9;
            case "0" -> NativeKeyEvent.VC_0;
            case "escape" -> NativeKeyEvent.VC_ESCAPE;
            case "f1" -> NativeKeyEvent.VC_F1;
            case "f2" -> NativeKeyEvent.VC_F2;
            case "f3" -> NativeKeyEvent.VC_F3;
            case "f4" -> NativeKeyEvent.VC_F4;
            case "f5" -> NativeKeyEvent.VC_F5;
            case "f6" -> NativeKeyEvent.VC_F6;
            case "f7" -> NativeKeyEvent.VC_F7;
            case "f8" -> NativeKeyEvent.VC_F8;
            case "f9" -> NativeKeyEvent.VC_F9;
            case "f10" -> NativeKeyEvent.VC_F10;
            case "f11" -> NativeKeyEvent.VC_F11;
            case "f12" -> NativeKeyEvent.VC_F12;
            default -> throw new RuntimeException("Invalid ui shortcut, Item: " + text);
        };
    }

    public static String mapKeyCode(int keyCode) {
        return switch (keyCode) {
            case NativeKeyEvent.VC_CONTROL -> "Ctrl";
            case NativeKeyEvent.VC_SHIFT -> "Shift";
            case NativeKeyEvent.VC_ALT -> "Alt";
            case NativeKeyEvent.VC_ENTER -> "Enter";
            case NativeKeyEvent.VC_SPACE -> "Space";
            case NativeKeyEvent.VC_A -> "A";
            case NativeKeyEvent.VC_B -> "B";
            case NativeKeyEvent.VC_C -> "C";
            case NativeKeyEvent.VC_D -> "D";
            case NativeKeyEvent.VC_E -> "E";
            case NativeKeyEvent.VC_F -> "F";
            case NativeKeyEvent.VC_G -> "G";
            case NativeKeyEvent.VC_H -> "H";
            case NativeKeyEvent.VC_I -> "I";
            case NativeKeyEvent.VC_J -> "J";
            case NativeKeyEvent.VC_K -> "K";
            case NativeKeyEvent.VC_L -> "L";
            case NativeKeyEvent.VC_M -> "M";
            case NativeKeyEvent.VC_N -> "N";
            case NativeKeyEvent.VC_O -> "O";
            case NativeKeyEvent.VC_P -> "P";
            case NativeKeyEvent.VC_Q -> "Q";
            case NativeKeyEvent.VC_R -> "R";
            case NativeKeyEvent.VC_S -> "S";
            case NativeKeyEvent.VC_T -> "T";
            case NativeKeyEvent.VC_U -> "U";
            case NativeKeyEvent.VC_V -> "V";
            case NativeKeyEvent.VC_W -> "W";
            case NativeKeyEvent.VC_X -> "X";
            case NativeKeyEvent.VC_Y -> "Y";
            case NativeKeyEvent.VC_Z -> "Z";
            case NativeKeyEvent.VC_1 -> "1";
            case NativeKeyEvent.VC_2 -> "2";
            case NativeKeyEvent.VC_3 -> "3";
            case NativeKeyEvent.VC_4 -> "4";
            case NativeKeyEvent.VC_5 -> "5";
            case NativeKeyEvent.VC_6 -> "6";
            case NativeKeyEvent.VC_7 -> "7";
            case NativeKeyEvent.VC_8 -> "8";
            case NativeKeyEvent.VC_9 -> "9";
            case NativeKeyEvent.VC_0 -> "0";
            case NativeKeyEvent.VC_ESCAPE -> "Escape";
            case NativeKeyEvent.VC_F1 -> "F1";
            case NativeKeyEvent.VC_F2 -> "F2";
            case NativeKeyEvent.VC_F3 -> "F3";
            case NativeKeyEvent.VC_F4 -> "F4";
            case NativeKeyEvent.VC_F5 -> "F5";
            case NativeKeyEvent.VC_F6 -> "F6";
            case NativeKeyEvent.VC_F7 -> "F7";
            case NativeKeyEvent.VC_F8 -> "F8";
            case NativeKeyEvent.VC_F9 -> "F9";
            case NativeKeyEvent.VC_F10 -> "F10";
            case NativeKeyEvent.VC_F11 -> "F11";
            case NativeKeyEvent.VC_F12 -> "F12";
            default -> throw new RuntimeException("Invalid key code: " + keyCode);
        };
    }

}
