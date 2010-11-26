package dykzei.eleeot.GotHigh.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class BoardItem extends LinearLayout{
	private boolean focused; 
	private Paint p;
	private Rect clientRect = new Rect(0,0,0,0);
	
	public BoardItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		p = new Paint();
		p.setStyle(Style.FILL);
		p.setColor(0xff003000);
	}

	@Override
	public void onDraw(Canvas canvas){
		if(focused)
			canvas.drawRect(clientRect, p);
		super.onDraw(canvas);
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent event){
		int action = event.getAction();
		switch(action){
			case MotionEvent.ACTION_DOWN:
				focused = true;
				postInvalidate();
				break;
				
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				focused = false;
				postInvalidate();
				break;
		}

		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		clientRect = new Rect(0,0,getMeasuredWidth(),getMeasuredHeight());
	}
}
