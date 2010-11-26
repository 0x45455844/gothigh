package dykzei.eleeot.GotHigh;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper{
	
	private static final String DB_NAME = "gethigh4chan";
	private static DB self;

	public DB(Context context) {
		super(context, DB_NAME, null, 1);
		self = this;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE board (_id TEXT, moment INTEGER, text TEXT, image TEXT);");		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE board;");
		onCreate(db);
	}
	
	public static Cursor getBoard(){
		SQLiteDatabase db = getSelf().getReadableDatabase();
		Cursor c = db.rawQuery("SELECT _id, moment, text, image FROM board ORDER BY moment DESC", null);
		return c;
	}
	
	public static void suicide(){
		self = null;
	}
	
	private static DB getSelf(){
		if(self == null)
			self = new DB(Application.getContext());
		return self;
	}

}
