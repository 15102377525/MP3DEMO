package mp3.stk.com.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import mp3.stk.com.adapter.NetworkFragmentAdapter;
import mp3.stk.com.adapter.SearchFragmentAdapter;
import mp3.stk.com.model.LyricModel;
import mp3.stk.com.model.NetworkModel;
import mp3.stk.com.model.SearchModel;
import mp3.stk.com.mp3demo.R;
import mp3.stk.com.mp3demo.SongDetailsActivity;
import mp3.stk.com.utils.HelperUrl;
import okhttp3.Call;

public class SearchFragment extends Fragment implements View.OnClickListener {
    ListView listview;
    Button btnSearch;
    EditText etSearch;

    SearchFragmentAdapter adapter;
    SearchModel searchmodel;
    List<SearchModel.ShowapiResBodyBean.PagebeanBean.ContentlistBean> list;
    List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> music_list;
    //输入框的值
    String searchStr;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, null);
        listview = (ListView) view.findViewById(R.id.listview);
        btnSearch = (Button) view.findViewById(R.id.btnSearch);
        etSearch = (EditText) view.findViewById(R.id.etSearch);
        btnSearch.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                searchStr = etSearch.getText().toString();
                if (searchStr.equals("")) {
                    Toast.makeText(getActivity(), "请输入搜索内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                String url = HelperUrl.searchMusic + searchStr;
                String urls = null;
                try {
                    urls = URLEncoder.encode(url, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                OkHttpUtils
                        .get()
                        .url(HelperUrl.searchMusic + searchStr)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e("Error", e.getMessage());
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.e("TTT", response);
                                searchmodel = new Gson().fromJson(response, SearchModel.class);
                                list = searchmodel.getShowapi_res_body().getPagebean().getContentlist();
                                adapter = new SearchFragmentAdapter(getActivity(), list);
                                listview.setAdapter(adapter);
                                getMusiclist();
                            }
                        });
                break;
        }
    }

    public void getMusiclist() {
        music_list = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean songlistBean = new NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean();

            songlistBean.setAlbumid(list.get(i).getAlbumid());
            songlistBean.setDownUrl(list.get(i).getDownUrl());
            songlistBean.setSingerid(list.get(i).getSingerid());
            songlistBean.setSingername(list.get(i).getSingername());
            songlistBean.setSongid(list.get(i).getSongid());
            songlistBean.setSongname(list.get(i).getSongname());
            music_list.add(songlistBean);
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                                its.putExtra("list", (Serializable) music_list);
                                its.putExtra("isPlaying", 0);//是否可以播放  0 可以 1 不播放 2 播放中
                                its.putExtra("num", position);//播放歌曲的下标
                                its.putExtra("lyric", str);//歌曲的歌词
                                getActivity().startActivityForResult(its, 101);
                            }
                        });
            }
        });
    }
}
