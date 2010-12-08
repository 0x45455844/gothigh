package dykzei.eleeot.GotHigh.gui;

import java.io.File;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PoolActivity extends Activity {
	
	class MessageAdapter extends CursorAdapter {
		
		public MessageAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			PoolHolder poolHolder;
			if(view.getTag() == null){
				poolHolder = new PoolHolder(view);
				view.setTag(poolHolder);
			}else{
				poolHolder = (PoolHolder)view.getTag();
			}
			poolHolder.bind(cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.poolmessage, null);
			bindView(view, context, cursor);
			return view;
		}

	}
	
	class PoolHolder {
		
		String boardValue;
		String idValue;
		String imageValue;
		ImageView image;
		
		public PoolHolder(View view){
			image = (ImageView)view.findViewById(R.id.image);
		}
		
		public void bind(final Cursor c){
			idValue = c.getString(DB.POOL_COLUMN_INDEX_ID);
			boardValue = c.getString(DB.POOL_COLUMN_INDEX_BOARD);
			imageValue = c.getString(DB.POOL_COLUMN_INDEX_IMAGE);
			
			if(!imageValue.equals("")){
				String localImageFile = Application.getCacheImgPath(ImgDownloader.filenameFromUrl(imageValue));
				if(preloadImage(localImageFile)){
					image.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(PoolActivity.this, ThreadActivity.class);
							intent.putExtra(ThreadActivity.PARAM_BOARD, boardValue);
							intent.putExtra(ThreadActivity.PARAM_ID, idValue);
							startActivityForResult(intent, 0);
							
						}
					});
				}
			}
		}
		
		protected boolean preloadImage(String local){
			if((new File(local)).exists()){
				Bitmap bmp = BitmapFactory.decodeFile(local);
				if(bmp != null){
					image.setImageBitmap(bmp);
					return true;
				}
			}

			image.setOnClickListener(null);
			image.setImageBitmap(null);
			return false;
		}
	}
	
	protected GridView gridView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.pool);
		gridView = (GridView)findViewById(R.id.grid);
		gridView.setAdapter(new MessageAdapter(Application.getContext(), DB.getPool()));
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
