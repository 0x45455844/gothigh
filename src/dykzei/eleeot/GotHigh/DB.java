package dykzei.eleeot.GotHigh;

import dykzei.eleeot.GotHigh.chan.ChMessage;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper{
	
	public static final int BOARD_COLUMN_INDEX_ID = 2;
	public static final int BOARD_COLUMN_INDEX_TEXT = 3;
	public static final int BOARD_COLUMN_INDEX_IMAGE = 4;
	
	private static final String DB_NAME = "gethigh4chan";
	private static DB self;

	public DB(Context context) {
		super(context, DB_NAME, null, 1);
		self = this;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE board (_id INTEGER primary key autoincrement, moment INTEGER, id TEXT, text TEXT, image TEXT);");		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE board;");
		onCreate(db);
	}
	
	public static Cursor getBoard(){
		SQLiteDatabase db = getSelf().getReadableDatabase();
		Cursor c = db.rawQuery("SELECT _id, moment, id, text, image FROM board ORDER BY moment DESC", null);
		return c;
	}
	
	public static void suicide(){
		self.close();
		self = null;
	}
	
	private static DB getSelf(){
		if(self == null)
			self = new DB(Application.getContext());
		return self;
	}
	
	public static void clearBoard(){
		SQLiteDatabase db = getSelf().getWritableDatabase();			    
		db.delete("board", null, null);
	}
	
	public static void addBoardMessage(ChMessage message){
		SQLiteDatabase db = getSelf().getWritableDatabase();
		ContentValues values = new ContentValues();		
		values.put("moment", System.currentTimeMillis());
		values.put("id", message.getId());
		values.put("text", message.getText());
		values.put("image", message.getImage());	    	
		db.insert("board", null, values);
	}

}
