package net.gtminecraft.gitgames.server.minigame.impl.manhunt;

import net.gtminecraft.gitgames.compatability.mechanics.GameClassifiers;
import net.gtminecraft.gitgames.server.loader.AbstractGameClassLoader;
import net.gtminecraft.gitgames.server.map.manager.MapLoaderManager;
import net.gtminecraft.gitgames.server.minigame.AbstractGame;
import org.jetbrains.annotations.NotNull;

public class VanillaManhuntLoader extends AbstractGameClassLoader {

	public VanillaManhuntLoader(MapLoaderManager mapLoaderManager) {
		super(GameClassifiers.MANHUNT, mapLoaderManager);
	}

	@Override
	public @NotNull AbstractGame load(int gameKey) {
		return new VanillaManhunt(this.mapLoaderManager.getLobbyContainer(), gameKey);
	}
}
