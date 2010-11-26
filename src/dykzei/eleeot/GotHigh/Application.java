package dykzei.eleeot.GotHigh;

import android.content.Context;
import dykzei.eleeot.GotHigh.network.IAIBParser;
import dykzei.eleeot.GotHigh.network.Parser4chan;

public class Application extends android.app.Application {

	private static Application self;
	private static IAIBParser parser;
	
	public static IAIBParser getParser(){
		if(parser == null)
			parser = new Parser4chan();
		return parser;
	}
	
	public static Context getContext(){
		return self.getApplicationContext();
	}
	
	@Override
	public void onLowMemory() {
		synchronized(parser){
			parser = null;
		}
		DB.suicide();

		super.onLowMemory();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		self = this;
	}

	@Override
	public void onTerminate() {
		self = null;
		super.onTerminate();
	}
	
	
	
	
}
