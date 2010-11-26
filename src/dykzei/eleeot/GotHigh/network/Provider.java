package dykzei.eleeot.GotHigh.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.Logger;
import dykzei.eleeot.GotHigh.chan.ChThread;

public class Provider {

	
	
	public static void getBoardThreads(final String board, final IThreadReceiver receiver){
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				List<ChThread> list = new ArrayList<ChThread>();
				IAIBParser parser = Application.getParser();
				String url = parser.getBoardUrl(board);
				String content;
				try{					
					content = get(url);
					if(content == null || content.length() == 0)
						throw new Exception("not found");
				}catch(Exception e){
					Logger.w("" + e);
					return;
				}
				
//				String[] rawAibThreads = parser.getBoardThreads(content);
//				
//				for(String rawAibThread : rawAibThreads){
//					Thread rThread = AIBThread.synthAIBThreadTree(Settings.currentBoard, rawAibThread, null);
//					if(rThread != null){
//						getHoster().addAibThread(rThread);
//						if(Settings.showReplies){
//							RemoteThread prevRThread = rThread;
//							while(prevRThread.getNext() != null){
//								getHoster().addAibThread(prevRThread.getNext());
//								prevRThread = prevRThread.getNext();
//							}
//						}
//					}
//				}
				
				receiver.onThreadsFetchComplete();				
			}
		})).start();
		
	}
	
	private static String get(String sUrl) {
        String str;
        StringBuffer buff = new StringBuffer();
        try {
            URL url = new URL(sUrl);
            URLConnection con = url.openConnection();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((str = in.readLine()) != null) {
                buff.append(str);
            }
            str = buff.toString();
                
        } catch (Exception e) {
            str = "";
        }
        return str;
	}
}
