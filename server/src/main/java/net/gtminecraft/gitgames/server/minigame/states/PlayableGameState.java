package net.gtminecraft.gitgames.server.minigame.states;

import net.gtminecraft.gitgames.server.util.MinecraftUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public abstract class PlayableGameState extends GameState {

	public PlayableGameState(int priority) {
		super(priority);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		this.minigameManager.getSpectatorQueue().computeIfPresent(player.getUniqueId(), (k, v) -> {
			Player target = Bukkit.getPlayer(v);
			if (target == null) {
				this.minigameManager.connectToProxyLobby(player);
				return null;
			}

			player.teleportAsync(target.getLocation()).thenAccept(result -> {
				if (result) {
					this.game.hideSpectator(player);
					MinecraftUtil.setSpectator(player);
					player.sendMessage(ChatColor.GREEN + "You are now spectating " + target.getName() + ".");
				} else {
					player.sendMessage(ChatColor.RED + "Failed to teleport you to " + target.getName() + ". You have been connected back to the lobby.");
					this.minigameManager.connectToProxyLobby(player);
				}
			});

			return null;
		});

		this.game.getDisconnections().computeIfPresent(player.getUniqueId(), (k, v) -> {
			v.cancel();
			if (this.game.isPlayer(k) && !this.game.isSpectator(k)) {
				Bukkit.broadcast(this.game.gameDisplayName(player).append(Component.text(ChatColor.GRAY + " reconnected.")));
			}

			return null;
		});
	}
}
