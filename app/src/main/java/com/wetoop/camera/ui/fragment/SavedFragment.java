package com.wetoop.camera.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wetoop.camera.listview.MyListView;
import com.wetoop.camera.ui.vlc.VlcPlayerSaveActivity;
import com.wetoop.cameras.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/18.
 */
public class SavedFragment extends Fragment implements MyListView.IXListViewListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener{
    private MyListView listView;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView;

        rootView = inflater.inflate(R.layout.fragment_saved, container, false);
        listView = (MyListView)rootView.findViewById(R.id.saveCameraList);
        listView.setXListViewListener(this);//设置刷新加载监听
        listView.setOnScrollListener(this);//设置滑动隐藏头部选择栏
        listView.setOnItemClickListener(this);//设置item点击监听
        getData();
        return rootView;
    }

    private void getData() {
        onLoad();
        listView.setVisibility(View.VISIBLE);
        //progressBar.setVisibility(View.GONE);
        MyShowAdapter myadapter = new MyShowAdapter(getActivity(),1);
        listView.setAdapter(myadapter);
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos=position;
                VLCApplication app = (VLCApplication)getActivity().getApplication();
                app.setVideoPos(pos);
                catchTheOrderDialog.show();
                jumpVlc();
            }
        });*/

    }

    public class MyShowAdapter extends BaseAdapter {

        private Context context;
        private int size;
        private int scrollStauts = 0;
        //private ArrayList<ContactsBean> contactsBeen = new ArrayList<>();

        public MyShowAdapter(Context context, int size){
            this.context = context;
            this.size = size;
            //this.contactsBeen = contactsBeen;
        }
        public void updateUI(){
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount(){
            // TODO Auto-generated method stub
            //return list.size();
            return size;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.tab1_item, null);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                //holder.phone = (TextView) convertView.findViewById(R.id.phone);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.clean();
            }
            holder.name.setText("4G版本重新设置WiFi");
            //holder.phone.setText(contactsBeen.get(position).getPhone());
            return convertView;
        }
        public final class ViewHolder{
            public TextView name,phone;
            void clean(){
                name.setText(null);
                //phone.setText(null);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), VlcPlayerSaveActivity.class);
        startActivity(intent);
    }

    private void onLoad() {
        listView.stopRefresh();
        listView.stopLoadMore();
        listView.setRefreshTime(getNowDate());//下拉时显示上次刷新时间
    }
    /**
     * 获取当前系统时间
     *
     * @return 返回时间类型（yy月）
     */
    public static String getNowDate() {
        String strDate = null;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        strDate = format.format(date);
        return strDate;
    }

    @Override
    public void onRefresh() {
        getData();
    }

    @Override
    public void onLoadMore() {

    }
}
