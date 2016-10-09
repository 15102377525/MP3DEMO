package mp3.stk.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import mp3.stk.com.model.SearchModel;
import mp3.stk.com.mp3demo.R;

/**
 * Created by admin on 2016/9/27.
 */
public class SearchFragmentAdapter extends BaseAdapter {
    Context context;
    List<SearchModel.ShowapiResBodyBean.PagebeanBean.ContentlistBean> list;

    public SearchFragmentAdapter(Context context,
                                 List<SearchModel.ShowapiResBodyBean.PagebeanBean.ContentlistBean> list) {
        this.context = context;
        this.list = list;
    }


    @Override
    public int getCount() {
        return list.size() > 0 ? list.size() : 0;
//        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position) != null ? list.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        helperSearch helperSearch = null;
        if (helperSearch == null) {
            helperSearch = new helperSearch();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_networkadapter, null);
            helperSearch.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            helperSearch.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
            helperSearch.imgae_cover = (ImageView) convertView.findViewById(R.id.imgae_cover);
            convertView.setTag(helperSearch);
        } else {
            helperSearch = (SearchFragmentAdapter.helperSearch) convertView.getTag();
        }

        helperSearch.txt_title.setText(list.get(position).getSongname());
        helperSearch.txt_name.setText(list.get(position).getSingername());
        if (list.get(position).getAlbumpic_big() != null && !list.get(position).getAlbumpic_big().equals("")) {
            Picasso.with(context).load(list.get(position).getAlbumpic_big()).into(helperSearch.imgae_cover);
        } else {
            helperSearch.imgae_cover.setImageResource(R.mipmap.ic_launcher);
        }
        return convertView;
    }



    public class helperSearch {
        TextView txt_title, txt_name;
        ImageView imgae_cover;
    }
}
