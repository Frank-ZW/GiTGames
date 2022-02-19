package net.gtminecraft.gitgames.server.loader.manager;

import net.gtminecraft.gitgames.compatability.mechanics.AbstractGameClassifier;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.loader.GameClassLoaderInterface;
import net.gtminecraft.gitgames.server.map.manager.MapLoaderManager;
import net.gtminecraft.gitgames.server.minigame.impl.manhunt.VanillaManhuntLoader;
import net.gtminecraft.gitgames.server.minigame.impl.spleef.SpleefLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameLoaderManager {

	private final GameClassLoaderInterface[] loaderInterfaces = new GameClassLoaderInterface[Byte.MAX_VALUE];

	public GameLoaderManager(CorePlugin plugin) {
		MapLoaderManager mapLoaderManager = new MapLoaderManager(plugin);
		this.register(new VanillaManhuntLoader(mapLoaderManager));
		this.register(new SpleefLoader(mapLoaderManager));
	}

	@Nullable
	public GameClassLoaderInterface getGameLoader(@Nullable AbstractGameClassifier classifier) {
		return classifier == null ? null : this.loaderInterfaces[classifier.getClassifierId()];
	}

	public void register(@NotNull GameClassLoaderInterface classLoader) {
		this.loaderInterfaces[classLoader.getId()] = classLoader;
	}
}
