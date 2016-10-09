package mp3.stk.com.mp3demo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mp3.stk.com.fragment.LocalFragment;
import mp3.stk.com.fragment.MyFragmentPagerAdapter;
import mp3.stk.com.fragment.NetworkFragment;
import mp3.stk.com.fragment.SearchFragment;
import mp3.stk.com.model.NetworkModel;
import mp3.stk.com.utils.MusicService;

public class MainActivity extends FragmentActivity implements View.OnClickListener, MusicService.OnMusicTypeChang {
    NoScrollViewPager viewpager;
    LinearLayout linear_network, linear_search, linear_local;
    TextView txt_local, txt_search, txt_network;
    ArrayList<Fragment> list_fragment;
    LocalFragment localFragment;
    NetworkFragment networkFragment;
    SearchFragment searchFragment;


    /**
     * 播放器控件
     */
    RelativeLayout relative;
    ImageView image_start, image_url, image_next;
    TextView txt_title, txt_name;
    SeekBar seekbar;
    //播放的下标
    int num = -1;
    MusicService.MyBinder myBinder;
    List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> list;

    //歌词
    public String lyric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initViewPager();
    }

    public void init() {
        viewpager = (NoScrollViewPager) findViewById(R.id.viewpager);
        linear_network = (LinearLayout) findViewById(R.id.linear_network);
        linear_search = (LinearLayout) findViewById(R.id.linear_search);
        linear_local = (LinearLayout) findViewById(R.id.linear_local);
        txt_local = (TextView) findViewById(R.id.txt_local);
        txt_search = (TextView) findViewById(R.id.txt_search);
        txt_network = (TextView) findViewById(R.id.txt_network);
        linear_network.setOnClickListener(this);
        linear_search.setOnClickListener(this);
        linear_local.setOnClickListener(this);
        ////////////////////////////
        relative = (RelativeLayout) findViewById(R.id.relative);
        image_start = (ImageView) findViewById(R.id.image_start);
        image_url = (ImageView) findViewById(R.id.image_url);
        image_next = (ImageView) findViewById(R.id.image_next);
        txt_title = (TextView) findViewById(R.id.txt_title);
        txt_name = (TextView) findViewById(R.id.txt_name);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        image_start.setOnClickListener(this);
        image_next.setOnClickListener(this);
        relative.setOnClickListener(this);
    }

    public void initViewPager() {
        list_fragment = new ArrayList<Fragment>();
        localFragment = new LocalFragment();
        networkFragment = new NetworkFragment();
        searchFragment = new SearchFragment();
        list_fragment.add(networkFragment);
        list_fragment.add(searchFragment);
        list_fragment.add(localFragment);

        viewpager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), list_fragment));
        viewpager_init(0);
        viewpager.setOnPageChangeListener(new MyOnPageChangeListener());
    }


    @Override
    protected void onStart() {
        super.onStart();
        final Intent intent = new Intent();
        intent.setClass(MainActivity.this, MusicService.class);
        //启动Service，然后绑定该Service，这样我们可以在同时销毁该Activity，看看歌曲是否还在播放
        startService(intent);
        bindService(intent, conn, BIND_AUTO_CREATE);
        //注册广播
        mBroadcastReceiver = new MyReceiver();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction("music");
        myIntentFilter.addAction("music_end");
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
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
        }
    };
    MyReceiver mBroadcastReceiver;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linear_network:
                txtinit();
                viewpager_init(0);
                break;
            case R.id.linear_search:
                txtinit();
                viewpager_init(1);
                break;
            case R.id.linear_local:
                txtinit();
                viewpager_init(2);
                break;
            case R.id.image_start://暂停--播放
                if (lyric == null || lyric.equals("")) {
                    return;
                }
                if (myBinder.MyIsPlaying()) {//是否正在播放音乐改变图片
                    image_start.setImageResource(R.mipmap.play_ctrl_play);
                    myBinder.MyPause();
                } else {
                    image_start.setImageResource(R.mipmap.play_ctrl_pause_prs);
                    myBinder.MyStart();
                }
                break;
            case R.id.image_next://下一曲
                if (lyric == null || lyric.equals("")) {
                    return;
                }
                myBinder.MyMusicNext();
                break;

            case R.id.relative:
                if (lyric == null || lyric.equals("")) {
                    return;
                }
                Intent its = new Intent(MainActivity.this, SongDetailsActivity.class);
                //传递当前播放的列表
                its.putExtra("list", (Serializable) list);
                its.putExtra("isPlaying", 2);//是否可以播放  0 可以 1 不播放 2 播放中
                its.putExtra("num", num);//播放歌曲的下标
                //歌词
                its.putExtra("lyric", lyric);//歌曲的歌词
                startActivityForResult(its, 101);
                break;
        }
    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            txtinit();
            viewpager_init(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

    public void onChang() {
        myBinder.setOnMusicTypeChang(this);
    }

    public void txtinit() {
        //#8EE1E6
        txt_network.setTextColor(Color.parseColor("#000000"));
        txt_local.setTextColor(Color.parseColor("#000000"));
        txt_search.setTextColor(Color.parseColor("#000000"));
    }

    public void viewpager_init(int sum) {
        switch (sum) {
            case 0:
                txt_network.setTextColor(Color.parseColor("#8EE1E6"));
                viewpager.setCurrentItem(0);
                break;
            case 1:
                txt_search.setTextColor(Color.parseColor("#8EE1E6"));
                viewpager.setCurrentItem(1);
                break;
            case 2:
                txt_local.setTextColor(Color.parseColor("#8EE1E6"));
                viewpager.setCurrentItem(2);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == 102) {
            list = (List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean>) data.getSerializableExtra("list");
            num = data.getIntExtra("num", 0);
            lyric = data.getStringExtra("lyric");
            if (myBinder != null) {
                myBinder.getlist(list);
                myBinder.getnum(num);
                setTitleImage(num);
                if (myBinder.MyIsPlaying()) {
                    image_start.setImageResource(R.mipmap.play_ctrl_pause_prs);
                } else {
                    image_start.setImageResource(R.mipmap.play_ctrl_play);
                }
                seekbar.setMax(myBinder.getDuration());
                onChang();
            }
        }
    }


    /**
     * 更改播放音乐文字和图标
     */

    public void setTitleImage(int position) {
        txt_title.setText(list.get(position).getSongname());
        txt_name.setText(list.get(position).getSingername());
        Picasso.with(MainActivity.this).load(list.get(position).getAlbumpic_big()).into(image_url);
    }


    /***
     * 播放下一曲
     */

    @Override
    public void OnChangNext(int sum) {
        num = sum;
        setTitleImage(sum);
        myBinder.MyStart();
    }

    /**
     * 播放上一曲
     */
    @Override
    public void OnChangUp(int sum) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);
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
                //获取音乐下标和正在播放的音乐列表
                if (num != myBinder.returnNum()) {
                    num = myBinder.returnNum();
                    list = myBinder.returnList();
                    setTitleImage(num);
                    //获取歌词
                    lyric = myBinder.getLyric();
                }
                int getCurrentPosition = intent.getIntExtra("position", 0);
                seekbar.setProgress(getCurrentPosition);
                Log.e("TTT", data + "-------" + getCurrentPosition);
            } else if (data.equals("music_end")) {//播放完成
                myBinder.MyMusicNext();
            }
        }
    }
}
