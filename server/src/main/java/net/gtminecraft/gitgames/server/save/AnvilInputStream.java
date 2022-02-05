package net.gtminecraft.gitgames.server.save;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AnvilInputStream extends DataInputStream {

	private static final int NIBBLE_ARRAY_LENGTH = 0x800;

	public AnvilInputStream(@NotNull InputStream in) {
		super(in);
	}

	public int[] readIntArray(int length) throws IOException {
		int[] array = new int[length];
		for (int i = 0; i < length; i++) {
			array[i] = this.readInt();
		}

		return array;
	}

	public byte[] readByteArray(int length) throws IOException {
		byte[] array = new byte[length];
		for (int i = 0; i < length; i++) {
			array[i] = this.readByte();
		}

		return array;
	}
}
