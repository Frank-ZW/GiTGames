package net.gtminecraft.gitgames.compatability.wrapper;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.gtminecraft.gitgames.compatability.DefinedPacket;

@RequiredArgsConstructor
public class PacketWrapper {

	@Getter
	private final DefinedPacket packet;
	@Getter
	private final ByteBuf buf;
	@Setter
	private boolean released;

	public void trySingleRelease() {
		if (!this.released) {
			this.buf.release();
			this.released = true;
		}
	}
}
