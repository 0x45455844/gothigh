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
	public static final int BOARD_COLUMN_INDEX_DATE = 4;
	public static final int BOARD_COLUMN_INDEX_IMAGE = 5;
	public static final int BOARD_COLUMN_INDEX_SUBJECT = 6;
	public static final int BOARD_COLUMN_INDEX_OMMIT = 7;
	public static final int BOARD_COLUMN_INDEX_OMMIT_IMG = 8;
	public static final int BOARD_COLUMN_INDEX_FULLIMAGEINFO = 9;
	
	public static final int POOL_COLUMN_INDEX_ID = 2;
	public static final int POOL_COLUMN_INDEX_PARENTID = 3;
	public static final int POOL_COLUMN_INDEX_TEXT = 4;
	public static final int POOL_COLUMN_INDEX_DATE = 5;
	public static final int POOL_COLUMN_INDEX_IMAGE = 6;
	public static final int POOL_COLUMN_INDEX_SUBJECT = 7;
	public static final int POOL_COLUMN_INDEX_BOARD = 8;
	public static final int POOL_COLUMN_INDEX_FULLIMAGEINFO = 9;
	
	public static final int COLUMN_INDEX_FULLIMAGE = 3;
	
	
	private static final String DB_NAME = "gethigh4chan";
	private static DB self;

	public DB(Context context) {
		super(context, DB_NAME, null, 1);
		self = this;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE board (");
		sb.append("_id INTEGER primary key autoincrement,");
		sb.append(" moment INTEGER,");
		sb.append(" id TEXT,");
		sb.append(" text TEXT,");
		sb.append(" image TEXT,");
		sb.append(" fullimage TEXT,");
		sb.append(" fullimageinfo TEXT,");
		sb.append(" date TEXT,");
		sb.append(" subject TEXT,");
		sb.append(" ommit INTEGER,");
		sb.append(" ommiti INTEGER");
		sb.append(");");
		db.execSQL(sb.toString());
		db.execSQL("create index b_moment_idx on board (moment);");
		
		sb = new StringBuffer();
		sb.append("CREATE TABLE pool (");
		sb.append("_id INTEGER primary key autoincrement,");
		sb.append(" moment INTEGER,");
		sb.append(" id TEXT,");
		sb.append(" parentid TEXT,");
		sb.append(" text TEXT,");
		sb.append(" image TEXT,");
		sb.append(" fullimage TEXT,");
		sb.append(" fullimageinfo TEXT,");		
		sb.append(" date TEXT,");
		sb.append(" subject TEXT,");
		sb.append(" board TEXT");
		sb.append(");");
		db.execSQL(sb.toString());	
		db.execSQL("create index p_moment_idx on pool (moment);");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE board;");
		db.execSQL("DROP TABLE pool;");
		onCreate(db);
	}
	
	public static Cursor getBoard(){
		SQLiteDatabase db = getSelf().getReadableDatabase();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT _id,");
		sb.append(" moment,");
		sb.append(" id,");
		sb.append(" text,");
		sb.append(" date,");
		sb.append(" image,");
		sb.append(" subject,");
		sb.append(" ommit,");
		sb.append(" ommiti,");
		sb.append(" fullimageinfo");
		sb.append(" FROM board ORDER BY moment ASC");
		Cursor c = db.rawQuery(sb.toString(), null);
		return c;
	}
	
	public static Cursor getFullImage(String id, boolean board){
		SQLiteDatabase db = getSelf().getReadableDatabase();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT _id, moment, id, fullimage FROM ");
		sb.append(board ? "board" : "pool");
		sb.append(" WHERE id=\"");
		sb.append(id);
		sb.append("\"");
		Cursor c = db.rawQuery(sb.toString(), null);
		return c;
	}
	
	public static Cursor getThread(String id){
		SQLiteDatabase db = getSelf().getReadableDatabase();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT _id,");
		sb.append(" moment,");
		sb.append(" id,");
		sb.append(" parentid,");
		sb.append(" text,");
		sb.append(" date,");
		sb.append(" image,");		
		sb.append(" subject,");
		sb.append(" board,");
		sb.append(" fullimageinfo");
		sb.append(" FROM pool");
		sb.append(" WHERE parentid=\"");
		sb.append(id);		
		sb.append("\" ORDER BY moment ASC");
		Cursor c = db.rawQuery(sb.toString(), null);
		return c;
	}
	
	public static Cursor getSingleMessage(String id){
		SQLiteDatabase db = getSelf().getReadableDatabase();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT _id,");
		sb.append(" moment,");
		sb.append(" id,");
		sb.append(" parentid,");
		sb.append(" text,");
		sb.append(" date,");
		sb.append(" image,");		
		sb.append(" subject,");
		sb.append(" board,");
		sb.append(" fullimageinfo");
		sb.append(" FROM pool");
		sb.append(" WHERE id=\"");
		sb.append(id);		
		sb.append("\" ORDER BY moment ASC");
		Cursor c = db.rawQuery(sb.toString(), null);
		return c;
	}
	
	public static Cursor getPool(){
		SQLiteDatabase db = getSelf().getReadableDatabase();
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT _id,");
		sb.append(" moment,");
		sb.append(" id,");
		sb.append(" parentid,");
		sb.append(" text,");
		sb.append(" date,");
		sb.append(" image,");
		sb.append(" subject,");
		sb.append(" board");
		sb.append(" fullimageinfo");
		sb.append(" FROM pool WHERE parentid=id ORDER BY moment DESC");
		Cursor c = db.rawQuery(sb.toString(), null);
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
		db.execSQL("DELETE FROM board");
	}
	
	public static void shrinkPool(){
		SQLiteDatabase db = getSelf().getWritableDatabase();	
		// TODO: refer to setting
		db.execSQL("DELETE FROM pool WHERE moment < " + (System.currentTimeMillis() - 86400000));
	}
	
	public static void addBoardMessage(ChMessage message){
		SQLiteDatabase db = getSelf().getWritableDatabase();
		ContentValues values = new ContentValues();		
		values.put("moment", System.currentTimeMillis());
		values.put("id", message.id);
		values.put("text", message.text);
		values.put("date", message.date);
		values.put("image", message.image);
		values.put("subject", message.subject);
		values.put("ommit", message.ommit);
		values.put("ommiti", message.ommitImg);	
		values.put("fullimage", message.fullImage);	
		values.put("fullimageinfo", message.fullImageInfo);	
		db.insert("board", null, values);
	}
	
	public static void addPoolMessage(ChMessage message){
		SQLiteDatabase db = getSelf().getWritableDatabase();
		
		Cursor c = db.rawQuery("SELECT id FROM pool WHERE id=" + message.id, null);
		int count = c.getCount();
		c.close();
		if(count > 0)
			return;
		
		ContentValues values = new ContentValues();		
		values.put("moment", System.currentTimeMillis());
		values.put("id", message.id);
		values.put("parentid", message.parentId);
		values.put("text", message.text);
		values.put("date", message.date);
		values.put("image", message.image);
		values.put("subject", message.subject);		
		values.put("fullimage", message.fullImage);	
		values.put("fullimageinfo", message.fullImageInfo);			
		values.put("board", message.board);
		db.insert("pool", null, values);
	}

}
