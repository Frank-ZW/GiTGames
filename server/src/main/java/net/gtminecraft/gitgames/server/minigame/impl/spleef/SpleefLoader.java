package net.gtminecraft.gitgames.server.minigame.impl.spleef;

import net.gtminecraft.gitgames.compatability.mechanics.GameClassifiers;
import net.gtminecraft.gitgames.server.loader.GameClassLoaderInterface;
import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SpleefLoader implements GameClassLoaderInterface {

	@Override
	public double getId() {
		return GameClassifiers.SPLEEF_CLASSIFIER.getId();
	}

	@Override
	public @NotNull AbstractMinigame loadGame(@NotNull Location spawn, int gameKey) {
		return new Spleef(spawn, gameKey);
	}
}
