package mp3.stk.com.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;

import java.util.ArrayList;
import java.util.List;

import mp3.stk.com.model.NetworkModel;
import mp3.stk.com.mp3demo.R;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by admin on 2016/9/19.
 */
public class NetworkFragmentAdapter extends BaseAdapter {
    Context context;
    List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> list;

    public NetworkFragmentAdapter(Context context, List<NetworkModel.ShowapiResBodyBean.PagebeanBean.SonglistBean> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    helperNetwrokFragment helperNetwrokFragment = null;
    int sum;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        sum = position;
        if (convertView == null) {
            helperNetwrokFragment = new helperNetwrokFragment();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_networkadapter, null);
            helperNetwrokFragment.imgae_cover = (ImageView) convertView.findViewById(R.id.imgae_cover);
            helperNetwrokFragment.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            helperNetwrokFragment.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
            convertView.setTag(helperNetwrokFragment);
        } else {
            helperNetwrokFragment = (NetworkFragmentAdapter.helperNetwrokFragment) convertView.getTag();
        }
        helperNetwrokFragment.txt_title.setText(list.get(position).getSongname());
        helperNetwrokFragment.txt_name.setText(list.get(position).getSingername());
        if (!list.get(position).getAlbumpic_big().equals("") && list.get(position).getAlbumpic_big() != null) {
            Picasso.with(context).load(list.get(position).getAlbumpic_big()).into(helperNetwrokFragment.imgae_cover);
        } else {
            helperNetwrokFragment.imgae_cover.setImageResource(R.mipmap.ic_launcher);
        }

        return convertView;
    }


    public class helperNetwrokFragment {
        ImageView imgae_cover;
        TextView txt_title, txt_name;
    }
}
