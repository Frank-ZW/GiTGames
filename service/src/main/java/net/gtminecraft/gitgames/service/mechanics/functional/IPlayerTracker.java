package net.gtminecraft.gitgames.service.mechanics.functional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPlayerTracker {

	void updatePlayerTracker(@NotNull Player player, @Nullable Player target, @NotNull ItemStack tracker);
	boolean isPlayerTracker(@NotNull ItemStack item);
	@NotNull ItemStack createPlayerTracker();
}
