package net.gtminecraft.gitgames.server.map.manager;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import net.gtminecraft.gitgames.compatability.mechanics.AbstractGameClassifier;
import net.gtminecraft.gitgames.compatability.mechanics.GameClassifiers;
import net.gtminecraft.gitgames.server.CorePlugin;
import net.gtminecraft.gitgames.server.map.MapDataContainer;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

public class MapLoaderManager {

	private static final String RESOURCES_PATH = "mapdata";

	private final CorePlugin plugin;
	private final Random random;
	@Getter
	private MapDataContainer lobbyContainer;
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
		this.random = plugin.getRandom();
		this.mapConfigFile = new File(plugin.getDataFolder(), "mapdata.yml");
		this.saveDefaultConfig();

		this.mapFolder = new File(plugin.getDataFolder(), "mapdata");
		FileUtils.deleteQuietly(this.mapFolder);
		if (!this.mapFolder.mkdir()) {
			Bukkit.getLogger().warning("Failed to create new mapdata folder.");
			this.success = false;
			return;

		}

		URL url = CorePlugin.class.getClassLoader().getResource(MapLoaderManager.RESOURCES_PATH);
		if (url == null) {
			Bukkit.getLogger().warning("Failed to get embedded map resource folder from .jar file");
			this.success = false;
			return;
		}

		try (FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), Collections.emptyMap())) {
			Files.walk(fileSystem.getPath(MapLoaderManager.RESOURCES_PATH), Integer.MAX_VALUE).forEach(this::copyDirectoryResource);
		} catch (IOException e) {
			this.success = false;
			return;
		} catch (URISyntaxException ignored) {}

		this.mapConfig = YamlConfiguration.loadConfiguration(this.mapConfigFile);
		ConfigurationSection section = this.mapConfig.getConfigurationSection("maps");
		if (section == null) {
			Bukkit.getLogger().warning("There is no configuration section for entry maps inside mapdata.yml.");
			this.success = false;
			return;
		}

		/*
		 * spleef
		 * spleef.spleef_1
		 * spleef.spleef_1.lobby
		 * spleef.spleef_1.lobby.x
		 * spleef.spleef_1.lobby.y
		 * spleef.spleef_1.lobby.z
		 * spleef.spleef_1.lobby.yaw
		 * spleef.spleef_1.lobby.pitch
		 */
		for (String s : section.getKeys(true)) {
			String[] array = s.split("\\.");
			if (array.length == 1 && array[0].equalsIgnoreCase("lobby")) {
				String name = Preconditions.checkNotNull(section.getString(s + ".name"));
				double x = section.getDouble(s + ".x");
				double y = section.getDouble(s + ".y");
				double z = section.getDouble(s + ".z");
				float yaw = (float) section.getDouble(s + ".yaw");
				float pitch = (float) section.getDouble(s + ".pitch");
				this.lobbyContainer = new MapDataContainer(new File(this.mapFolder, name), x, y, z, yaw, pitch);
			} else if (array.length == 2 && !array[0].equalsIgnoreCase("lobby")) {
				AbstractGameClassifier classifier = GameClassifiers.classifierByName(array[0]);
				String worldName = Preconditions.checkNotNull(array[1], "Section name cannot be null while reading in data.");
				Preconditions.checkArgument(!GameClassifiers.INACTIVE.equals(classifier), "Game classifier must be known while reading in data.");

				double x = section.getDouble(s + ".lobby.x");
				double y = section.getDouble(s + ".lobby.y");
				double z = section.getDouble(s + ".lobby.z");
				float yaw = (float) section.getDouble(s + ".lobby.yaw");
				float pitch = (float) section.getDouble(s + ".lobby.pitch");
				this.maps.compute(classifier, (k, v) -> {
					if (v == null) {
						v = new ArrayList<>();
					}

					v.add(new MapDataContainer(new File(this.mapFolder, worldName), x, y, z, yaw, pitch));
					return v;
				});
			}
		}
	}

	@SneakyThrows
	private void copyDirectoryResource(Path child) {
		File copied = new File(plugin.getDataFolder(), child.toRealPath().normalize().toString());
		if (Files.isDirectory(child)) {
			if (!copied.exists() && !copied.mkdir()) {
				Bukkit.getLogger().warning("Failed to create new directory for " + copied.getName());
			}
		} else {
			Files.copy(child, copied.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public void saveDefaultConfig() {
		if (!this.mapConfigFile.exists()) {
			this.plugin.saveResource("mapdata.yml", false);
		}
	}

	public void reloadConfig() {
		this.mapConfig = YamlConfiguration.loadConfiguration(this.mapConfigFile);
	}

	public @NotNull MapDataContainer getMapContainer(@NotNull AbstractGameClassifier classifier) {
		List<MapDataContainer> containers = Preconditions.checkNotNull(this.maps.get(classifier));
		return containers.get(this.random.nextInt(containers.size()));
	}
}
