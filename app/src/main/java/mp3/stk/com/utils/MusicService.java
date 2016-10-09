package mp3.stk.com.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.util.Log;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import mp3.stk.com.model.LyricModel;
import mp3.stk.com.model.NetworkModel;
import okhttp3.Call;


public class MusicService extends Service implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {
    MyBinder myBinder;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    /**
     * 缓冲更新
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    //播放完成
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("TTT", "播放完成");
        Intent mIntent = new Intent("music_end");
        //发送广播
        sendBroadcast(mIntent);
    }

    // 播放准备
    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    int music_max = -1;

    public List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> lists;
    public int music_type = 0;
    public int num = 0;
    //歌词
    private String lyric;

    public void getList_list(List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> list) {
        this.lists = list;
    }

    public void getnum_num(int num) {
        this.num = num;
    }

    public class MyBinder extends Binder {

        private OnMusicTypeChang onMusicTypeChang;

        public void setOnMusicTypeChang(OnMusicTypeChang onMusicTypeChangs) {
            onMusicTypeChang = onMusicTypeChangs;
        }


        //获取歌词
        public String getLyric() {
            return lyric;
        }

        //得到歌词
        public void setLyric(String lyrics) {
            lyric = lyrics;
        }


        //音乐最大值
        public int getDuration() {
            return music_max;
        }

        //获取播放状态
        public void getMusic_type(int chang) {
            music_type = chang;
        }

        //获取所有歌曲
        public void getlist(List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> list) {
            getList_list(list);
        }

        //返回歌曲列表
        public List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> returnList() {
            return lists;
        }


        //获取当前播放的下标
        public void getnum(int nums) {
            getnum_num(nums);
        }


        //返回当前下标
        public int returnNum() {
            return num;
        }


        //下一曲
        public void MyMusicNext() {
            musicNext();
            if (onMusicTypeChang != null) {
                onMusicTypeChang.OnChangNext(num);//刷新num
            }
        }

        //上一曲
        public void MyMusicUp() {
            musicUp();
            onMusicTypeChang.OnChangUp(num);
        }

        //加载
        public void MyplayUrl(String url) {
            playUrl(url);
            start();
        }

        //改变播放的进度
        public void MyChangeProgress(int pro) {
            ChangeProgress(pro);
        }

        //开始播放
        public void MyStart() {
            start();
        }

        //暂停播放
        public void MyPause() {
            pause();
        }

        //停止播放
        public void MyStop() {
            stop();
        }

        //是否正在播放音乐
        public boolean MyIsPlaying() {
            return isPlaying();
        }
    }

    MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        myBinder = new MyBinder();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }


    /**
     * @param url url地址
     */
    public void playUrl(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url); // 设置数据源
            mediaPlayer.prepare(); // prepare自动播放
            music_max = mediaPlayer.getDuration();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //mediaPlayer播放进度
    public void ChangeProgress(int pro) {
        mediaPlayer.seekTo(pro);
    }


    //播放
    public void start() {
        //获取歌词
        OkHttpUtils
                .get()
                .url(HelperUrl.musicLyric + lists.get(num).getSongid())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("Error", e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e("TTT", response);
                        LyricModel lyricModel = new Gson().fromJson(response, LyricModel.class);
                        Log.e("ccc", lyricModel.getShowapi_res_body().getLyric());
                        //转码
                        lyric = Html.fromHtml(lyricModel.getShowapi_res_body().getLyric()).toString();
                        if (hand != null) {
                            hand.removeCallbacks(runn);
                        }
                        mediaPlayer.start();
                        Thread thread = new Thread(runn);
                        thread.start();//开启线程 发送广播
                    }
                });


    }

    // 暂停
    public void pause() {
        mediaPlayer.pause();
        hand.removeCallbacks(runn);
    }

    // 停止
    public void stop() {
        hand.removeCallbacks(runn);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    //音乐是否正在播放？
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }


    //下一曲
    public void musicNext() {
        switch (music_type) {
            case 0:
                if (num == lists.size()) {
                    num = 0;
                } else
                    num++;
                break;
            case 1:

                break;
            case 2:
                Random random = new Random();
                num = random.nextInt(lists.size());
                break;
        }
        playUrl(lists.get(num).getDownUrl());
    }


    //上一曲
    public void musicUp() {
        switch (music_type) {
            case 0:
                if (num == 0) {
                    num = lists.size() - 1;
                } else
                    num--;
                break;
            case 1:
                if (num == 0) {
                    num = lists.size() - 1;
                } else
                    num--;
                break;
            case 2:
                Random random = new Random();
                num = random.nextInt(lists.size());
                break;
        }
        playUrl(lists.get(num).getUrl());
    }


    public interface OnMusicTypeChang {
        public void OnChangNext(int sum);//播放下一曲

        public void OnChangUp(int sum);//播放上一曲
    }

    Runnable runn = new Runnable() {
        @Override
        public void run() {
            Message msg = hand.obtainMessage();
            msg.arg1 = mediaPlayer.getCurrentPosition();//音乐当前进度
            hand.sendMessage(msg);
        }
    };
    private Handler hand = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent mIntent = new Intent("music");
            mIntent.putExtra("position", msg.arg1);
            //发送广播
            sendBroadcast(mIntent);
            hand.postDelayed(runn, 1000);
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hand != null) {
            hand.removeCallbacks(runn);
        }
    }
}
