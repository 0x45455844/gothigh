package dykzei.eleeot.GotHigh.chan;

import java.util.List;

public class ChPool {
	protected List<ChThread> threads;
	
	public void add(ChThread thread){
		threads.add(thread);
	}
}