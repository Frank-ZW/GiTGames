package net.gtminecraft.gitgames.server.loader;

import net.gtminecraft.gitgames.server.minigame.impl.manhunt.VanillaManhunt;
import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class VanillaManhuntLoader implements GameClassLoaderInterface {

	@Override
	public @NotNull AbstractMinigame loadGame(@NotNull Location spawn, int gameKey) {
		return new VanillaManhunt(spawn, gameKey);
	}
}
