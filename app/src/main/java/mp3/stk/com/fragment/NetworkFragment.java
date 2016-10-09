package mp3.stk.com.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mp3.stk.com.adapter.NetworkFragmentAdapter;
import mp3.stk.com.model.LyricModel;
import mp3.stk.com.model.NetworkModel;
import mp3.stk.com.mp3demo.R;
import mp3.stk.com.mp3demo.SongDetailsActivity;
import mp3.stk.com.utils.HelperUrl;
import mp3.stk.com.utils.MusicService;
import okhttp3.Call;

public class NetworkFragment extends Fragment implements View.OnClickListener {
    ListView listview;
    MusicService.MyBinder myBinder;
    NetworkFragmentAdapter adapter;
    NetworkModel networkModel;
    List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> list;
    ArrayList<String> list_str;
    RadioGroup radioGroup;
    HorizontalScrollView horizontal;
    int[] type = {3, 5, 6, 16, 17, 18, 19, 23, 26};
    int sum = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmet_network, null);
        listview = (ListView) view.findViewById(R.id.listview);
        radioGroup = (RadioGroup) view.findViewById(R.id.rgChannel);
        horizontal = (HorizontalScrollView) view.findViewById(R.id.hvChannel);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getList();
        getmusic();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Intent its = new Intent(getActivity(), SongDetailsActivity.class);
                //传递当前播放的列表
                OkHttpUtils
                        .get()
                        .url(HelperUrl.musicLyric + list.get(position).getSongid())
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
                                String str = Html.fromHtml(lyricModel.getShowapi_res_body().getLyric()).toString();
                                Log.e("ccc", str);
                                /////////////////////////////////////////////
                                its.putExtra("list", (Serializable) list);
                                its.putExtra("isPlaying", 0);//是否可以播放  0 可以 1 不播放 2 播放中
                                its.putExtra("num", position);//播放歌曲的下标
                                its.putExtra("lyric", str);//歌曲的歌词
                                getActivity().startActivityForResult(its, 101);
                            }
                        });
            }
        });

    }


    public void getList() {
        list_str = new ArrayList<>();
        list_str.add("欧美");
        list_str.add("内地");
        list_str.add("港台");
        list_str.add("韩国");
        list_str.add("日本");
        list_str.add("民谣");
        list_str.add("摇滚");
        list_str.add("销量");
        list_str.add("热歌");
        for (int i = 0; i < list_str.size(); i++) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setButtonDrawable(null);           // 设置按钮的样式
            radioButton.setPadding(20, 20, 20, 20);                 // 设置文字距离按钮四周的距离
            radioButton.setText(list_str.get(i));
            radioButton.setButtonDrawable(android.R.color.transparent);
            radioButton.setId(i);
            radioButton.setTextSize(20);
            if (i == 0) {
                radioButton.setTextColor(Color.parseColor("#8EE1E6"));
            } else {
                radioButton.setTextColor(Color.parseColor("#000000"));
            }
            radioGroup.addView(radioButton);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < group.getChildCount(); i++) {
                    RadioButton r = (RadioButton) group.getChildAt(i);        //根据索引值获取单选按钮
                    r.setTextColor(Color.parseColor("#000000"));
                }
                sum = checkedId;
                RadioButton tempButton = (RadioButton) getActivity().findViewById(checkedId); // 通过RadioGroup的findViewById方法，找到ID为checkedID的RadioButton
                tempButton.setTextColor(Color.parseColor("#8EE1E6"));
                getmusic();
            }
        });
    }

    public void getmusic() {
        OkHttpUtils
                .get()
                .url(HelperUrl.misicURl + type[sum])
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("Error", e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        networkModel = new Gson().fromJson(response, NetworkModel.class);
                        list = networkModel.getShowapi_res_body().getPagebean().getSonglist();
                        adapter = new NetworkFragmentAdapter(getActivity(), list);
                        listview.setAdapter(adapter);
                    }
                });
    }

    @Override
    public void onClick(View v) {
    }

}
