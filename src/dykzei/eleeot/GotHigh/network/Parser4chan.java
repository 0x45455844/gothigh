package dykzei.eleeot.GotHigh.network;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser4chan implements IAIBParser {
	private static Pattern pattern;
	private static Matcher matcher;
	
	private static final String[] boards = new String[]{
		"a", "b", "c", "d", "e", "g", "gif", "h",
		"hr", "k", "m", "o", "p", "r", "s", "t",
		"u", "v", "w", "wg", "i", "ic", "cm", "y",
		"r9k", "3", "adv", "an", "cgl", "ck", "co",
		"fa", "fit", "int", "jp", "lit", "mu", "n",
		"new", "po", "sci", "sp", "tg", "toy", "trv",
		"tv", "x"
	};
	
	private static final String[] boardNames = new String[]{
		"Anime & Manga", "Random", "Anime/Cute", "Hentai/Alternative", 
		"Ecchi", "Technology", "Animated GIF", "Hentai", "High Resolution",
		"Weapons", "Mecha", "Auto", "Photo", "Request", "Sexy Beautiful Women",
		"Torrents", "Yuri", "Video Games", "Anime/Wallpapers", "Wallpapers/General",
		"Oekaki", "Artwork/Critique", "Cute/Male", "Yaoi", "ROBOT9000", "3DCG", 
		"Advice", "Animals & Nature", "Cosplay & EGL", "Food & Cooking", 
		"Comics & Cartoons", "Fashion", "Health & Fitness", "International", 
		"Otaku Culture", "Literature", "Music", "Transportation", "News", 
		"Papercraft & Origami", "Science & Math", "Sports", "Traditional Games", 
		"Toys", "Travel", "Television & Film", "Paranormal"
	};
	
	private String getInner(String rawCancer, String A, String B, String regex){
		return getInner(rawCancer, A, B, regex, 0, 0);
	}
	
	private String getInner(String rawCancer, String A, String B, String regex, int correctionA, int correctionB){
		Pattern pattern = Pattern.compile(A + regex + B);
		Matcher matcher = pattern.matcher(rawCancer);
		if(matcher.find()){
			String rez = matcher.group();
			int lenA = A.length() - correctionA;
			int lenB = B.length() - correctionB;
			return rez.substring(lenA, rez.length() - lenB);
		}
		return "";
	}
	
	@Override
	public String getHostUrl() {
		return "http://boards.4chan.org";
	}
	
	@Override
	public String getBoardUrl(String board) {
		return getHostUrl() + "/" + board;
	}
	
	@Override
	public String getAibName() {
		return "4chan";
	}
	
	@Override
	public String[] getAibBoards() {
		return boards;
	}

	@Override
	public String[] getAibBoardNames() {
		return boardNames;
	}
	
	@Override
	public String getBoardCode(int index) {
		if(index >= 0 && index < boards.length)
			return boards[index];
		
		return boards[0];
	}

	@Override
	public String getBoardName(int index) {
		if(index >= 0 && index < boardNames.length)
			return boardNames[index];
		
		return boardNames[0];
	}
	
	@Override
	public int getAibMaxPages() {
		return 15;
	}
	
	@Override
	public String[] getBoardThreads(String raw) {
		String RCStart = "<span class=\"filesize\">";
		String RCEnd = "<tr><td>Previous</td><td>\\[";
		
		pattern = Pattern.compile(RCStart + ".*" + RCEnd, Pattern.DOTALL);		
		matcher = pattern.matcher(raw);
		
		if(!matcher.find()){
			RCEnd = "Previous\" accesskey=\"z\"></td></form><td>\\[";
			
			pattern = Pattern.compile(RCStart + ".*" + RCEnd, Pattern.DOTALL);		
			matcher = pattern.matcher(raw);
			
			if(!matcher.find()){
				return new String[]{""};
			}
		}
		
		String inter = matcher.group();
		String cancer = inter.substring(RCStart.length(), inter.length() - RCEnd.length());
		
		pattern = Pattern.compile(".*?<hr", Pattern.DOTALL);
		matcher = pattern.matcher(cancer);
		
		List<String> list = new ArrayList<String>();
		
		while(matcher.find()){
			list.add(matcher.group());
		}
		
		list.remove(list.size() - 1);
		
		return list.toArray(new String[list.size()]);
	}

	@Override
	public String getBoardThreadHeadMessage(String raw) {
		pattern = Pattern.compile(".*?<table");
		matcher = pattern.matcher(raw);
		if(matcher.find()){
			return matcher.group();
		}else{
			pattern = Pattern.compile(".*?<\\/blockquote>");
			matcher = pattern.matcher(raw);
			if(matcher.find()){
				return matcher.group();
			}
		}
		return "";
	}
	
	@Override
	public String getId(String raw) {
		String id = getInner(raw, "<span id=\"norep", "\">", ".*?");
		if(id == null || id.length() == 0){
			id = getInner(raw, "<span id=\"nothread", "\">", ".*?");
		}
		return id;
	}
	
	@Override
	public String getText(String raw) {
		return getInner(raw, "<blockquote>", "</blockquote>", ".*?");
	}
	
	@Override
	public String getImage(String raw) {
		return getInner(raw, "<img src=", " border=0 ali", ".*?");
	}
	
	@Override
	public String getDate(String raw) {
		String value = getInner(raw, "/span> ", " <span id=\"no", ".*?");
		while(value.indexOf("span") > 0){
			value = getInner(value, "</span> ", "", ".*");
		}		
		return value;
	}
	
	@Override
	public int getOmmited(String raw, boolean withImage) {
		String om_area = getInner(raw, "omittedposts\">", "</span", ".*?");		
		try{
			return Integer.parseInt(withImage 
					? getInner(om_area, "sts and ", " image repl", ".*?")
					: getInner(om_area, "", " posts ", ".*?"));
		}catch(Exception e){
			return 0;
		}
	}
	
	@Override
	public String getSubject(String raw) {
		String subj = getInner(raw, "<span class=\"filetitle\">", "</span>", ".*?");
		if(subj.length() == 0)
			subj = getInner(raw, "<span class=\"replytitle\">", "</span>", ".*?");		
		return subj;
	}
	
	@Override
	public int getChildCount(String raw, boolean images){
		if(images){
			return 0; // TODO: help to know it?
		}else{
			pattern = Pattern.compile("<table><tr.*?/tr></table>");
			Matcher matcher = pattern.matcher(raw);
			int count = 0;
			while(matcher.find()){
				count++;
			}
			return count;
		}
	}
	
	@Override
	public String getMessageUrl(String board, String id) {
		String url = getHostUrl() + "/" + board + "/res/" + id;		
		return url;
	}
	
	public String[] getThreadMessages(String raw){
		String RCStart = "<span class=\"filesize\">";
		String RCEnd = "<hr>";
		
		pattern = Pattern.compile(RCStart + ".*?" + RCEnd, Pattern.DOTALL);		
		matcher = pattern.matcher(raw);
		
		String rawThread = null;		
		if(matcher.find()){
			rawThread = matcher.group();
		}
		
		pattern = Pattern.compile(".*?<table");
		matcher = pattern.matcher(rawThread);
		
		String first = null;
		if(matcher.find()){
			first = matcher.group();
		}else{
			pattern = Pattern.compile(".*?<\\/blockquote>");
			matcher = pattern.matcher(rawThread);
			if(matcher.find()){
				first = matcher.group();
			}
		}
		
		pattern = Pattern.compile("<table><tr.*?/tr></table>");
		matcher = pattern.matcher(rawThread);
		
		List<String> list = new ArrayList<String>();
		list.add(first);
		
		while(matcher.find()){
			list.add(matcher.group());
		}
		
		return list.toArray(new String[list.size()]);
	}

}
