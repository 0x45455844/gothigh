package dykzei.eleeot.GotHigh.gui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.Logger;
import dykzei.eleeot.GotHigh.Settings;
import dykzei.eleeot.GotHigh.network.IDownloadDone;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import dykzei.eleeot.GotHigh.network.Provider;

public class BoardActivity extends MessageActivity {

	class BoardHolder extends MessageHolder{
		
		public BoardHolder(MessageItem parent){
			super(parent);
		}
		
		@Override
		public void bind(Cursor c){
			id.setText(c.getString(DB.BOARD_COLUMN_INDEX_ID));
			date.setText(c.getString(DB.BOARD_COLUMN_INDEX_DATE));
			
			int om = c.getInt(DB.BOARD_COLUMN_INDEX_OMMIT);
			int omi = c.getInt(DB.BOARD_COLUMN_INDEX_OMMIT_IMG);
			ommit.setVisibility(om == 0 ? View.GONE : View.VISIBLE);
			if(om != 0){
				ommit.setText(om + " / " + omi);
			}
			
			String subject = c.getString(DB.BOARD_COLUMN_INDEX_SUBJECT);
			this.subject.setVisibility(subject.equals("")? View.GONE : View.VISIBLE);
			this.subject.setText(subject);
			
			final String remoteImageFile = c.getString(DB.BOARD_COLUMN_INDEX_IMAGE);
			String localImageFile = Application.getCacheImgPath(ImgDownloader.filenameFromUrl(remoteImageFile));
			
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
						
			text.setText(prepareText(c.getString(DB.BOARD_COLUMN_INDEX_TEXT)));
			
			root.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(BoardActivity.this, ThreadActivity.class);
					intent.putExtra(ThreadActivity.PARAM_BOARD, Settings.getBoardCode());
					intent.putExtra(ThreadActivity.PARAM_ID, id.getText());
					startActivityForResult(intent, 0);
				}
			});
		}
	}
	
	protected int page;
	
	@Override
	public void onCreate(Bundle state){
		super.onCreate(state);
		
		leftButton.setVisibility(View.GONE);
		leftButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				navigatePage(false);
			}
		});
		rightButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				navigatePage(true);
			}
		});
		
		ImgDownloader.shrinkCache();
	}
	
	@Override
	public CursorAdapter createAdapter(){
		return new MessageAdapter(Application.getContext(), DB.getBoard());
	}

	@Override
	protected void download() {
		boardPageText.setText("" + page);
		boardNameText.setText(Settings.getBoardCode());
		
		DB.clearBoard();
		
		String board = Settings.getBoardCode();
		if(page > 0){
			board += "/" + page;
		}
		Provider.getBoardThreads(board, this);		
	}
	
	private void navigatePage(boolean further){
		page += (further ? 1 : -1);
		
		leftButton.setVisibility(page == 0 ? View.GONE : View.VISIBLE);
		rightButton.setVisibility(page >= Application.getParser().getAibMaxPages() ? View.GONE : View.VISIBLE);
		
		refresh();
	}

	@Override
	protected MessageHolder getMessageHolder(MessageItem parent) {
		return new BoardHolder(parent);
	}
	
}
