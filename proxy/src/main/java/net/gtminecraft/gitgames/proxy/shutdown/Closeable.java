package net.gtminecraft.gitgames.proxy.shutdown;

public interface Closeable {

	void cancelShutdown(String who);
	void scheduleShutdown(int duration);
}
