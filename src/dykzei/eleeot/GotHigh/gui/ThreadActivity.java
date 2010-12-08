package dykzei.eleeot.GotHigh.gui;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.Logger;
import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.network.IDownloadDone;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import dykzei.eleeot.GotHigh.network.Provider;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;

public class ThreadActivity extends MessageActivity {
	
	class ThreadHolder extends MessageHolder{
		
		String idValue;
		String imageValue;
		
		public ThreadHolder(View view){
			super(view);
		}
		
		@Override
		public void bind(final Cursor c){
			idValue = c.getString(DB.POOL_COLUMN_INDEX_ID);
			id.setText(idValue);
			date.setText(c.getString(DB.POOL_COLUMN_INDEX_DATE));

			ommit.setVisibility(View.GONE);
			
			String subject = c.getString(DB.POOL_COLUMN_INDEX_SUBJECT);
			this.subject.setVisibility(subject.equals("")? View.GONE : View.VISIBLE);
			this.subject.setText(subject);
			
			text.setText(prepareText(c.getString(DB.POOL_COLUMN_INDEX_TEXT)));
			
			imageValue = c.getString(DB.POOL_COLUMN_INDEX_IMAGE);
			String localImageFile = Application.getCacheImgPath(ImgDownloader.filenameFromUrl(imageValue));
			
			if(c.getPosition() < 1){
				body.setOrientation(LinearLayout.VERTICAL);
			}else{
				body.setOrientation(LinearLayout.HORIZONTAL);
			}
			
			preloadImage(localImageFile, imageValue, new IDownloadDone() {					
				@Override
				public void downloadDone(boolean success) {
					if(success){	
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
			
			root.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					expand();					
				}
			});
			
			image.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					download();					
				}
			});
			
			root.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					MenuItem mnuQuote = menu.add(1, MENU_EXPAND, 10, R.string.expand);
	    			mnuQuote.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							expand();						
							return true;
						}
					});
	    			
	    			if(imageValue != null && !imageValue.equals("")){
		    			MenuItem mnuImage = menu.add(1, MENU_IMAGE, 20, R.string.image);
		    			mnuImage.setOnMenuItemClickListener(new OnMenuItemClickListener() {
							
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								download();
								return true;
							}
						});
	    			}					
				}
			});
		}
		
		private void expand(){
			Intent intent = new Intent(ThreadActivity.this, ExpandActivity.class);
			intent.putExtra(ExpandActivity.PARAM_ID, idValue);
			startActivityForResult(intent, 0);	
		}
		
		private void download(){			
			Cursor cfii = DB.getFullImage(idValue, false);
			if(cfii.moveToFirst()){
				String imgUrl = cfii.getString(DB.COLUMN_INDEX_FULLIMAGE);
				downloadHirezAndShow(imgUrl);						
			}
			cfii.close();
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
		poolButton.setVisibility(View.GONE);
		
		Bundle params = getIntent().getExtras();
		if(params != null){
			id = params.getString(PARAM_ID);
			board = params.getString(PARAM_BOARD);
			
			boardNameText.setText(id);
		}
		
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && (event.getAction() & KeyEvent.ACTION_DOWN) == KeyEvent.ACTION_DOWN){
			finish();
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected CursorAdapter createAdapter() {
		return new MessageAdapter(Application.getContext(), DB.getThread(id), R.layout.message);
	}

	@Override
	protected void download() {
		DB.shrinkPool();
		Provider.getThreadMessages(board, id, this);		
	}

	@Override
	protected MessageHolder getMessageHolder(View view) {
		return new ThreadHolder(view);
	}
	
	@Override
	protected void setContent() {
		setContentView(R.layout.board);
	}
}
