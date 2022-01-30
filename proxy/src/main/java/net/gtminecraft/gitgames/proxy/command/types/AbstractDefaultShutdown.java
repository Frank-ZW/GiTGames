package net.gtminecraft.gitgames.proxy.command.types;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDefaultShutdown extends AbstractShutdown {

	public AbstractDefaultShutdown(CoreProxyPlugin plugin, String permission) {
		super(plugin, permission);
	}

	public abstract void accept(@NotNull CommandSender sender, String[] args);
}
