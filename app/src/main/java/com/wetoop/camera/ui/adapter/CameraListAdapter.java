package com.wetoop.camera.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetoop.camera.bean.AllCameraListBean;
import com.wetoop.cameras.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by User on 2018/1/30.
 */

public class CameraListAdapter extends BaseAdapter {

    private Context context;
    private int size;
    private int scrollStatus = 0;
    private ArrayList<AllCameraListBean> cameraList = new ArrayList<>();

    public CameraListAdapter(Context context, int size, ArrayList<AllCameraListBean> cameraList, ListView listView){
        this.context = context;
        this.size = size;
        this.cameraList = cameraList;
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub  
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE://停止  
                        scrollStatus = 0;
                        updateUI();
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸滑动  
                        scrollStatus = 1;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING://快速滑动 
                        scrollStatus = 2;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
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
    public View getView(int position, View convertView, ViewGroup parent){
        // TODO Auto-generated method stub
        ViewHolder holder ;
        if(convertView==null){
            holder= new ViewHolder();
            convertView= LayoutInflater.from(context).inflate(R.layout.tab2_item, null);
            holder.name = (TextView)convertView.findViewById(R.id.name);
            holder.sn = (TextView)convertView.findViewById(R.id.sn);
            holder.online = (TextView)convertView.findViewById(R.id.online);
            holder.iv = (ImageView)convertView.findViewById(R.id.imageView_device);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
            holder.clean();
        }
        RelativeLayout media_title = (RelativeLayout)convertView.findViewById(R.id.media_title);
        RelativeLayout shareR = (RelativeLayout) convertView.findViewById(R.id.shareR);
        TextView joinText = (TextView) convertView.findViewById(R.id.joinText);
        TextView shareText = (TextView) convertView.findViewById(R.id.shareText);

        if(cameraList.size()>0){
            if(cameraList.get(position).getOnline().equals("0")){
                holder.online.setText("【不在线】");
                holder.online.setTextColor(context.getResources().getColor(R.color.galaeye_grey));
            }else{
                holder.online.setText("【在线】");
                holder.online.setTextColor(context.getResources().getColor(R.color.green));
            }
            if(cameraList.get(position).isJoin()){//已参加
                joinText.setVisibility(View.VISIBLE);
                holder.name.setText("名称：" + cameraList.get(position).getDeviceName());
                holder.sn.setText(cameraList.get(position).getSn());
                String myJpgPath = getSDPath() + "/FGCamera/" + cameraList.get(position).getNet_id() + ".png";
                File f = new File(myJpgPath);
                if (f.exists()) {
                    if (scrollStatus == 0) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap bm = BitmapFactory.decodeFile(myJpgPath, options);
                        holder.iv.setImageBitmap(bm);
                        holder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                } else {
                    holder.iv.setImageResource(R.drawable.video_placeholder);
                }
            }else {//未参加共享的
                joinText.setVisibility(View.GONE);
                if (cameraList.get(position).getNet_id().equals("") || cameraList.get(position).getSn().equals("")) {
                    media_title.setVisibility(View.GONE);
                } else {
                    media_title.setVisibility(View.VISIBLE);
                    holder.name.setText("名称：" + cameraList.get(position).getDeviceName());
                    holder.sn.setText(cameraList.get(position).getSn());
                    int shareNum = cameraList.get(position).getShareNum();
                    if(shareNum>0){
                        shareText.setText("共享数："+shareNum);
                    }
                    String myJpgPath = getSDPath() + "/FGCamera/" + cameraList.get(position).getNet_id() + ".png";
                    File f = new File(myJpgPath);
                    if (f.exists()) {
                        if (scrollStatus == 0) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap bm = BitmapFactory.decodeFile(myJpgPath, options);
                            holder.iv.setImageBitmap(bm);
                            holder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    } else {
                        holder.iv.setImageResource(R.drawable.video_placeholder);
                    }
                }
            }
        }

        return convertView;
    }
    private final class ViewHolder{
        public TextView name;
        public TextView sn;
        public TextView online;
        ImageView iv;
        void clean(){
            name.setText(null);
            sn.setText(null);
            online.setText(null);
            iv.setImageBitmap(null);
        }
    }
    private String getSDPath() {
        File sdDir = context.getFilesDir();
        File destDir = new File(sdDir + "/FGCamera");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return sdDir.toString();
    }
}