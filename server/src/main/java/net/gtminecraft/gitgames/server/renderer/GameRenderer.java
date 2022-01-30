package net.gtminecraft.gitgames.server.renderer;

import io.papermc.paper.chat.ChatRenderer;
import net.gtminecraft.gitgames.service.mechanics.AbstractMinigame;
import net.gtminecraft.gitgames.service.util.StringUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameRenderer implements ChatRenderer {

	private final AbstractMinigame minigame;

	public GameRenderer(AbstractMinigame minigame) {
		this.minigame = minigame;
	}

	@Override
	public @NotNull Component render(@NotNull Player player, @NotNull Component displayName, @NotNull Component message, @NotNull Audience viewer) {
		return (this.minigame.isSpectator(player.getUniqueId()) ? StringUtil.SPECTATOR_PREFIX : StringUtil.GAME_PREFIX).append(this.minigame.playerChatHandler(player)).append(Component.text(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + " » " + ChatColor.RESET + ChatColor.WHITE)).append(message);
	}
}
