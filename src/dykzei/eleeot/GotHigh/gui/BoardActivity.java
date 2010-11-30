package dykzei.eleeot.GotHigh.gui;

import java.io.File;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.Logger;
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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
		ImageView image;
		TextView text;
		TextView date;
		TextView ommit;
		TextView subject;
		
		View headb, headc;
		
		public BoardHolder(BoardItem parent){
			id = (TextView)parent.findViewById(R.id.id);
			text = (TextView)parent.findViewById(R.id.text);
			image = (ImageView)parent.findViewById(R.id.image);
			date = (TextView)parent.findViewById(R.id.date);
			ommit = (TextView)parent.findViewById(R.id.ommit);
			subject = (TextView)parent.findViewById(R.id.subject);
			
			headb = parent.findViewById(R.id.headb);
			headc = parent.findViewById(R.id.headc);
		}
		
		public void bind(Cursor c){
			id.setText(c.getString(DB.BOARD_COLUMN_INDEX_ID));
			date.setText(c.getString(DB.BOARD_COLUMN_INDEX_DATE));
			
			int om = c.getInt(DB.BOARD_COLUMN_INDEX_OMMIT);
			int omi = c.getInt(DB.BOARD_COLUMN_INDEX_OMMIT_IMG);
			headb.setVisibility(om == 0 ? View.GONE : View.VISIBLE);
			if(om != 0){
				ommit.setText(om + " / " + omi);
			}
			
			String subject = c.getString(DB.BOARD_COLUMN_INDEX_SUBJECT);
			headc.setVisibility(subject.equals("")? View.GONE : View.VISIBLE);
			this.subject.setText(subject);
			
			final String remoteImageFile = c.getString(DB.BOARD_COLUMN_INDEX_IMAGE);
			String localImageFile = Application.getCacheImgPath(ImgDownloader.filenameFromUrl(remoteImageFile));
			
			if(!(new File(localImageFile)).exists()){
				image.setImageBitmap(null);
				ImgDownloader.scheduleCacheDownload(remoteImageFile, new IDownloadDone() {					
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
			}else{
				Bitmap bmp = BitmapFactory.decodeFile(localImageFile);
				if(bmp != null)
					image.setImageBitmap(bmp);
			}
						
			text.setText(prepareText(c.getString(DB.BOARD_COLUMN_INDEX_TEXT)));
		}
		
		private String prepareText(String src){
			if(src.length() > 256)
				return src.substring(0, 256) + "...";
			return src;
		}
	}
	
	private int page;
	private static ProgressDialog progressDialog;
	private static BoardActivity self;
	private ListView listView;
	private ImageButton refreshButton;
	private ImageButton leftButton;
	private ImageButton rightButton;
	
	private TextView boardNameText;
	private TextView boardPageText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.board);
		self = this;
		listView = (ListView)findViewById(R.id.list);		
		listView.setAdapter(new BoardAdapter(Application.getContext(), DB.getBoard()));	
		
		refreshButton = (ImageButton)findViewById(R.id.refresh);
		refreshButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refresh();				
			}
		});
		
		leftButton = (ImageButton)findViewById(R.id.left);
		leftButton.setVisibility(View.GONE);
		leftButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				navigatePage(false);
			}
		});
		rightButton = (ImageButton)findViewById(R.id.right);
		rightButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				navigatePage(true);
			}
		});
		
		boardNameText = (TextView)findViewById(R.id.boardName);
		boardPageText = (TextView)findViewById(R.id.boardPage);
		
		ImgDownloader.shrinkCache();
		refresh();
	}	
	
	@Override
	protected void onDestroy(){
		self = null;
		getAdapter().getCursor().close();
		super.onDestroy();
	}

	@Override
	public void onThreadsFetchComplete() {		
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
	
	private BoardAdapter getAdapter(){
		return (BoardAdapter)listView.getAdapter();
	}
	
	private void refresh(){
		boardPageText.setText("" + page);
		boardNameText.setText(Application.getParser().getBoardCode(Settings.board));
		
		DB.clearBoard();
		
		String board = Application.getParser().getBoardCode(Settings.board);
		if(page > 0){
			board += "/" + page;
		}
		Provider.getBoardThreads(board, this);
		progressDialog = ProgressDialog.show(this, null, getString(R.string.loading));
	}
	
	private void navigatePage(boolean further){
		page += (further ? 1 : -1);
		
		leftButton.setVisibility(page == 0 ? View.GONE : View.VISIBLE);
		rightButton.setVisibility(page >= Application.getParser().getAibMaxPages() ? View.GONE : View.VISIBLE);
		
		refresh();
	}
}
