package dykzei.eleeot.GotHigh.gui;

import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.network.IThreadReceiver;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
	
	protected static ProgressDialog progressDialog;
	protected static MessageActivity self;
	protected ListView listView;
	protected ImageButton refreshButton;
	protected ImageButton leftButton;
	protected ImageButton rightButton;
	protected ImageButton poolButton;
	
	protected TextView boardNameText;
	protected TextView boardPageText;
	
	protected abstract CursorAdapter createAdapter();
	protected abstract void download();
	protected abstract MessageHolder getMessageHolder(MessageItem parent);
	
	protected class MessageAdapter extends CursorAdapter {
		
		public MessageAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			MessageItem messageItem = (MessageItem)view;
			MessageHolder messageHolder;
			if(messageItem.getTag() == null){
				messageHolder = getMessageHolder(messageItem);
				messageItem.setTag(messageHolder);
			}else{
				messageHolder = (MessageHolder)messageItem.getTag();
			}
			messageHolder.bind(cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			MessageItem messageItem = (MessageItem)getLayoutInflater().inflate(R.layout.message_item, null);
			bindView(messageItem, context, cursor);
			return messageItem;
		}

	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.board);
		self = this;
		listView = (ListView)findViewById(R.id.list);		
		
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
		boardPageText = (TextView)findViewById(R.id.boardPage);		
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
		self = null;
		getAdapter().getCursor().close();
		super.onDestroy();
	}

	@Override
	public void onComplete() {		
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				if(self != null){
					getAdapter().getCursor().requery();
					if(progressDialog != null){
						progressDialog.dismiss();
						progressDialog = null;
					}
				}
			}
		});		
	}
	
	protected CursorAdapter getAdapter(){
		return (CursorAdapter) listView.getAdapter();
	}
	
	protected void refresh(){
		progressDialog = ProgressDialog.show(this, null, getString(R.string.loading));
		download();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK 
				&& (event.getAction() & KeyEvent.ACTION_DOWN) == KeyEvent.ACTION_DOWN){
			if(progressDialog != null){
				progressDialog.dismiss();
				progressDialog = null;
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
}
