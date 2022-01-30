package net.gtminecraft.gitgames.compatability;

import net.gtminecraft.gitgames.compatability.wrapper.ChannelWrapper;
import net.gtminecraft.gitgames.compatability.wrapper.PacketWrapper;

public abstract class PacketHandler extends AbstractPacketHandler {

	public boolean shouldHandle(PacketWrapper wrapper) throws Exception {
		return true;
	}

	public void connected(ChannelWrapper wrapper) throws Exception {}

	public void disconnected(ChannelWrapper wrapper) throws Exception {}

	public void exception(Throwable t) throws Exception {
		throw new Exception(t);
	}

	public abstract void handle(PacketWrapper wrapper) throws Exception;
}
