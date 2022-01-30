package net.gtminecraft.gitgames.proxy.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class StringUtil {

	public static final BaseComponent EMPTY_STRING = new TextComponent("");
	public static final BaseComponent EARLY_LOGIN = new TextComponent(ChatColor.RED + "Please wait for the Core to finish loading before logging in.");
	public static final BaseComponent INSUFFICIENT_PERMISSION = new TextComponent(ChatColor.RED + "You do not have permission to run this command.");
	public static final BaseComponent PLAYERS_ONLY = new TextComponent(ChatColor.RED + "This command can only be executed by players.");
	public static final BaseComponent RUN_ON_INVALID_SERVER = new TextComponent(ChatColor.RED + "This command is cannot be executed on this server.");
	public static final BaseComponent ERROR_RETRIEVING_DATA = new TextComponent(ChatColor.RED + "An error occurred while retrieving your player data. Basic server features will be disabled. If this occurs, contact an administrator immediately.");

	public static final String SHUTDOWN_COMMAND = "gitgames.command.shutdown";
	public static final String SHUTDOWN_CANCEL_COMMAND = "gitgames.command.shutdown.cancel";
	public static final String SHUTDOWN_START_COMMAND = "gitgames.command.shutdown.start";
	public static final String SERVER_STATUS_COMMAND = "gitgames.command.serverstatus";
	public static final String SOLO_MINIGAME_COMMAND = "gitgames.command.%s.solo";
	public static final String FORCE_END_MINIGAME_COMMAND = "gitgames.command.forceend";
	public static final String SPECTATE_COMMAND = "gitgames.command.spectate";

}
