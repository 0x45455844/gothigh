package dykzei.eleeot.GotHigh;

public class Settings {

	public static int board = 1;
	public static int maxCache = 1024 * 1024;
	
	public static String getBoardCode(){
		return Application.getParser().getBoardCode(board);
	}
	
	public static String getBoardName(){
		return Application.getParser().getBoardName(board);
	}
}
