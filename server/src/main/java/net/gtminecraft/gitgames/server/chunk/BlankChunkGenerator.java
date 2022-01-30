package net.gtminecraft.gitgames.server.chunk;

import org.bukkit.generator.ChunkGenerator;

public class BlankChunkGenerator extends ChunkGenerator {

	@Override
	public boolean shouldGenerateNoise() {
		return false;
	}

	@Override
	public boolean shouldGenerateSurface() {
		return false;
	}

	@Override
	public boolean shouldGenerateBedrock() {
		return false;
	}

	@Override
	public boolean shouldGenerateCaves() {
		return false;
	}

	@Override
	public boolean shouldGenerateDecorations() {
		return false;
	}

	@Override
	public boolean shouldGenerateMobs() {
		return false;
	}

	@Override
	public boolean shouldGenerateStructures() {
		return false;
	}
}
