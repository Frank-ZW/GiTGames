package net.gtminecraft.gitgames.server.loader;

import net.gtminecraft.gitgames.server.minigame.AbstractGame;
import org.jetbrains.annotations.NotNull;

public interface GameClassLoaderInterface {

	int getId();
	@NotNull AbstractGame load(int gameKey);
}
