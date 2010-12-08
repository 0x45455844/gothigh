package dykzei.eleeot.GotHigh.gui;

import java.io.File;

import dykzei.eleeot.GotHigh.R;
import dykzei.eleeot.GotHigh.network.IDownloadDone;
import dykzei.eleeot.GotHigh.network.ImgDownloader;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.graphics.Matrix;

public class ImageActivity extends Activity {

	public static final String PARAM_FILENAME = "filename";
	
	private ImageView image;
	private String filename;
	private GestureDetector gestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.image);
		
		gestureDetector = new GestureDetector(new OnGestureListener() {
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}
			
			@Override
			public void onShowPress(MotionEvent e) {
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
					float distanceY) {
				return false;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
				
			}
			
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				return false;
			}
			
			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}
		});
		
		image = (ImageView)findViewById(R.id.image);
		image.setOnTouchListener(new OnTouchListener() {
			
			private float[] coord = new float[2];
			private boolean tracking;
			private Matrix matrix = new Matrix();
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					coord[0] = event.getX(0);
					coord[1] = event.getY(0);
					image.setScaleType(ScaleType.MATRIX);
					return true;
				}
				if(event.getAction() == MotionEvent.ACTION_POINTER_1_DOWN){
					coord[0] = Math.abs(event.getX(1) - coord[0]);
					coord[1] = Math.abs(event.getY(1) - coord[1]);
					tracking = true;
					return true;
				}
				if(event.getAction() == MotionEvent.ACTION_POINTER_1_UP){
					tracking = false;
					return true;
				}
				if(event.getAction() == MotionEvent.ACTION_MOVE){
					if(tracking){
						float sx = (float)Math.abs(event.getX(0) - event.getX(1)) / (float)coord[0];
						float sy = (float)Math.abs(event.getY(0) - event.getY(1)) / (float)coord[1];
						matrix.reset();
						matrix.setScale(sx, sy);
						image.setImageMatrix(matrix);
						return true;	
					}					
				}
				return false;
			}
		});
		
		Bundle params = getIntent().getExtras();
		if(params == null || !params.containsKey(PARAM_FILENAME)){
			finish();
		}else{
			//TODO: do leveled load
			filename = params.getString(PARAM_FILENAME);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inTempStorage = new byte[16*1024];
			Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
			image.setImageBitmap(bitmap);
		}
		
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
	
	protected void preloadHirezImage(String local, String remote, IDownloadDone idd){
		if(!(new File(local)).exists() || remote.equals("")){
			image.setOnClickListener(null);
			image.setImageBitmap(null);
			if(idd != null)
				ImgDownloader.scheduleHirezDownload(remote, idd);
		}else{
			Bitmap bmp = BitmapFactory.decodeFile(local);
			if(bmp != null)
				image.setImageBitmap(bmp);
		}
	}
	
}
