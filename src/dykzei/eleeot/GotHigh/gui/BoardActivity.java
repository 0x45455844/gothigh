package dykzei.eleeot.GotHigh.gui;

import java.lang.ref.WeakReference;

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
import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.Logger;
import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.Settings;
import dykzei.eleeot.GotHigh.network.IDownloadDone;
import dykzei.eleeot.GotHigh.network.IThreadReceiver;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import dykzei.eleeot.GotHigh.network.Provider;

public class BoardActivity extends MessageActivity {

	class BoardHolder extends MessageHolder{
		
		String idValue;
		String imageValue;
		
		public BoardHolder(View view){
			super(view);
		}
		
		@Override
		public void bind(final Cursor c){
			idValue = c.getString(DB.BOARD_COLUMN_INDEX_ID);
			imageValue = c.getString(DB.BOARD_COLUMN_INDEX_IMAGE);
			id.setText(idValue);
			date.setText(c.getString(DB.BOARD_COLUMN_INDEX_DATE));
			text.setText(prepareText(c.getString(DB.BOARD_COLUMN_INDEX_TEXT)));
			int om = c.getInt(DB.BOARD_COLUMN_INDEX_OMMIT);
			int omi = c.getInt(DB.BOARD_COLUMN_INDEX_OMMIT_IMG);
			ommit.setVisibility(om == 0 ? View.GONE : View.VISIBLE);
			if(om != 0){
				ommit.setText(om + " / " + omi);
			}
			String subject = c.getString(DB.BOARD_COLUMN_INDEX_SUBJECT);
			this.subject.setVisibility(subject.equals("")? View.GONE : View.VISIBLE);
			this.subject.setText(subject);

			String localImageFile = Application.getCacheImgPath(ImgDownloader.filenameFromUrl(imageValue));
			
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
					Intent intent = new Intent(BoardActivity.this, ThreadActivity.class);
					intent.putExtra(ThreadActivity.PARAM_BOARD, Settings.getBoardCode());
					intent.putExtra(ThreadActivity.PARAM_ID, idValue);
					startActivityForResult(intent, 0);
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
						    			
	    			MenuItem mnuImage = menu.add(1, MENU_IMAGE, 20, R.string.image);
	    			mnuImage.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							download();							
							return true;
						}
					});
					
				}
			});
		}
		
		private void download(){			
			Cursor cfii = DB.getFullImage(idValue, true);
			if(cfii.moveToFirst()){
				String imgUrl = cfii.getString(DB.COLUMN_INDEX_FULLIMAGE);
				downloadHirezAndShow(imgUrl);						
			}
			cfii.close();
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
		poolButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(BoardActivity.this, PoolActivity.class));				
			}
		});
		
		ImgDownloader.shrinkCache();
	}
	
	@Override
	public CursorAdapter createAdapter(){
		return new MessageAdapter(Application.getContext(), DB.getBoard(), R.layout.message);
	}

	@Override
	protected void download() {
		DB.clearBoard();
		
		String board = Settings.getBoardCode();
		if(page > 0){
			board += "/" + page;
		}
		boardNameText.setText(board);
		Provider.getBoardThreads(board, new WeakReference<IThreadReceiver>(this));		
	}
	
	private void navigatePage(boolean further){
		page += (further ? 1 : -1);
		
		leftButton.setVisibility(page == 0 ? View.GONE : View.VISIBLE);
		rightButton.setVisibility(page >= Application.getParser().getAibMaxPages() ? View.GONE : View.VISIBLE);
		
		refresh();
	}

	@Override
	protected MessageHolder getMessageHolder(View view) {
		return new BoardHolder(view);
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
	protected void setContent() {
		setContentView(R.layout.board);
	}
	
}
