package io.github.theepicblock.discordunban;

import org.bukkit.configuration.file.FileConfiguration;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LangStrings {
    private Map<String, String> stringMap = new HashMap<>();

    public LangStrings(FileConfiguration config) {
        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            stringMap.put(key, Objects.requireNonNull(config.getString(key)).replace("'", "''")); //single quotes must be escaped with double quotes for MessageFormat
        }
    }

    public String getString(String key) {
        String v = stringMap.get(key);
        return v != null ? v : "couldn't find key '" + key + "', please make sure it is in lang.yml. This may be a newly added string. In which case you should update your lang.yml either by deleting it or by manually adding the new value";
    }

    public String getFormatted(String key, Object... objects) {
        return MessageFormat.format(getString(key), objects);
    }
}
