package com.bardiademon.manager.clipboard.data.mapper;

import com.bardiademon.Jjson.exception.JjsonException;
import com.bardiademon.Jjson.object.JjsonObject;
import com.bardiademon.manager.clipboard.data.enums.ClipboardType;
import com.bardiademon.manager.clipboard.data.model.ConfigModel;
import com.bardiademon.manager.clipboard.util.Paths;

import java.io.IOException;

public class ConfigMapper {

    private static final String DEFAULT_CONFIG = """
            {
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
                    joConfig.getJjsonArray("clipboard_types").stream().map(item -> ClipboardType.valueOf(item.toString())).toList(),
                    joConfig.getInteger("clipboard_save_count")
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
