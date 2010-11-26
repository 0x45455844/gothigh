package dykzei.eleeot.GotHigh;

import java.io.File;

import android.content.Context;
import android.os.Environment;
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
		prepareSD();
	}

	@Override
	public void onTerminate() {
		self = null;
		super.onTerminate();
	}
	
	public static String getRootPath(){
		StringBuffer sdcard = new StringBuffer(); 
		sdcard.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		
		if(sdcard.charAt(sdcard.length()-1) != '/')
			sdcard.append('/');
		
		sdcard.append("gothigh/");
		
		return sdcard.toString();
	}
	
	public static String getCacheImgPath(String fname){
		return getRootPath() + ".cache/" + cachedImageExt(fname);
	}
	
	private static void prepareSD(){
		File f = new File(getRootPath(), "");
		if(!f.exists())
			f.mkdirs();
		
		f = new File(getCacheImgPath(""), "");
		if(!f.exists())
			f.mkdirs();
	}
	
	private static String cachedImageExt(String fn){
		if(fn != null && !fn.equals("")){
			int dotpos = fn.lastIndexOf('.');
			if(dotpos > 0){
				return fn.substring(0, dotpos);
			}
		}
		return fn;
	}
	
	
}
