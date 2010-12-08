package dykzei.eleeot.GotHigh.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.Logger;
import dykzei.eleeot.GotHigh.Settings;

public class ImgDownloader implements Runnable{
	
	class Ant implements Runnable{

		private IDownloadDone downloadDone;
		private String url;
		private String path;
		private Thread currentThread;
		private boolean alive;
		
		public Ant(IDownloadDone idd, String url, String path){
			this.downloadDone = idd;
			this.url = url;
			this.path = path;
		}
		
		@Override
		public void run() {	
			alive = true;
			currentThread = Thread.currentThread();
			currentThread.setPriority(3);
			downloadDone.downloadDone(downloadFile(url, path));
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
		if(remote != null && !remote.equals(""))
			getSelf().scheduleCacheDownloadInt(remote, idd);
	}
	
	private void scheduleCacheDownloadInt(String url, IDownloadDone idd){
		synchronized (queue) {
			String filename = filenameFromUrl(url);
			String path = Application.getCacheImgPath(filename);
			queue.addFirst(new Ant(idd, url, path));
		}		
	}
	
	public static void scheduleHirezDownload(String remote, IDownloadDone idd){
		if(remote != null && !remote.equals(""))
			getSelf().scheduleHirezDownloadInt(remote, idd);
	}
	
	private void scheduleHirezDownloadInt(String url, IDownloadDone idd){
		synchronized (queue) {
			String filename = filenameFromUrl(url);
			String path = Application.getHirezImgPath(filename);
			queue.addFirst(new Ant(idd, url, path));
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

						Arrays.sort(subFiles, new Comparator<File>() {

							public int compare(final File o1, final File o2) {
								long l1 = o1.lastModified();
								long l2 = o2.lastModified();
								if(l1>l2)
									return 1;
								if(l1<l2)
									return -1;
								return 0;
						    }
						});
						
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
				
				Thread.sleep(512);
				
				if(threads.isEmpty()){
					idleTime += 512;
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
	
	public static boolean downloadFile(String url, String path){
		Thread.currentThread().setPriority(3);
		try {
			URL oUrl = new URL(url);
			
			InputStream is = null;
			try{
				is = oUrl.openStream();
			}catch(IOException e){
				is = null;
			}			
			
			if(is != null && path != null){
				try {
					File f = new File(path);
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
			    	is.close();
			    	return false;
				}
				is.close();
			}						
		} catch (Exception e) {
			Logger.w(e.toString());
			return false;
		}
		
		return true;
	}
}
