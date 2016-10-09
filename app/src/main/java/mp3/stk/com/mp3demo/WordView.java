package mp3.stk.com.mp3demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mp3.stk.com.model.LrcHandle;

public class WordView extends TextView {


    private List<String> mWordsList = new ArrayList<String>();
    private List<Integer> mlistint;
    private Paint mLoseFocusPaint;
    private Paint mOnFocusePaint;
    private float mX = 0;
    private float mMiddleY = 0;
    private float mY = 0;
    private static final int DY = 50;
    /**
     * 播放的台词下标（舍去前四条，前四条没有时间目录）
     */
    private int mIndex = 5;

    public WordView(Context context) throws IOException {
        super(context);
        init();
    }

    public WordView(Context context, AttributeSet attrs) throws IOException {
        super(context, attrs);
        init();
    }

    public WordView(Context context, AttributeSet attrs, int defStyle)
            throws IOException {
        super(context, attrs, defStyle);
        init();
    }


    //改变当前显示歌词的下标

    public void setmIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#00000000"));
        Paint p = mLoseFocusPaint;
        p.setTextAlign(Paint.Align.CENTER);
        Paint p2 = mOnFocusePaint;
        p2.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mWordsList.get(mIndex), mX, mMiddleY, p2);
        int alphaValue = 25;
        float tempY = mMiddleY;
        for (int i = mIndex - 1; i >= 0; i--) {
            tempY -= DY;
            if (tempY < 0) {
                break;
            }
            p.setColor(Color.argb(255 - alphaValue, 245, 245, 245));
            canvas.drawText(mWordsList.get(i), mX, tempY, p);
            alphaValue += 25;
        }
        alphaValue = 25;
        tempY = mMiddleY;
        for (int i = mIndex + 1, len = mWordsList.size(); i < len; i++) {
            tempY += DY;
            if (tempY > mY) {
                break;
            }
            p.setColor(Color.argb(255 - alphaValue, 245, 245, 245));
            canvas.drawText(mWordsList.get(i), mX, tempY, p);
            alphaValue += 25;
        }
        mIndex++;
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);

        mX = w * 0.5f;
        mY = h;
        mMiddleY = h * 0.3f;
    }


    private String licy;

    public void setLicy(String licy) {
        this.licy = licy;
        LrcHandle lrcHandler = new LrcHandle();
        lrcHandler.readLRC(licy);
        mWordsList = lrcHandler.getWords();
        mlistint = lrcHandler.getTime();
    }

    @SuppressLint("SdCardPath")
    private void init() throws IOException {
        setFocusable(true);
        mLoseFocusPaint = new Paint();
        mLoseFocusPaint.setAntiAlias(true);
        mLoseFocusPaint.setTextSize(20);
        mLoseFocusPaint.setColor(Color.WHITE);
        mLoseFocusPaint.setTypeface(Typeface.SERIF);

        mOnFocusePaint = new Paint();
        mOnFocusePaint.setAntiAlias(true);
        mOnFocusePaint.setColor(Color.YELLOW);
        mOnFocusePaint.setTextSize(30);
        mOnFocusePaint.setTypeface(Typeface.SANS_SERIF);
    }


    /**
     * 按当前的歌曲的播放时间，从歌词里面获得那一句
     *
     * @param time 当前歌曲的播放时间
     * @return 返回当前歌词的索引值
     */
    public void SelectIndex(int time) {
        int index = 6;
        for (int i = 0; i < mlistint.size(); i++) {
            int temp = mlistint.get(i);
            if (temp < time) {
                ++index;
            }
        }
        mIndex = index - 1;
        if (mIndex < 6) {
            mIndex = 6;
        }
    }

}
