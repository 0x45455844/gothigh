package dykzei.eleeot.GotHigh.chan;

public class ChMessage {

	private String board;
	private String id;
	private String text;
	private String image;
	
	private ChMessage previous;
	private ChMessage next;
	
	public ChMessage(String id, String board){
		this.id = id;
		this.board = board;
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		if(id != null)
			this.id = id;
	}
	
	public String getBoard(){
		return board;
	}
	
	public void setBoard(String board){
		if(board != null)
			this.board = board;
	}
	
	public String getImage(){
		return image;
	}
	
	public void setImage(String image){
		if(image != null)
			this.image = image;
	}
	
	public String getText(){
		return text;
	}
	
	public void setText(String text){
		if(text != null)
			this.text = text;
	}
	
	public ChMessage getPrevious(){
		return previous;
	}
	
	public void setPrevios(ChMessage previous){
		this.previous = previous;
	}
	
	public ChMessage getNext(){
		return next;
	}
	
	public void setNext(ChMessage next){
		this.next = next;
	}
}
