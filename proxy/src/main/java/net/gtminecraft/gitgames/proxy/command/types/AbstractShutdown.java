package net.gtminecraft.gitgames.proxy.command.types;

import lombok.AllArgsConstructor;
import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public abstract class AbstractShutdown {

	protected final CoreProxyPlugin plugin;
	protected final String permission;

	protected boolean hasPermission(@NotNull CommandSender sender) {
		return this.permission == null || this.permission.isEmpty() || sender.hasPermission(this.permission);
	}
}