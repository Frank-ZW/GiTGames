package net.gtminecraft.gitgames.server.util;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

public class StringUtil {

	public static final Component GAME_PREFIX = Component.text(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "Game Chat" + ChatColor.DARK_GRAY + "] ");
	public static final Component SPECTATOR_PREFIX = Component.text(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Spectator Chat" + ChatColor.DARK_GRAY + "] ");
	public static final Component LOBBY_PREFIX = Component.text(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "Lobby Chat" + ChatColor.DARK_GRAY + "] ");

	public static final String ERROR_TELEPORTING_TO_MAP = ChatColor.RED + "Failed to teleport you to the %s world(s). Contact an administrator if this occurs.";
}
