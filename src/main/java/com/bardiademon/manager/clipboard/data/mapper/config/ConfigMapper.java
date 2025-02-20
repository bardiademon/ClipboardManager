package com.bardiademon.manager.clipboard.data.mapper.config;

import com.bardiademon.Jjson.array.JjsonArray;
import com.bardiademon.Jjson.exception.JjsonException;
import com.bardiademon.Jjson.object.JjsonObject;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.data.enums.UILookAndFeelType;
import com.bardiademon.manager.clipboard.data.model.config.ClipboardHandlerPeriod;
import com.bardiademon.manager.clipboard.data.model.config.ConfigModel;
import com.bardiademon.manager.clipboard.data.model.config.ConfigShortcutModel;
import com.bardiademon.manager.clipboard.util.Paths;

import java.io.IOException;

public class ConfigMapper {

    private static final JjsonObject DEFAULT_CONFIG = new JjsonObject()
            .put("theme", UILookAndFeelType.MAC_DARK.name())
            .put("shortcuts", new JjsonObject()
                    .put("open_ui", "F7")
                    .put("close_app", "Ctrl+Alt+O")
                    .put("clear_all_clipboard", "Ctrl+Alt+F8")
                    .put("delete_last_data", "Alt+F8")
                    .put("clear_system_clipboard", "Ctrl+Alt+F9")
                    .put("restart", "Ctrl+Alt+F11")
            )
            .put("clipboard_handler_period", new JjsonObject()
                    .put("clipboard_handler_mills", 500)
                    .put("clipboard_image_handler_sec", 10)
            )
            .put("clipboard_types", JjsonArray.ofArray(ClipboardType.getValues()))
            .put("clipboard_save_count", 50);

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
                joConfig = DEFAULT_CONFIG;

                joConfig.write(Paths.CONFIG_PATH, false, true);

                System.out.println("JoConfig: " + joConfig);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        try {
            JjsonObject shortcuts = joConfig.getJjsonObject("shortcuts");
            int[] openUI = ShortcutMapper.mapUiShortcut(shortcuts.getString("open_ui"));
            int[] closeApp = ShortcutMapper.mapUiShortcut(shortcuts.getString("close_app"));
            int[] clearAllClipboard = ShortcutMapper.mapUiShortcut(shortcuts.getString("clear_all_clipboard"));
            int[] deleteLastClipboard = ShortcutMapper.mapUiShortcut(shortcuts.getString("delete_last_data"));
            int[] clearSystemClipboard = ShortcutMapper.mapUiShortcut(shortcuts.getString("clear_system_clipboard"));
            int[] restart = ShortcutMapper.mapUiShortcut(shortcuts.getString("restart"));

            JjsonObject joClipboardHandlerPeriod = joConfig.getJjsonObject("clipboard_handler_period");

            return new ConfigModel(
                    UILookAndFeelType.valueOf(joConfig.getString("theme")),
                    new ConfigShortcutModel(openUI, closeApp, clearAllClipboard, deleteLastClipboard, clearSystemClipboard, restart),
                    joConfig.getJjsonArray("clipboard_types").stream().map(item -> ClipboardType.valueOf(item.toString())).toList(),
                    joConfig.getInteger("clipboard_save_count"),
                    new ClipboardHandlerPeriod(joClipboardHandlerPeriod.getInteger("clipboard_handler_mills"), joClipboardHandlerPeriod.getInteger("clipboard_image_handler_sec"))
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}
