package net.gtminecraft.gitgames.server.minigame.impl.spleef;

import net.gtminecraft.gitgames.compatability.mechanics.GameClassifiers;
import net.gtminecraft.gitgames.server.loader.GameClassLoaderInterface;
import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SpleefLoader implements GameClassLoaderInterface {

	@Override
	public int getId() {
		return GameClassifiers.SPLEEF.getClassifierId();
	}

	@Override
	public @NotNull AbstractMinigame loadGame(@NotNull Location spawn, int gameKey) {
		return new Spleef(spawn, gameKey);
	}
}
