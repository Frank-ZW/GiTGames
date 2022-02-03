package net.gtminecraft.gitgames.server.map;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface GameMapInterface {

	boolean isLoaded();
	boolean load();
	void unload();
	boolean restoreFromSource();
	@NotNull World getWorld();
}
