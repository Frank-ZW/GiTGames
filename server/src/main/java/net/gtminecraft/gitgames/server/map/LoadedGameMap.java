package net.gtminecraft.gitgames.server.map;

import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.location.CustomLocation;
import net.gtminecraft.gitgames.server.minigame.AbstractGame;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LoadedGameMap extends AbstractGameMap {

	private Location lobby;
	private final File sourceWorldFolder;
	private File activeWorldFolder;
	private World world;
	private final CustomLocation location;

	public LoadedGameMap(CorePlugin plugin, AbstractGame game, File sourceWorldFolder, CustomLocation location, boolean loadOnInit) {
		super(plugin, game);
		this.sourceWorldFolder = sourceWorldFolder;
		this.location = location;
		if (loadOnInit) {
			Bukkit.getScheduler().callSyncMethod(this.plugin, this::load);
		}
	}

	@Override
	public boolean isLoaded() {
		return this.world != null;
	}

	@Override
	public boolean load() {
		if (this.isLoaded()) {
			return true;
		}

		this.activeWorldFolder = new File(Bukkit.getWorldContainer().getParentFile(), this.sourceWorldFolder.getName() + "_copy_" + System.currentTimeMillis());
		try {
			FileUtils.copyDirectory(this.sourceWorldFolder, this.activeWorldFolder);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Failed to load game map from source folder", e);
			return false;
		}

		this.world = Bukkit.createWorld(new WorldCreator(this.activeWorldFolder.getName()));
		if (this.world != null) {
			this.world.setKeepSpawnInMemory(false);
			this.world.setAutoSave(false);
			this.lobby = this.location.toLocation(this.world);
		}

		return this.isLoaded();
	}

	@Override
	public void unload() {
		if (this.world != null) {
			Bukkit.unloadWorld(this.world, false);
		}

		if (this.activeWorldFolder != null) {
			try {
				FileUtils.deleteDirectory(this.activeWorldFolder);
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, "Failed to delete world directory for " + this.activeWorldFolder.getName(), e);
			}
		}

		this.world = null;
		this.activeWorldFolder = null;
	}

	@Override
	public boolean restoreFromSource() {
		this.unload();
		return this.load();
	}

	@Override
	public @NotNull World getWorld() {
		return this.world;
	}

	@Override
	public @NotNull Location getLobby() {
		return this.lobby;
	}
}
