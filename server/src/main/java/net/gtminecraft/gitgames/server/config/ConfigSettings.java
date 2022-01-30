package net.gtminecraft.gitgames.server.config;

import lombok.Getter;
import net.gtminecraft.gitgames.server.CorePlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigSettings {

	private final CorePlugin plugin;
	private final FileConfiguration config;
	@Getter
	private Location lobby;
	@Getter
	private String name;
	@Getter
	private String host;
	@Getter
	private int port;
	@Getter
	private String pluginHost;
	@Getter
	private int pluginPort;
	@Getter
	private boolean initialStartup;

	public ConfigSettings(CorePlugin plugin) {
		this.plugin = plugin;
		File file = new File(plugin.getDataFolder(), "config.yml");
		if (!file.exists()) {
			this.initialStartup = true;
			plugin.saveDefaultConfig();
		}

		this.config = plugin.getConfig();
	}

	public void readSettings() {
		this.name = this.config.getString("connection-settings.server.name");
		this.host = this.config.getString("connection-settings.server.host");
		this.port = this.config.getInt("connection-settings.server.port");
		this.pluginHost = this.config.getString("connection-settings.plugin.host");
		this.pluginPort = this.config.getInt("connection-settings.plugin.port");

		String worldName = this.config.getString("lobby-location.world-name");
		if (worldName == null) {
			Bukkit.getLogger().warning("An invalid entry has been detected for the world name in the lobby. Before reloading the plugin, double check that all values are appropriately entered.");
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			return;
		}

		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			Bukkit.getLogger().warning("Failed to retrieve a world with the name '" + StringUtils.capitalize(worldName) + "'. Is it loaded into memory?");
			Bukkit.getPluginManager().disablePlugin(this.plugin);
			return;
		}

		String yEntry = this.config.getString("lobby-location.y-value");
		if (yEntry == null) {
			Bukkit.getLogger().warning("An invalid entry has been detected in the y-coordinate for the lobby location. Before reloading the plugin, double check that all values are appropriately entered.");
			return;
		}

		try {
			int x = this.config.getInt("lobby-location.x-value");
			int z = this.config.getInt("lobby-location.z-value");
			int y;
			if ("HIGHEST".equalsIgnoreCase(yEntry)) {
				Bukkit.getLogger().info(ChatColor.GREEN + "The keyword 'HIGHEST' has been entered for the y-coordinate... setting the y-coordinate to the highest y-value at (" + x + ", " + z + ")");
				y = world.getHighestBlockYAt(x, z);
			} else {
				y = Integer.parseInt(yEntry);
			}

			double yaw = this.config.getDouble("lobby-location.yaw");
			double pitch = this.config.getDouble("lobby-location.pitch");
			this.lobby = new Location(world, x, y, z, (float) yaw, (float) pitch);
		} catch (NumberFormatException e) {
			Bukkit.getLogger().warning("One or more entries for the lobby location is not a number. Before reloading the plugin, double check that all values are appropriately entered.");
			Bukkit.getPluginManager().disablePlugin(this.plugin);
		}
	}

	public void saveSettings() {
		this.plugin.saveConfig();
	}
}
