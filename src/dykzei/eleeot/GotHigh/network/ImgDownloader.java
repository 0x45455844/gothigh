package dykzei.eleeot.GotHigh.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.Logger;
import dykzei.eleeot.GotHigh.Settings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImgDownloader implements Runnable{
	
	class Ant implements Runnable{

		private IDownloadDone downloadDone;
		private String url;
		private Thread currentThread;
		private boolean alive;
		
		public Ant(IDownloadDone idd, String url){
			this.downloadDone = idd;
			this.url = url;
		}
		
		@Override
		public void run() {	
			alive = true;
			currentThread = Thread.currentThread();
			currentThread.setPriority(3);
			try {
				URL oUrl = new URL(url);
				
				InputStream is = null;
				try{
					is = oUrl.openStream();
				}catch(IOException e){
					is = null;
				}
				String filename = filenameFromUrl(url);
				
				if(is != null && filename != null){
					try {
						File f = new File(Application.getCacheImgPath(filename));
						FileOutputStream fos = new FileOutputStream(f, false);
						byte[] buff = new byte[1024];
						do{
							int count = is.read(buff);
							if(count > 0){
								fos.write(buff, 0, count);
								fos.flush();
							}else{
								break;
							}
						}while(true);
					    
					    fos.close();
				    } catch (Exception e) {
				    	Logger.w(e.toString());
					}
					is.close();
					Bitmap bmp = BitmapFactory.decodeFile(Application.getCacheImgPath(filename));
					downloadDone.downloadDone(bmp);
				}						
			} catch (Exception e) {
				Logger.w(e.toString());
			}
			alive = false;
		}
		
		public boolean isAlive(){
			return alive;
		}
		
		public void kill(){
			alive = false;
			currentThread.interrupt();
		}
	}
	
	private static ImgDownloader self;
	
	private static final int MAX_THREADS = 10;
	private static final long MAX_IDLE_TIME = 60000;
	
	private List<Ant> threads = new ArrayList<Ant>();
	private LinkedList<Ant> queue = new LinkedList<Ant>();
	private boolean isAlive;
	private long idleTime;
	
	private ImgDownloader(){
		isAlive = true;
		new Thread(this).start();
	}
	
	public static void destroy(){
    	if(self != null)
    		self.kill();
    	self = null;
    }
	
	private static ImgDownloader getSelf(){
		if(self == null || !self.isAlive)
			self = new ImgDownloader();
		return self;
	}
    
	public static void scheduleCacheDownload(String remote, IDownloadDone idd){
		getSelf().scheduleCacheDownloadInt(remote, idd);
	}
	
	private void scheduleCacheDownloadInt(String url, IDownloadDone idd){
		synchronized (queue) {
			queue.addFirst(new Ant(idd, url));
		}		
	}
	
	public static void shrinkCache(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				try{
					Thread.currentThread().setPriority(3);
					String cacheRoot = Application.getCacheImgPath("");
					File cr = new File(cacheRoot);
					
					long cacheSize = getDirSize(cr);
					if(cacheSize > Settings.maxCache){
						File[] subFiles = cr.listFiles();
						long diff = cacheSize - Settings.maxCache,
							 correction = 0;
						for(File sf : subFiles){
							correction += sf.length();
							sf.delete();
							if(correction > diff)
								break;
						}
					}
				}catch(Exception e){
					Logger.w(e.toString());
				}
			}			
		}).start();
	}
	
	private static long getDirSize(File dir) {
		long size = 0;
		if (dir.isFile()) {
			size = dir.length();
		} else {
			File[] subFiles = dir.listFiles();
			for (File file : subFiles)
				size += getDirSize(file);
		}
		return size;
	}
	
	public void kill(){
		synchronized (queue) {
			queue.clear();
		}
		
		for(Ant a : threads){
			a.kill();
		}
		
		isAlive = false;
	}
	
	@Override
	public void run() {
		while(isAlive){
			try {
				
				for(int i = 0; i < threads.size(); i++){
					if(!threads.get(i).isAlive()){
						threads.remove(i);
						i--;
					}
				}
				
				if(threads.size() < MAX_THREADS){
 					synchronized (queue) {
						Ant a = queue.poll();
						if(a != null){
							Thread t = new Thread(a);
							threads.add(a);
							t.start();
						}
					}					
				}
				
				Thread.sleep(256);
				
				if(threads.isEmpty()){
					idleTime += 256;
				}else{
					idleTime = 0;
				}
				
				if(idleTime >= MAX_IDLE_TIME)
					isAlive = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String filenameFromUrl(String url){
		if(url != null){
			int p = url.lastIndexOf("/");
			if(p > 0 && url.length() > (p + 1))
				return url.substring(p + 1);
		}
		return url;
	}
}
