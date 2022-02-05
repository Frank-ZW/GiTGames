package net.gtminecraft.gitgames.server.runnable;

import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.server.CorePlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public abstract class ChainableBukkitRunnable extends BukkitRunnable {

	protected final CorePlugin plugin;

	public @NotNull ChainableBukkitRunnable cancelThenRun(@NotNull Runnable runnable) {
		runnable.run();
		return this;
	}
}
