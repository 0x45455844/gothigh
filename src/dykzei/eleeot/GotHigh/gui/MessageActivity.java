package dykzei.eleeot.GotHigh.gui;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.network.IDownloadDone;
import dykzei.eleeot.GotHigh.network.IThreadReceiver;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public abstract class MessageActivity extends Activity implements IThreadReceiver{
	
	protected class MessageAdapter extends CursorAdapter {
		
		private int resource;
		
		public MessageAdapter(Context context, Cursor cursor, int resource) {
			super(context, cursor);
			this.resource = resource;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			MessageHolder messageHolder;
			if(view.getTag() == null){
				messageHolder = getMessageHolder(view);
				view.setTag(messageHolder);
			}else{
				messageHolder = (MessageHolder)view.getTag();
			}
			messageHolder.bind(cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = getLayoutInflater().inflate(resource, null);
			bindView(view, context, cursor);
			return view;
		}

	}
	
	protected static final int MENU_EXPAND = 889022;
	protected static final int MENU_IMAGE = 889023;
	
	protected static ProgressDialog progressDialog;
	protected ListView listView;
	protected ImageButton refreshButton;
	protected ImageButton leftButton;
	protected ImageButton rightButton;
	protected ImageButton poolButton;
	
	protected TextView boardNameText;
	
	protected abstract CursorAdapter createAdapter();
	protected abstract void download();
	protected abstract MessageHolder getMessageHolder(View view);
	protected abstract void setContent();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContent();
		listView = (ListView)findViewById(R.id.list);
		listView.setItemsCanFocus(false);
		refreshButton = (ImageButton)findViewById(R.id.refresh);
		refreshButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refresh();				
			}
		});
		
		leftButton = (ImageButton)findViewById(R.id.left);
		rightButton = (ImageButton)findViewById(R.id.right);
		poolButton = (ImageButton)findViewById(R.id.pool);
		boardNameText = (TextView)findViewById(R.id.boardName);	
	}	
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);		
		listView.setAdapter(createAdapter());	
		if(getAdapter().getCursor().getCount() == 0)
			refresh();
	}
	
	@Override
	protected void onDestroy(){
		flipProgress(false);
		getAdapter().getCursor().close();
		super.onDestroy();
	}

	@Override
	public void onComplete() {
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				getAdapter().getCursor().requery();
				flipProgress(false);
			}
		});		
	}
	
	protected CursorAdapter getAdapter(){
		return (CursorAdapter) listView.getAdapter();
	}
	
	protected void refresh(){
		flipProgress(true);
		download();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK 
				&& (event.getAction() & KeyEvent.ACTION_DOWN) == KeyEvent.ACTION_DOWN){
			flipProgress(false);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected void flipProgress(final boolean up){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(up){
					if(progressDialog == null){
						progressDialog = ProgressDialog.show(MessageActivity.this, null, getString(R.string.loading));
						// progressDialog.setCancelable(true);
					}
				}else{
					if(progressDialog != null){
						progressDialog.dismiss();
						progressDialog = null;
					}
				}				
			}
		});		
	}
	
	protected void downloadHirezAndShow(final String url){
		flipProgress(true);
		ImgDownloader.scheduleHirezDownload(url, new IDownloadDone() {
			
			@Override
			public void downloadDone(boolean success) {
				flipProgress(false);
				if(success){
					String filename = ImgDownloader.filenameFromUrl(url);
					String path = Application.getHirezImgPath(filename);
					Intent intent = new Intent(Application.getContext(), ImageActivity.class);
					intent.putExtra(ImageActivity.PARAM_FILENAME, path);
					startActivityForResult(intent, 0);
				}
			}
		});
	}
}
