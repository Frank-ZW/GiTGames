package net.gtminecraft.gitgames.server.renderer;

import io.papermc.paper.chat.ChatRenderer;
import net.gtminecraft.gitgames.server.minigame.AbstractGame;
import net.gtminecraft.gitgames.server.util.StringUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GameRenderer implements ChatRenderer {

	private final AbstractGame game;

	public GameRenderer(AbstractGame game) {
		this.game = game;
	}

	@Override
	public @NotNull Component render(@NotNull Player player, @NotNull Component displayName, @NotNull Component message, @NotNull Audience viewer) {
		return (this.game.isSpectator(player.getUniqueId()) ? StringUtil.SPECTATOR_PREFIX : StringUtil.GAME_PREFIX).append(this.game.playerChatHandler(player)).append(Component.text(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + " » " + ChatColor.RESET + ChatColor.WHITE)).append(message);
	}
}
