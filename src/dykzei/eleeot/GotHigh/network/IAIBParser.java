package dykzei.eleeot.GotHigh.network;

public interface IAIBParser {
	String getAibName();
	String getHostUrl();
	String getBoardUrl(String board);
	String[] getAibBoards();
	String[] getAibBoardNames();
	int getAibMaxPages();
	String[] getBoardThreads(String raw);
}
