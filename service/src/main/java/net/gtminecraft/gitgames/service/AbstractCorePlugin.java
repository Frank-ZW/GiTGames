package net.gtminecraft.gitgames.service;

import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class AbstractCorePlugin extends JavaPlugin {

	@Getter
	protected NamespacedKey key;
	@Getter
	protected Chat chat;
	protected Advancement netherAdvancement;
	protected Advancement endAdvancement;
	@Getter
	protected static AbstractCorePlugin instance;

	public void grantAdvancement(@NotNull Player player, @NotNull Advancement advancement) {
		AdvancementProgress progress = player.getAdvancementProgress(advancement);
		Collection<String> remaining = progress.getRemainingCriteria();
		for (String s : remaining) {
			progress.awardCriteria(s);
		}
	}

	public void grantNetherAdvancement(@NotNull Player player) {
		this.grantAdvancement(player, this.netherAdvancement);
	}

	public void grantEndAdvancement(@NotNull Player player) {
		this.grantAdvancement(player, this.endAdvancement);
	}
}
