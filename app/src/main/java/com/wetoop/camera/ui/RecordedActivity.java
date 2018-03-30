package com.wetoop.camera.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wetoop.camera.CameraJni;
import com.wetoop.camera.bean.RecordedBean;
import com.wetoop.camera.datepicker.MonthDateView;
import com.wetoop.cameras.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/12.
 */

public class RecordedActivity extends FragmentActivity {
    private ImageView iv_left;
    private ImageView iv_right;
    private TextView tv_date,title_text;
    private TextView tv_week;
    private TextView tv_today;
    private MonthDateView monthDateView;
    private List<Integer> list = new ArrayList<Integer>();
    private ArrayList<RecordedBean> recordingBeansArray;
    private ListView listView;
    private RelativeLayout back;
    private TextView noData;
    private ProgressBar pb;
    private int[] monthList;
    private String[] dayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorded);
        iv_left = (ImageView) findViewById(R.id.iv_left);
        iv_right = (ImageView) findViewById(R.id.iv_right);
        listView = (ListView)findViewById(R.id.recorded_list);
        monthDateView = (MonthDateView) findViewById(R.id.monthDateView);
        tv_date = (TextView) findViewById(R.id.date_text);
        tv_week  =(TextView) findViewById(R.id.week_text);
        tv_today = (TextView) findViewById(R.id.tv_today);
        title_text = (TextView)findViewById(R.id.title_text);
        Intent intent = getIntent();
        title_text.setText(intent.getStringExtra("title"));
        monthDateView.setTextView(tv_date,tv_week);
        monthDateView.setDateClick(new MonthDateView.DateClick() {

            @Override
            public void onClickOnDate() {
                System.out.println("day="+monthDateView.getmSelDay()+"<>month="+monthDateView.getmSelMonth());
                getDayData(2017,monthDateView.getmSelMonth(),monthDateView.getmSelDay());
                //Toast.makeText(getApplication(), "点击了：" + monthDateView.getmSelDay(), Toast.LENGTH_SHORT).show();
            }
        });
        back = (RelativeLayout)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        noData = (TextView)findViewById(R.id.noDateText);
        pb = (ProgressBar)findViewById(R.id.progressBar);
        noData.setVisibility(View.VISIBLE);
        pb.setVisibility(View.VISIBLE);
        getMonthData(2017,monthDateView.getmSelMonth());
        setOnlistener();
    }

    private void setOnlistener(){
        iv_left.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                monthDateView.onLeftClick();
                System.out.println(monthDateView.getmSelMonth()+"月");
                getMonthData(2017,monthDateView.getmSelMonth());
            }
        });

        iv_right.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                monthDateView.onRightClick();
                System.out.println(monthDateView.getmSelMonth()+"月");
                getMonthData(2017,monthDateView.getmSelMonth());
                /*list.add(1);
                list.add(4);
                list.add(6);
                list.add(9);
                monthDateView.setDaysHasThingList(list);*/
            }
        });

        tv_today.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                monthDateView.setTodayToView();
            }
        });
    }

    private void getMonthData(final int year, final int month) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                monthList = CameraJni.searchByMonth(year,month);
                if(monthList.length>=0){
                    Message msg =handler.obtainMessage();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }else{
                    Message msg =handler.obtainMessage();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }
        });
        t.start();
    }
    private void getDayData(final int year, final int month, final int day) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run(){
                dayList = CameraJni.searchByDay(year, month, day);
                if(dayList.length>=0){
                    Message msg =handler.obtainMessage();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }else{
                    Message msg =handler.obtainMessage();
                    msg.what = 4;
                    handler.sendMessage(msg);
                }
            }
        });
        t.start();
    }

    private void getData() {
        System.out.println("getData====");
        pb.setVisibility(View.GONE);
        MyShowAdapter myadapter = new MyShowAdapter(RecordedActivity.this, recordingBeansArray);
        myadapter.notifyDataSetChanged();
        listView.setAdapter(myadapter);
    }

    public class MyShowAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<RecordedBean> talkingList;

        public MyShowAdapter(Context context, ArrayList<RecordedBean> talkingList) {
            this.context = context;
            this.talkingList = talkingList;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            //return list.size();
            return talkingList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return talkingList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.recored_item, null);
            }
            TextView name = (TextView) convertView.findViewById(R.id.username);

            if (talkingList.size() > 0) {
                name.setText(talkingList.get(position).getRecordingName());
            }
            return convertView;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage (Message msg){
            switch (msg.what) {
                case 1:
                    //System.out.println("1");
                    if(monthList.length==1){
                        if(monthList[0]==32){
                            Toast.makeText(RecordedActivity.this,"搜索出错",Toast.LENGTH_SHORT).show();
                        }
                    }else if(monthList.length==0){
                        //noData
                        listView.setVisibility(View.GONE);
                        noData.setVisibility(View.VISIBLE);
                        pb.setVisibility(View.VISIBLE);
                    }else{
                        list.clear();
                        for(int i=0;i<monthList.length;i++){
                            list.add(monthList[i]);
                        }
                        monthDateView.setDaysHasThingList(list);
                    }
                    break;
                case 2:
                    Toast.makeText(RecordedActivity.this,"搜索出错",Toast.LENGTH_SHORT).show();
                    //System.out.println("2");
                    break;
                case 3:
                    System.out.println("3");
                    break;
                case 4:
                    //error
                    System.out.println("4");
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //Intent intent = new Intent(Recorded.this, DeviceInfo.class);
            //startActivity(intent);
            finish();
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }
}

