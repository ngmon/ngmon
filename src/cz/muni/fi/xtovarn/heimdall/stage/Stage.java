package cz.muni.fi.xtovarn.heimdall.stage;

public interface Stage<T_in, T_out> extends Runnable {

	@Override
	void run();

	public T_out work(T_in workItem);
}
