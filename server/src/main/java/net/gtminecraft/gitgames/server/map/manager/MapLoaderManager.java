package net.gtminecraft.gitgames.server.map.manager;

import lombok.Getter;
import net.gtminecraft.gitgames.compatability.mechanics.AbstractGameClassifier;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.map.GameMapInterface;
import net.gtminecraft.gitgames.server.map.LoadableGameMap;
import net.gtminecraft.gitgames.server.map.MapDataContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public class MapLoaderManager {

	private static final String RESOURCES_PATH = "mapdata";

	private final CorePlugin plugin;
	private final Map<AbstractGameClassifier, List<MapDataContainer>> maps = new HashMap<>();
	@Getter
	private FileConfiguration mapConfig;
	private final File mapConfigFile;
	@Getter
	private final File mapFolder;
	@Getter
	private boolean success = true;

	public MapLoaderManager(CorePlugin plugin) {
		this.plugin = plugin;
		this.mapConfigFile = new File(plugin.getDataFolder(), "mapdata.yml");
		this.saveDefaultConfig();

		// If mapdata already exists, loop through subdirectories and check if each folder is equal
		this.mapFolder = new File(plugin.getDataFolder(), "mapdata");
		if (!this.mapFolder.exists()) {
			if (!this.mapFolder.mkdir()) {
				Bukkit.getLogger().warning("Failed to create new mapdata folder.");
				this.success = false;
				return;
			}

			URL url = CorePlugin.class.getClassLoader().getResource(MapLoaderManager.RESOURCES_PATH);
			if (url == null) {
				Bukkit.getLogger().warning("Failed to get embedded mapdata resource from .jar file");
				this.success = false;
				return;
			}

			try (FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), Collections.emptyMap())) {
				Stream<Path> walk = Files.walk(fileSystem.getPath(MapLoaderManager.RESOURCES_PATH), Integer.MAX_VALUE);
				walk.forEach(child -> {
					try {
						File copied = new File(plugin.getDataFolder(), child.toRealPath().normalize().toString());
						if (Files.isDirectory(child)) {
							if (!copied.exists()) {
								copied.mkdir();
							}
						} else {
							Files.copy(child, copied.toPath(), StandardCopyOption.REPLACE_EXISTING);
						}
					} catch (IOException e) {
						Bukkit.getLogger().log(Level.SEVERE, "Failed to convert child path in .jar resources folder to a canonical path", e);
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException ignored) {}

//			try {
//				URI uri = url.toURI();
//				try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
//					Path rootFolderPath = fileSystem.getPath(MapLoaderManager.RESOURCES_PATH);
//					Stream<Path> walk = Files.walk(rootFolderPath, Integer.MAX_VALUE);
//					walk.forEach(child -> {
//						try {
//							File copied = new File(plugin.getDataFolder(), child.toRealPath().normalize().toString());
//							if (Files.isDirectory(child)) {
//							} else {
//								copied.createNewFile();
//								Files.copy(child, copied.toPath(), StandardCopyOption.REPLACE_EXISTING);
//							}
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					});
//				}
//			} catch (URISyntaxException | IOException e) {
//				e.printStackTrace();
//			}
		}

		this.mapConfig = YamlConfiguration.loadConfiguration(this.mapConfigFile);
		ConfigurationSection section = this.mapConfig.getConfigurationSection("maps");
		if (section == null) {
			Bukkit.getLogger().warning("There is no configuration section for entry maps inside mapdata.yml.");
			this.success = false;
			return;
		}

		// Use true getKeys call
	}

	public void saveDefaultConfig() {
		if (!this.mapConfigFile.exists()) {
			this.plugin.saveResource("mapdata.yml", false);
		}
	}

	public void reloadConfig() {
		this.mapConfig = YamlConfiguration.loadConfiguration(this.mapConfigFile);
	}

	public @NotNull GameMapInterface loadMap(String worldName, boolean loadOnInit) {
		return null;
	}

	public @NotNull GameMapInterface loadMap(File worldsFolder, String worldName, boolean loadOnInit) {
		return new LoadableGameMap(worldsFolder, worldName, loadOnInit);
	}
}
