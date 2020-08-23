package io.github.theepicblock.discordunban;

import org.bukkit.configuration.file.FileConfiguration;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public class LangStrings {
    private final Map<String, String> stringMap = new HashMap<>();

    public LangStrings(FileConfiguration config, Logger logger) {
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            String value = config.getString(key);
            if (value == null) {
                logger.warning(String.format("%s is not a string", key));
                continue;
            }
            value = value.replace("'","''"); //single quotes must be escaped with double quotes for MessageFormat
            stringMap.put(key, value);
        }
    }

    public String getString(String key) {
        String v = stringMap.get(key);
        return v != null ? v : String.format("couldn't find key '%s', please make sure it is in lang.yml. This may be a newly added string. In which case you should update your lang.yml either by deleting it or by manually adding the new value", key);
    }

    public String getFormatted(String key, Object... objects) {
        return MessageFormat.format(getString(key), objects);
    }
}
