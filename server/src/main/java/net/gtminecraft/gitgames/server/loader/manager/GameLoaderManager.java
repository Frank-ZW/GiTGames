package net.gtminecraft.gitgames.server.loader.manager;

import net.gtminecraft.gitgames.server.loader.GameClassLoaderInterface;
import net.gtminecraft.gitgames.server.minigame.impl.manhunt.VanillaManhuntLoader;
import net.gtminecraft.gitgames.server.minigame.impl.spleef.SpleefLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameLoaderManager {

	private final GameClassLoaderInterface[] loaderInterfaces = new GameClassLoaderInterface[Byte.MAX_VALUE];

	public GameLoaderManager() {
		this.register(new VanillaManhuntLoader());
		this.register(new SpleefLoader());
	}

	@Nullable
	public GameClassLoaderInterface getGameLoader(int gameId) {
		return this.loaderInterfaces[gameId];
	}

	public void register(@NotNull GameClassLoaderInterface classLoader) {
		this.loaderInterfaces[classLoader.getId()] = classLoader;
	}
}
