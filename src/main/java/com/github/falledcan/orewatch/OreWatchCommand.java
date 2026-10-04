package com.github.falledcan.orewatch;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OreWatchCommand implements CommandExecutor {

    private final OreWatch plugin;
    private final GlowManager manager;
    private final Messages messages;

    OreWatchCommand(OreWatch plugin, GlowManager manager, Messages messages) {
        this.plugin = plugin;
        this.manager = manager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("orewatch.reload")) {
                sender.sendMessage(messages.prefixed(sender, "no-permission"));
                return true;
            }
            plugin.reload();
            sender.sendMessage(messages.prefixed(sender, "reloaded"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.prefixed(sender, "player-only"));
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            sendMenu(player);
            return true;
        }

        String arg = args[0].toLowerCase();
        if (arg.equals("all")) {
            manager.enableAll(player);
            player.sendMessage(messages.prefixed(player, "all-on"));
        } else if (arg.equals("off")) {
            manager.disable(player);
            player.sendMessage(messages.prefixed(player, "all-off"));
        } else {
            OreType type = OreType.fromName(arg);
            if (type == null) {
                player.sendMessage(messages.prefixed(player, "unknown-ore", args[0]));
                return true;
            }
            boolean enabled = manager.toggle(player, type);
            player.sendMessage(messages.prefixed(player, "toggled", oreLabel(player, type),
                    messages.get(player, enabled ? "state-on" : "state-off")));
        }
        // GUI 代わりのメニューをクリックで操作した時は、メニューを出し直す
        if (args.length >= 2 && args[1].equals("menu")) {
            sendMenu(player);
        }
        return true;
    }

    private void sendMenu(Player player) {
        player.sendMessage(messages.get(player, "menu-header"));

        ComponentBuilder line = new ComponentBuilder();
        int count = 0;
        for (OreType type : OreType.values()) {
            boolean on = manager.isEnabled(player, type);
            String name = messages.oreName(player, type);
            line.append(button(
                    (on ? ChatColor.GREEN + "■ " : ChatColor.DARK_GRAY + "□ ") + oreLabel(player, type),
                    "/orewatch " + type.id + " menu",
                    messages.get(player, on ? "menu-hover-disable" : "menu-hover-enable", name)));
            line.append("   ", ComponentBuilder.FormatRetention.NONE);
            if (++count % 4 == 0) {
                player.spigot().sendMessage(line.create());
                line = new ComponentBuilder();
            }
        }
        if (count % 4 != 0) {
            player.spigot().sendMessage(line.create());
        }

        player.spigot().sendMessage(new ComponentBuilder()
                .append(button(messages.get(player, "button-all-on"), "/orewatch all menu",
                        messages.get(player, "hover-all-on")))
                .append("  ", ComponentBuilder.FormatRetention.NONE)
                .append(button(messages.get(player, "button-all-off"), "/orewatch off menu",
                        messages.get(player, "hover-all-off")))
                .create());
    }

    private String oreLabel(Player player, OreType type) {
        return type.chatColor + messages.oreName(player, type);
    }

    private static BaseComponent[] button(String text, String command, String hover) {
        TextComponent component = new TextComponent(text);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
        return new BaseComponent[]{component};
    }
}
