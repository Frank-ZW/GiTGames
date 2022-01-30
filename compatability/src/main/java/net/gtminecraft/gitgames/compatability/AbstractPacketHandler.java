package net.gtminecraft.gitgames.compatability;

import net.gtminecraft.gitgames.compatability.packet.*;

public abstract class AbstractPacketHandler {

	public void handle(PacketHandshake packet) throws Exception {}
	public void handle(PacketKeepAlive packet) throws Exception {}
	public void handle(PacketServerDisconnect packet) throws Exception {}
	public void handle(PacketShutdown packet) throws Exception {}
	public void handle(PacketGameUpdate packet) throws Exception {}
	public void handle(PacketPlayerConnect packet) throws Exception {}
	public void handle(PacketPlayerQueue packet) throws Exception {}
	public void handle(PacketServerAction packet) throws Exception {}
	public void handle(PacketCreateGame packet) throws Exception {}
}
