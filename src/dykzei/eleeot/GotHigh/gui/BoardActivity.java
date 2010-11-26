package dykzei.eleeot.GotHigh.gui;

import dykzei.eleeot.GotHigh.Application;
import dykzei.eleeot.GotHigh.DB;
import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.Settings;
import dykzei.eleeot.GotHigh.network.IThreadReceiver;
import dykzei.eleeot.GotHigh.network.Provider;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}

	}
	
	class BoardHolder {
		TextView text;
		ImageView image;
		
		public void bind(Cursor c){
			text.setText(c.getString(2));
			//image.setText(c.getString(3));
		}
	}
	
	private ListView list;
	private BoardAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.board);
		
		list = (ListView)findViewById(R.id.list);		
		list.setAdapter(new BoardAdapter(Application.getContext(), DB.getBoard()));	
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		
		if(adapter == null || adapter.isEmpty())
			Provider.getBoardThreads(Settings.board, this);
	}



	@Override
	public void onThreadsFetchComplete() {
		
		runOnUiThread(new Runnable() {			
			@Override
			public void run() {
				((BoardAdapter)list.getAdapter()).getCursor().requery();				
			}
		});
		
		
		
	}
	
}
