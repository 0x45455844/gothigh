package dykzei.eleeot.GotHigh.gui;

import java.io.File;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.network.IDownloadDone;
import dykzei.eleeot.GotHigh.network.ImgDownloader;

public abstract class MessageHolder {
	protected TextView id;
	protected ImageView image;
	protected TextView text;
	protected TextView date;
	protected TextView ommit;
	protected TextView subject;
	
	protected RelativeLayout head;
	protected LinearLayout body;
	protected MessageItem root;
	
	public MessageHolder(MessageItem parent){
		id = (TextView)parent.findViewById(R.id.id);
		text = (TextView)parent.findViewById(R.id.text);
		image = (ImageView)parent.findViewById(R.id.image);
		date = (TextView)parent.findViewById(R.id.date);
		ommit = (TextView)parent.findViewById(R.id.ommit);
		subject = (TextView)parent.findViewById(R.id.subject);		
		head = (RelativeLayout)parent.findViewById(R.id.head);
		body = (LinearLayout)parent.findViewById(R.id.body);
		root = (MessageItem)parent.findViewById(R.id.root);	
	}
	
	public abstract void bind(Cursor c);
	
	protected String prepareText(String src){
		if(src.length() > 256)
			return src.substring(0, 256) + "...";
		return src;
	}
	
	protected void preloadImage(String local, String remote, IDownloadDone idd){
		if(remote.equals("")){
			image.setImageBitmap(null);
			return;
		}
		
		if(!(new File(local)).exists()){
			image.setImageBitmap(null);
			ImgDownloader.scheduleCacheDownload(remote, idd);
		}else{
			Bitmap bmp = BitmapFactory.decodeFile(local);
			if(bmp != null)
				image.setImageBitmap(bmp);
		}
	}
}
