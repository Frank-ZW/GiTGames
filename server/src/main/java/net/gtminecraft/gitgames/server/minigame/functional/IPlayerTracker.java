package net.gtminecraft.gitgames.service.mechanics.functional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPlayerTracker {

	/**
	 * Updates the player tracker to the target's location. If the target player is not online, this method will
	 * fail silently.
	 *
	 * @param player	The owner of the player tracker.
	 * @param target	The player whose location the player tracker should point to.
	 * @param tracker	The player tracker item itself.
	 */
	void updatePlayerTracker(@NotNull Player player, @Nullable Player target, @NotNull ItemStack tracker);

	/**
	 * @param item	The ItemStack to check if player tracker.
	 * @return		True if the item is a player tracker, false otherwise.
	 */
	boolean isPlayerTracker(@NotNull ItemStack item);

	/**
	 * @return	An ItemStack representing the player tracker.
	 */
	@NotNull ItemStack createPlayerTracker();
}
