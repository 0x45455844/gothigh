package dykzei.eleeot.GotHigh.gui;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.Settings;
import dykzei.eleeot.GotHigh.network.IDownloadDone;
import dykzei.eleeot.GotHigh.network.IThreadReceiver;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import dykzei.eleeot.GotHigh.network.Provider;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class BoardActivity extends Activity implements IThreadReceiver{
	
	class BoardAdapter extends CursorAdapter {
		
		public BoardAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			BoardItem boardItem = (BoardItem)view;
			BoardHolder boardHolder;
			if(boardItem.getTag() == null){
				boardHolder = new BoardHolder(boardItem);
				boardItem.setTag(boardHolder);
			}else{
				boardHolder = (BoardHolder)boardItem.getTag();
			}
			boardHolder.bind(cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			BoardItem boardItem = (BoardItem)getLayoutInflater().inflate(R.layout.board_item, null);
			bindView(boardItem, context, cursor);
			return boardItem;
		}

	}
	
	class BoardHolder {
		TextView id;
		TextView text;
		ImageView image;
		
		public BoardHolder(BoardItem parent){
			id = (TextView)parent.findViewById(R.id.id);
			text = (TextView)parent.findViewById(R.id.text);
			image = (ImageView)parent.findViewById(R.id.image);
		}
		
		public void bind(Cursor c){
			id.setText(c.getString(DB.BOARD_COLUMN_INDEX_ID));
			text.setText(c.getString(DB.BOARD_COLUMN_INDEX_TEXT));
			if(!preloadImage(c))
				ImgDownloader.scheduleCacheDownload(c.getString(DB.BOARD_COLUMN_INDEX_IMAGE), 
					new IDownloadDone() {					
						@Override
						public void downloadDone(Bitmap bmp) {
							if(bmp != null){					
								listView.invalidateViews();
							}
						}
					}
				);	
		}
		
		private boolean preloadImage(Cursor c){
			String filename = Application.getCacheImgPath(ImgDownloader
					.filenameFromUrl(c.getString(DB.BOARD_COLUMN_INDEX_IMAGE)));
			Bitmap bmp = BitmapFactory.decodeFile(filename);
			
			if(bmp != null){
				image.setImageDrawable(new BitmapDrawable(bmp));	
				listView.invalidateViews();
				return true;
			}
			
			return false;
		}
	}
	
	private ListView listView;
	private ImageButton refreshButton;
	private ProgressDialog progressDialog;
	private TextView boardNameText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.board);
		
		listView = (ListView)findViewById(R.id.list);		
		listView.setAdapter(new BoardAdapter(Application.getContext(), DB.getBoard()));	
		
		refreshButton = (ImageButton)findViewById(R.id.refresh);
		refreshButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refresh();				
			}
		});
		
		boardNameText = (TextView)findViewById(R.id.boardName);
		
		refresh();
	}	

	@Override
	protected void onResume() {
		super.onResume();			
		boardNameText.setText(Application.getParser().getBoardName(Settings.board));
	}

	@Override
	public void onThreadsFetchComplete() {		
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				getAdapter().getCursor().requery();
				if(progressDialog != null){
					progressDialog.dismiss();
					progressDialog = null;
				}
			}
		});		
	}
	
	private BoardAdapter getAdapter(){
		return (BoardAdapter)listView.getAdapter();
	}
	
	private void refresh(){
		DB.clearBoard();
		Provider.getBoardThreads(Application.getParser().getBoardCode(Settings.board), this);
		progressDialog = ProgressDialog.show(this, null, getString(R.string.loading));
	}
	
}
