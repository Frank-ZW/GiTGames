package net.gtminecraft.gitgames.proxy.data;

import lombok.Getter;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.mechanics.PlayerStatus;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerData {

	@Getter
	private final ProxiedPlayer player;
	@Getter
	private final String name;
	@Getter
	private final UUID uniqueId;
	@Getter
	@Setter
	private PlayerStatus status;

	public PlayerData(ProxiedPlayer player) {
		this.player = player;
		this.name = player.getName();
		this.uniqueId = player.getUniqueId();
		this.status = PlayerStatus.INACTIVE;
	}

	public void connect(@Nullable ServerInfo server) {
		if (server == null) {
			return;
		}

		this.player.connect(server);
	}
}
