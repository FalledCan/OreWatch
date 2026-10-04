package com.github.falledcan.block_xray;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XrayCmdTab implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            for (OreType type : OreType.values()) {
                options.add(type.id);
            }
            options.add("all");
            options.add("off");
            if (sender.hasPermission("blockxray.reload")) {
                options.add("reload");
            }
            return StringUtil.copyPartialMatches(args[0], options, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
