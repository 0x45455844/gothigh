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
		
		return "b";
	}

	@Override
	public String getBoardName(int index) {
		if(index >= 0 && index < boardNames.length)
			return boardNames[index];
		
		return "Random";
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
		
		if(matcher.find()){
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
		
		return new String[]{};
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

}
