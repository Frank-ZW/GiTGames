package net.gtminecraft.gitgames.server.loader.manager;

import net.gtminecraft.gitgames.compatability.mechanics.GameType;
import net.gtminecraft.gitgames.server.loader.GameClassLoaderInterface;
import net.gtminecraft.gitgames.server.loader.VanillaManhuntLoader;
import org.jetbrains.annotations.Nullable;

public class GameLoaderManager {

	private final GameClassLoaderInterface[] loaderInterfaces = new GameClassLoaderInterface[Byte.MAX_VALUE];

	public GameLoaderManager() {
		this.loaderInterfaces[GameType.MANHUNT.ordinal()] = new VanillaManhuntLoader();
	}

	@Nullable
	public GameClassLoaderInterface getGameLoader(int gameId) {
		return this.loaderInterfaces[gameId];
	}
}
