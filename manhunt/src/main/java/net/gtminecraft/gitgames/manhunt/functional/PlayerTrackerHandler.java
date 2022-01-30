package net.gtminecraft.gitgames.manhunt.functional;

import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.service.AbstractCorePlugin;
import net.gtminecraft.gitgames.service.mechanics.functional.IPlayerTracker;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class PlayerTrackerHandler implements IPlayerTracker {

	private final AbstractCorePlugin plugin;
	private final Component displayName;
	private final String persistentData;

	@Override
	public void updatePlayerTracker(@NotNull Player player, @Nullable Player target, @NotNull ItemStack tracker) {
		if (target == null) {
			player.sendActionBar(Component.text(ChatColor.RED + "There are no players to track!"));
			return;
		}

		CompassMeta meta = (CompassMeta) tracker.getItemMeta();
		if (player.getWorld().equals(target.getWorld())) {
			meta.setLodestone(target.getLocation());
			meta.setLodestoneTracked(false);
			tracker.setItemMeta(meta);
			player.sendActionBar(Component.text(ChatColor.GREEN + "Currently tracking " + target.getName() + "'s latest location."));
		} else {
			player.sendActionBar(Component.text(ChatColor.RED + "There are no players to track!"));
		}
	}

	@Override
	public boolean isPlayerTracker(@NotNull ItemStack item) {
		ItemMeta itemMeta = item.getItemMeta();
		PersistentDataContainer container = itemMeta.getPersistentDataContainer();
		return container.has(this.plugin.getKey(), PersistentDataType.STRING) && this.persistentData.equals(container.get(this.plugin.getKey(), PersistentDataType.STRING)) && itemMeta instanceof CompassMeta;
	}

	@Override
	public @NotNull ItemStack createPlayerTracker() {
		ItemStack item = new ItemStack(Material.COMPASS);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.displayName(this.displayName);
		itemMeta.getPersistentDataContainer().set(this.plugin.getKey(), PersistentDataType.STRING, "player_tracker");
		item.setItemMeta(itemMeta);
		return item;
	}
}
