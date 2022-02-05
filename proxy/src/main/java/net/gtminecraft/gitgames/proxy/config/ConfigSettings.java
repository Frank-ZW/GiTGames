package net.gtminecraft.gitgames.proxy.config;

import lombok.Getter;
import net.gtminecraft.gitgames.compatability.mechanics.ServerType;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.util.CaseInsensitiveSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;
import java.util.logging.Level;

public class ConfigSettings {

	private final CoreProxyPlugin plugin;
	private Set<String> fallbacks;
	private Set<String> minigames;
	private Set<String> lobbies;
	@Getter
	private int port;
	private boolean connected = true;

	public ConfigSettings(CoreProxyPlugin plugin) {
		this.plugin = plugin;
		File file = new File(plugin.getDataFolder(), "configuration.yml");
		if (!file.exists()) {
			try (InputStream input = plugin.getResourceAsStream("configuration.yml")) {
				Files.copy(input, file.toPath());
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "Failed to create configuration.yml file", e);
				this.connected = false;
				return;
			}
		}

		try {
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
			this.fallbacks = new CaseInsensitiveSet(config.getStringList("servers.fallback"));
			this.minigames = new CaseInsensitiveSet(config.getStringList("servers.minigame"));
			this.lobbies = new CaseInsensitiveSet(config.getStringList("servers.lobby"));
			this.port = config.getInt("connection-settings.plugin.port");
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to get configuration provider loader", e);
			this.connected = false;
		}
	}

	@Nullable
	public ServerInfo fallbackServer(String exclusion) {
		for (String s : this.fallbacks) {
			ServerInfo serverInfo = this.plugin.getProxy().getServerInfo(s);
			if (serverInfo != null && !serverInfo.getName().equalsIgnoreCase(exclusion)) {
				return serverInfo;
			}
		}

		return null;
	}

	public ServerType getServerType(String name) {
		return this.lobbies.contains(name) ? ServerType.LOBBY : (this.minigames.contains(name) ? ServerType.MINIGAME : ServerType.OTHER);
	}

	public boolean isMinigame(String name) {
		return this.minigames.contains(name);
	}

	public boolean isLobby(String name) {
		return this.lobbies.contains(name);
	}

	public boolean isConnected() {
		return this.connected;
	}
}
