package mp3.stk.com.mp3demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.Serializable;
import java.util.List;

import mp3.stk.com.model.LrcHandle;
import mp3.stk.com.model.LyricModel;
import mp3.stk.com.model.NetworkModel;
import mp3.stk.com.utils.HelperUrl;
import mp3.stk.com.utils.MusicService;
import okhttp3.Call;

public class SongDetailsActivity extends Activity implements MusicService.OnMusicTypeChang, View.OnClickListener {
    MusicService.MyBinder myBinder;
    List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> list;
    //是否可以播放音乐
    int isPlaying = -1;
    int num = -1;//下标

    //背景
    LinearLayout linear;
    //图片
    ImageView image_chang, image_up, image_isstart, image_next;
    //标题
    TextView txt_title, txt_name;
    //播放的状态
    int image_tyoe = 0;
    SeekBar seekBar;
    MyReceiver mBroadcastReceiver;

    //返回
    ImageView image_finish;
    //音乐时间
    TextView txt_startTime, txt_endTime;
    Intent it;


    //歌词
    String lyric;
    private WordView mWordView;
    private List<Integer> mTimeList;


    private int INTERVAL = 45;//歌词每行的间隔

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        Intent intent = new Intent();
        intent.setClass(this, MusicService.class);
        //启动Service，然后绑定该Service，这样我们可以在同时销毁该Activity，看看歌曲是否还在播放
        startService(intent);
        bindService(intent, conn, this.BIND_AUTO_CREATE);
        //注册广播
        mBroadcastReceiver = new MyReceiver();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("music_end");
        myIntentFilter.addAction("music");
        //注册广播
        this.registerReceiver(mBroadcastReceiver, myIntentFilter);
        init();
        //获取
        it = getIntent();
        list = (List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean>) it.getSerializableExtra("list");
        isPlaying = it.getIntExtra("isPlaying", -1);
        num = it.getIntExtra("num", -1);
        lyric = it.getStringExtra("lyric");
        //解析歌词
        showMisuc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void init() {
        mWordView = (WordView) findViewById(R.id.mWordView);
        image_finish = (ImageView) findViewById(R.id.image_finish);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        linear = (LinearLayout) findViewById(R.id.linear);
        image_chang = (ImageView) findViewById(R.id.image_chang);
        image_up = (ImageView) findViewById(R.id.image_up);
        image_isstart = (ImageView) findViewById(R.id.image_isstart);
        image_next = (ImageView) findViewById(R.id.image_next);
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_name = (TextView) findViewById(R.id.txt_name);
        txt_startTime = (TextView) findViewById(R.id.txt_startTime);
        txt_endTime = (TextView) findViewById(R.id.txt_endTime);
        image_chang.setOnClickListener(this);
        image_up.setOnClickListener(this);
        image_isstart.setOnClickListener(this);
        image_next.setOnClickListener(this);
        image_finish.setOnClickListener(this);

        /**
         * seekbar监听
         * */
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    myBinder.MyChangeProgress(progress);//改变拖动的进度
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBars) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBars) {
            }
        });

    }

    //使用ServiceConnection来监听Service状态的变化
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //这里我们实例化audioService,通过binder来实现
            myBinder = (MusicService.MyBinder) binder;
            onChang();
            //传递播放的音乐集合
            myBinder.getlist(list);
            if (isPlaying == 0) {
                myBinder.getnum(num);//传递下标
                myBinder.MyplayUrl(list.get(num).getUrl());//开始播放
                txt_endTime.setText(getTimeFromInt(myBinder.getDuration()));
                seekBar.setMax(myBinder.getDuration());
                //改变背景图片
                setTitle();

            } else {//播放中
                myBinder.getnum(num);//传递下标
                txt_endTime.setText(getTimeFromInt(myBinder.getDuration()));
                seekBar.setMax(myBinder.getDuration());
                //改变背景图片
                setTitle();
            }
        }
    };


    //显示歌词
    public void showMisuc() {
        mWordView.setLicy(lyric);
        LrcHandle lrcHandler = new LrcHandle();
        lrcHandler.readLRC(lyric);
        mTimeList = lrcHandler.getTime();
        mWordView.setmIndex(6);
    }

    public void onChang() {
        myBinder.setOnMusicTypeChang(this);
    }

    /***
     * x下一曲回掉
     */

    @Override
    public void OnChangNext(int sum) {
        txt_endTime.setText(getTimeFromInt(myBinder.getDuration()));
        seekBar.setMax(myBinder.getDuration());
        num = sum;
        setTitle();
        /**
         * 歌词
         * */
        lyric = myBinder.getLyric();
        myBinder.MyStart();
        showMisuc();
    }

    /**
     * 上一曲
     */
    @Override
    public void OnChangUp(int sum) {
        txt_endTime.setText(getTimeFromInt(myBinder.getDuration()));
        seekBar.setMax(myBinder.getDuration());
        num = sum;
        setTitle();
        lyric = myBinder.getLyric();
        myBinder.MyStart();
        showMisuc();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_up://上一曲
                myBinder.MyMusicUp();
                break;
            case R.id.image_next://xia一曲
                myBinder.MyMusicNext();
                break;
            case R.id.image_isstart://播放--暂停
                if (myBinder.MyIsPlaying()) {
                    image_isstart.setImageResource(R.mipmap.img_appwidget91_voice_play_pressed);
                    myBinder.MyPause();
                } else {
                    image_isstart.setImageResource(R.mipmap.img_appwidget91_voice_pause_pressed);
                    myBinder.MyStart();
                }
                break;
            case R.id.image_chang://播放状态
                switch (image_tyoe) {
                    case 0:
                        image_tyoe = 1;
                        image_chang.setImageResource(R.mipmap.img_appwidget91_voice_playmode_repeat_current);
                        myBinder.getMusic_type(image_tyoe);
                        break;
                    case 1:
                        image_tyoe = 2;
                        image_chang.setImageResource(R.mipmap.img_appwidget91_voice_playmode_shuffle);
                        myBinder.getMusic_type(image_tyoe);
                        break;
                    case 2:
                        image_tyoe = 0;
                        image_chang.setImageResource(R.mipmap.img_appwidget91_voice_playmode_repeat_all);
                        myBinder.getMusic_type(image_tyoe);
                        break;
                }
                break;
            case R.id.image_finish://返回
                //当前播放的所有状态（列表，下标）
                it.putExtra("list", (Serializable) list);
                it.putExtra("num", num);//播放歌曲的下标
                //返回当前歌词
                it.putExtra("lyric", lyric);
                setResult(102, it);
                this.finish();
                break;
        }
    }


    //改变标题和背景图片
    public void setTitle() {
        Picasso.with(SongDetailsActivity.this).load(list.get(num).getAlbumpic_big()).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                Drawable drawable = new BitmapDrawable(bitmap);
                linear.setBackgroundDrawable(drawable);
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {

            }

            @Override
            public void onPrepareLoad(Drawable drawable) {

            }
        });
        txt_name.setText(list.get(num).getSingername());
        txt_title.setText(list.get(num).getSongname());
    }


    /**
     * 广播
     */
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //获取广播中的数据（即得到 "这是初始数据" 字符串）
            String data = intent.getAction();
            if (data.equals("music")) {//判断是谁发送的广播
                int getCurrentPosition = intent.getIntExtra("position", 0);
                txt_startTime.setText(getTimeFromInt(getCurrentPosition));
                seekBar.setProgress(getCurrentPosition);
//                Log.e("TTT", "歌曲时间当前播放时间------- " + getCurrentPosition);
                //修改歌词索引
                mWordView.SelectIndex(getCurrentPosition);
                mWordView.invalidate();
            } else if (data.equals("music_end")) {//播放完成
                myBinder.MyMusicNext();
            }
        }
    }


    public static String getTimeFromInt(int time) {
        if (time <= 0) {
            return "0:00";
        }
        int secondnd = (time / 1000) / 60;
        int million = (time / 1000) % 60;
        String f = String.valueOf(secondnd);
        String m = million >= 10 ? String.valueOf(million) : "0" + String.valueOf(million);
        return f + ":" + m;
    }


}
