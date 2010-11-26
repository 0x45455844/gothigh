package dykzei.eleeot.GotHigh.chan;

public class ChMessage {

	private String id = "";
	private String text = "";
	private String image = "";
	
	private ChMessage previous;
	private ChMessage next;
	
	public ChMessage(String id){
		this.id = id;
	}
	
	public String getId(){
		return id;
	}
	
	public ChMessage setId(String id){
		if(id != null)
			this.id = id;
		return this;
	}
	
	public String getImage(){
		return image;
	}
	
	public ChMessage setImage(String image){
		if(image != null)
			this.image = image;
		return this;
	}
	
	public String getText(){
		return text;
	}
	
	public ChMessage setText(String text){
		if(text != null)
			this.text = text;
		return this;
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
