package net.gtminecraft.gitgames.server.loader.manager;

import net.gtminecraft.gitgames.server.loader.GameClassLoaderInterface;
import net.gtminecraft.gitgames.server.minigame.impl.manhunt.VanillaManhuntLoader;
import net.gtminecraft.gitgames.server.minigame.impl.spleef.SpleefLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GameLoaderManager {

	private final Map<Double, GameClassLoaderInterface> loaderInterfaces = new HashMap<>();

	public GameLoaderManager() {
		this.register(new VanillaManhuntLoader());
		this.register(new SpleefLoader());
	}

	@Nullable
	public GameClassLoaderInterface getGameLoader(double gameId) {
		return this.loaderInterfaces.get(gameId);
	}

	public void register(@NotNull GameClassLoaderInterface classLoader) {
		this.loaderInterfaces.put(classLoader.getId(), classLoader);
	}
}
