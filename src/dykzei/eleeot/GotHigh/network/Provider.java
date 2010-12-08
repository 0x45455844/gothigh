package dykzei.eleeot.GotHigh.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

import android.text.Html;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.Logger;
import dykzei.eleeot.GotHigh.chan.ChMessage;

public class Provider {
	
	public static void getBoardThreads(final String board, final WeakReference<IThreadReceiver> receiver){
		(new Thread(new Runnable() {			
			@Override
			public void run() {
				IAIBParser parser = Application.getParser();
				String url = parser.getBoardUrl(board);
				String content;
				try{					
					content = get(url);
					if(content == null || content.length() == 0)
						throw new Exception(url + ": unavailable.");
				}catch(Exception e){
					Logger.w("" + e);
					if(receiver.get() != null){
						receiver.get().onComplete();
					}
					return;
				}				
				String[] raws = parser.getBoardThreads(content);				
								
				for(String raw : raws){
					String rawHeadMessage = parser.getBoardThreadHeadMessage(raw);
					ChMessage message = new ChMessage(parser.getId(rawHeadMessage));
					message.text = Html.fromHtml(parser.getText(rawHeadMessage)).toString();
					message.image = parser.getImage(rawHeadMessage);
					message.fullImage = parser.getFullImage(rawHeadMessage);
					message.fullImageInfo = parser.getFullImageInfo(rawHeadMessage);
					message.date = parser.getDate(rawHeadMessage);
					message.ommit = parser.getOmmited(rawHeadMessage, false);
					message.ommit += parser.getChildCount(raw, false);
					message.ommitImg = parser.getOmmited(rawHeadMessage, true);
					message.ommitImg += parser.getChildCount(raw, true);
					message.subject = parser.getSubject(rawHeadMessage);
					if(receiver.get() == null ){
						break;
					}
					DB.addBoardMessage(message);
				}				
				if(receiver.get() != null){
					receiver.get().onComplete();
				}		
			}
		})).start();		
	}
	
	public static void getThreadMessages(final String board, final String id, final IThreadReceiver receiver){
		(new Thread(new Runnable() {			
			@Override
			public void run() {
				IAIBParser parser = Application.getParser();
				String url = parser.getMessageUrl(board, id);
				String content;
				try{					
					content = get(url);
					if(content == null || content.length() == 0)
						throw new Exception(url + ": unavailable.");
				}catch(Exception e){
					Logger.w("" + e);
					receiver.onComplete();
					return;
				}				
				String[] raws = parser.getThreadMessages(content);				
								
				for(String raw : raws){
					ChMessage message = new ChMessage(parser.getId(raw));
					message.parentId = id;
					message.text = Html.fromHtml(parser.getText(raw)).toString();
					message.image = parser.getImage(raw);
					message.fullImage = parser.getFullImage(raw);
					message.fullImageInfo = parser.getFullImageInfo(raw);
					message.date = parser.getDate(raw);
					message.subject = parser.getSubject(raw);
					message.board = board;
					DB.addPoolMessage(message);
				}				
				receiver.onComplete();				
			}
		})).start();		
	}
	
	private static String get(String sUrl) {
        String str;
        StringBuffer buff = new StringBuffer();
        try {
            URL url = new URL(sUrl);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(20000);
            
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
