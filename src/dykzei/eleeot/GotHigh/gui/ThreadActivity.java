package dykzei.eleeot.GotHigh.gui;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.Logger;
import dykzei.eleeot.GotHigh.network.IDownloadDone;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import dykzei.eleeot.GotHigh.network.Provider;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;

public class ThreadActivity extends MessageActivity {
	
	class ThreadHolder extends MessageHolder{
		
		public ThreadHolder(MessageItem parent){
			super(parent);
		}
		
		@Override
		public void bind(Cursor c){
			id.setText(c.getString(DB.POOL_COLUMN_INDEX_ID));
			date.setText(c.getString(DB.POOL_COLUMN_INDEX_DATE));

			ommit.setVisibility(View.GONE);
			
			String subject = c.getString(DB.POOL_COLUMN_INDEX_SUBJECT);
			this.subject.setVisibility(subject.equals("")? View.GONE : View.VISIBLE);
			this.subject.setText(subject);
			
			text.setText(prepareText(c.getString(DB.POOL_COLUMN_INDEX_TEXT)));
			
			final String remoteImageFile = c.getString(DB.POOL_COLUMN_INDEX_IMAGE);
			String localImageFile = Application.getCacheImgPath(ImgDownloader.filenameFromUrl(remoteImageFile));
			
			if(c.getPosition() < 1){
				body.setOrientation(LinearLayout.VERTICAL);
			}else{
				body.setOrientation(LinearLayout.HORIZONTAL);
			}
			
			preloadImage(localImageFile, remoteImageFile, new IDownloadDone() {					
				@Override
				public void downloadDone(Bitmap bmp) {
					if(bmp != null){	
						Logger.w(id.getText() + " downloaded.");
						runOnUiThread(new Runnable() {									
							@Override
							public void run() {
								listView.invalidateViews();
							}
						});	    								
					}
				}
			});
		}
	}
	
	public static final String PARAM_ID = "id";
	public static final String PARAM_BOARD = "board";
	
	private String id;
	private String board;
	
	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		
		leftButton.setVisibility(View.GONE);		
		rightButton.setVisibility(View.GONE);
		boardPageText.setVisibility(View.GONE);
		poolButton.setVisibility(View.GONE);
		
		Bundle params = getIntent().getExtras();
		if(params != null){
			id = params.getString(PARAM_ID);
			board = params.getString(PARAM_BOARD);
			
			boardNameText.setText(id);
		}
		
		//overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	protected CursorAdapter createAdapter() {
		return new MessageAdapter(Application.getContext(), DB.getThread(id));
	}

	@Override
	protected void download() {
		DB.shrinkPool();
		Provider.getThreadMessages(board, id, this);		
	}

	@Override
	protected MessageHolder getMessageHolder(MessageItem parent) {
		return new ThreadHolder(parent);
	}
}
