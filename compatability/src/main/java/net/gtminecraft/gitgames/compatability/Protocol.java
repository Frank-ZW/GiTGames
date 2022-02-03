package net.gtminecraft.gitgames.compatability;

import lombok.Getter;
import net.gtminecraft.gitgames.compatability.packet.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum Protocol {

	HANDSHAKE
	{
		{
			this.TO_CLIENT.registerPacket(PacketHandshake.class, PacketHandshake::new, 0x00);
			this.TO_SERVER.registerPacket(PacketHandshake.class, PacketHandshake::new, 0x00);
		}
	},

	PLAY
	{
		{
			this.TO_CLIENT.registerPacket(PacketShutdown.class, PacketShutdown::new, 0x00);
			this.TO_CLIENT.registerPacket(PacketServerDisconnect.class, PacketServerDisconnect::new, 0x01);
			this.TO_CLIENT.registerPacket(PacketKeepAlive.class, PacketKeepAlive::new, 0x02);
			this.TO_CLIENT.registerPacket(PacketPlayerQueue.class, PacketPlayerQueue::new, 0x03);
			this.TO_CLIENT.registerPacket(PacketServerAction.class, PacketServerAction::new, 0x04);
			this.TO_CLIENT.registerPacket(PacketCreateGame.class, PacketCreateGame::new, 0x05);

			this.TO_SERVER.registerPacket(PacketShutdown.class, PacketShutdown::new, 0x00);
			this.TO_SERVER.registerPacket(PacketServerDisconnect.class, PacketServerDisconnect::new, 0x01);
			this.TO_SERVER.registerPacket(PacketKeepAlive.class, PacketKeepAlive::new, 0x02);
			this.TO_SERVER.registerPacket(PacketGameUpdate.class, PacketGameUpdate::new, 0x03);
			this.TO_SERVER.registerPacket(PacketPlayerConnect.class, PacketPlayerConnect::new, 0x04);
			this.TO_SERVER.registerPacket(PacketPlayerDataUpdate.class, PacketPlayerDataUpdate::new, 0x05);
			this.TO_SERVER.registerPacket(PacketPlayerDisconnect.class, PacketPlayerDisconnect::new, 0x06);
		}
	};

	private static final int MAX_PACKET_ID = 255;
	public final DirectionData TO_SERVER = new DirectionData(Direction.TO_SERVER);
	public final DirectionData TO_CLIENT = new DirectionData(Direction.TO_CLIENT);

	public static final class DirectionData {

		@Getter
		private final Direction direction;
		private final ProtocolData protocolData;

		public DirectionData(Direction direction) {
			this.direction = direction;
			this.protocolData = new ProtocolData();
		}

		public int getId(Class<? extends DefinedPacket> clazz) {
			return this.protocolData.packetMap.get(clazz);
		}

		private void registerPacket(Class<? extends DefinedPacket> clazz, Supplier<? extends DefinedPacket> constructor, int packetId) {
			this.protocolData.packetMap.put(clazz, packetId);
			this.protocolData.constructors[packetId] = constructor;
		}

		@Nullable
		public DefinedPacket createPacket(int packetId) {
			Supplier<? extends DefinedPacket> constructor = this.protocolData.constructors[packetId];
			return constructor == null ? null : constructor.get();
		}
	}

	public enum Direction {
		TO_SERVER, TO_CLIENT
	}

	@SuppressWarnings("unchecked")
	private static class ProtocolData {

		private final Map<Class<? extends DefinedPacket>, Integer> packetMap = new HashMap<>(Protocol.MAX_PACKET_ID);
		private final Supplier<? extends DefinedPacket>[] constructors = new Supplier[Protocol.MAX_PACKET_ID];
	}
}
