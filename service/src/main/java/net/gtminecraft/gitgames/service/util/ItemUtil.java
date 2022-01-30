package net.gtminecraft.gitgames.service.util;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ItemUtil {

	public static final TextComponent SPECTATOR_GUI = Component.text(ChatColor.BLUE + "Players");
	public static final TextComponent SPECTATOR_COMPASS = Component.text(ChatColor.GREEN + "Spectator Compass");
	public static final TextComponent SPECTATOR_QUIT = Component.text(ChatColor.RED + "Leave the game");
	public static final TextComponent DEFAULT_PLAYER_TRACKER = Component.text(ChatColor.RED + "Player Tracker");
	public static final TextComponent LOCKOUT_BOARD_TITLE = Component.text(ChatColor.BLUE + "Challenges");
	public static final TextComponent LOCKOUT_PLAYER_TRACKER = Component.text(ChatColor.GREEN + "Players");
	private static final Set<Material> BED_ITEMS = ImmutableSet.of(
			Material.BLACK_BED,
			Material.BLUE_BED,
			Material.BROWN_BED,
			Material.CYAN_BED,
			Material.GRAY_BED,
			Material.GREEN_BED,
			Material.LIGHT_BLUE_BED,
			Material.LIGHT_GRAY_BED,
			Material.LIME_BED,
			Material.MAGENTA_BED,
			Material.ORANGE_BED,
			Material.PINK_BED,
			Material.PURPLE_BED,
			Material.RED_BED,
			Material.WHITE_BED,
			Material.YELLOW_BED
	);

	public static ItemStack createTaggedItem(Material material, @NotNull Component name, String ... tags) {
		ItemStack item = new ItemStack(material);
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta != null) {
			itemMeta.displayName(name);
			item.setItemMeta(itemMeta);
		}

		return item;
	}

	public static ItemStack createSpectatorCompass() {
		return ItemUtil.createTaggedItem(Material.COMPASS, ItemUtil.SPECTATOR_COMPASS, "spectator_compass");
	}

	public static ItemStack createSpectatorQuit() {
		return ItemUtil.createTaggedItem(Material.RED_BED, ItemUtil.SPECTATOR_QUIT, "spectator_quit");
	}

	public static boolean isBed(Material material) {
		return ItemUtil.BED_ITEMS.contains(material);
	}

	public static boolean isBed(Block block) {
		return block != null && ItemUtil.isBed(block.getType());
	}

}
