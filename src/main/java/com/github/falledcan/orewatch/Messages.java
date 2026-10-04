package com.github.falledcan.orewatch;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * plugins/OreWatch/lang/*.yml からメッセージを読む。
 * config.yml の language が auto のときは、プレイヤーのゲームの言語設定に合わせる。
 */
public class Messages {

    private static final String[] LANGUAGES = {"ja", "en"};
    private static final String FALLBACK = "en";

    private final OreWatch plugin;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();
    private String language;

    Messages(OreWatch plugin) {
        this.plugin = plugin;
    }

    void load() {
        language = plugin.getConfig().getString("language", "auto").toLowerCase(Locale.ROOT);
        languages.clear();
        for (String lang : LANGUAGES) {
            String path = "lang/" + lang + ".yml";
            File file = new File(plugin.getDataFolder(), path);
            if (!file.exists()) {
                plugin.saveResource(path, false);
            }
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            // 編集したファイルにキーが足りなくても、jar 内の元の文で補う
            InputStream defaults = plugin.getResource(path);
            if (defaults != null) {
                yaml.setDefaults(YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaults, StandardCharsets.UTF_8)));
            }
            languages.put(lang, yaml);
        }
        if (!language.equals("auto") && !languages.containsKey(language)) {
            plugin.getLogger().warning("Unknown language '" + language + "', using " + FALLBACK);
            language = FALLBACK;
        }
    }

    /** prefix を付けたメッセージ */
    String prefixed(CommandSender sender, String key, Object... args) {
        return get(sender, "prefix") + get(sender, key, args);
    }

    String get(CommandSender sender, String key, Object... args) {
        String text = languageOf(sender).getString(key, key);
        if (args.length > 0) {
            text = MessageFormat.format(text.replace("'", "''"), args);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    String oreName(CommandSender sender, OreType type) {
        return get(sender, "ore." + type.id);
    }

    private YamlConfiguration languageOf(CommandSender sender) {
        String lang = language;
        if (lang.equals("auto")) {
            lang = FALLBACK;
            if (sender instanceof Player) {
                // ゲームの言語設定 (例: ja_jp, en_us) の先頭2文字で判定
                String locale = ((Player) sender).getLocale().toLowerCase(Locale.ROOT);
                String code = locale.length() >= 2 ? locale.substring(0, 2) : locale;
                if (languages.containsKey(code)) {
                    lang = code;
                }
            }
        }
        return languages.get(lang);
    }
}
