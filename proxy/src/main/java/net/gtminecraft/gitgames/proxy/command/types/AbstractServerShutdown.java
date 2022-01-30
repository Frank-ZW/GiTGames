package net.gtminecraft.gitgames.proxy.command.types;

import net.gtminecraft.gitgames.proxy.CoreProxyPlugin;
import net.gtminecraft.gitgames.proxy.data.ServerData;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractServerShutdown extends AbstractShutdown {

	public AbstractServerShutdown(CoreProxyPlugin plugin, String permission) {
		super(plugin, permission);
	}

	public abstract void accept(@NotNull CommandSender sender, @NotNull ServerData serverData, String[] args);
}