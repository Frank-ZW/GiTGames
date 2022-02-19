package net.gtminecraft.gitgames.server.map;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.location.CustomLocation;
import net.gtminecraft.gitgames.server.minigame.AbstractGame;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
public class MapDataContainer {

	private final File sourceWorldFolder;
	private final double x;
	private final double y;
	private final double z;
	private final float yaw;
	private final float pitch;

	public @NotNull LoadedGameMap toLoadedGameMap(@NotNull AbstractGame game) {
		return new LoadedGameMap(CorePlugin.getInstance(), game, this.sourceWorldFolder, new CustomLocation(this.x, this.y, this.z, this.yaw, this.pitch), true);
	}
}
