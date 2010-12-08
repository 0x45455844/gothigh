package dykzei.eleeot.GotHigh.network;

public interface IAIBParser {
	String getAibName();
	String getHostUrl();
	String getBoardUrl(String board);
	String[] getAibBoards();
	String[] getAibBoardNames();
	String getBoardCode(int index);
	String getBoardName(int index);
	int getAibMaxPages();
	
	String[] getBoardThreads(String raw);
	String getBoardThreadHeadMessage(String raw);
	String getId(String raw);
	String getText(String raw);
	String getImage(String raw);
	String getDate(String raw);
	int getOmmited(String raw, boolean withImage);
	int getChildCount(String raw, boolean images);
	String getSubject(String raw);
	String getMessageUrl(String board, String id);
	String[] getThreadMessages(String raw);
	String getFullImage(String raw);
	String getFullImageInfo(String raw);
}
