package net.gtminecraft.gitgames.server.minigame.impl.manhunt;

import net.gtminecraft.gitgames.compatability.mechanics.GameClassifiers;
import net.gtminecraft.gitgames.server.loader.GameClassLoaderInterface;
import net.gtminecraft.gitgames.server.minigame.AbstractMinigame;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class VanillaManhuntLoader implements GameClassLoaderInterface {

	@Override
	public int getId() {
		return GameClassifiers.MANHUNT.getClassifierId();
	}

	@Override
	public @NotNull AbstractMinigame loadGame(@NotNull Location spawn, int gameKey) {
		return new VanillaManhunt(spawn, gameKey);
	}
}
