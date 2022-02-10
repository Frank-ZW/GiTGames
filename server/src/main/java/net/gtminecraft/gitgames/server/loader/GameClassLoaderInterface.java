package net.gtminecraft.gitgames.server.loader;

import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface GameClassLoaderInterface {

	int getId();
	@NotNull AbstractMinigame loadGame(@NotNull Location spawn, int gameKey);
}
