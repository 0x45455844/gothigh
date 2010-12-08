package dykzei.eleeot.GotHigh.gui;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

public class ExpandActivity extends Activity {

	public static final String PARAM_ID = "filename";
	
	private String id;
	
	private class ExpandHolder extends MessageHolder{
		
		public ExpandHolder(MessageItem parent){
			super(parent);
		}
		
		@Override
		public void bind(final Cursor c){
			id.setText(c.getString(DB.POOL_COLUMN_INDEX_ID));
			date.setText(c.getString(DB.POOL_COLUMN_INDEX_DATE));

			ommit.setVisibility(View.GONE);
			
			String subject = c.getString(DB.POOL_COLUMN_INDEX_SUBJECT);
			this.subject.setVisibility(subject.equals("")? View.GONE : View.VISIBLE);
			this.subject.setText(subject);
			
			text.setText(c.getString(DB.POOL_COLUMN_INDEX_TEXT));
			
			final String remoteImageFile = c.getString(DB.POOL_COLUMN_INDEX_IMAGE);
			String localImageFile = Application.getCacheImgPath(ImgDownloader.filenameFromUrl(remoteImageFile));
			
			preloadImage(localImageFile, remoteImageFile, null);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.expand);
		
		Bundle params = getIntent().getExtras();
		if(params == null || !params.containsKey(PARAM_ID)){
			finish();
			return;
		}
		
		id = params.getString(PARAM_ID);
		
		load();
		
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void load(){
		MessageItem root = (MessageItem) findViewById(R.id.root);
		ExpandHolder holder = new ExpandHolder(root);
		
		Cursor cursor = DB.getSingleMessage(id);
		if(cursor.moveToFirst()){
			holder.bind(cursor);
		}else{
			cursor.close();
			finish();
			return;
		}
		
		cursor.close();
		return;
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
}
