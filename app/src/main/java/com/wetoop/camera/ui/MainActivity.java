package com.wetoop.camera.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.wetoop.camera.CameraJni;
import com.wetoop.camera.MyCrashHandler;
import com.wetoop.camera.tools.PermissionUtil;
import com.wetoop.camera.ui.fragment.AccountInfoFragment;
import com.wetoop.camera.App;
import com.wetoop.camera.api.ResultMessage;
import com.wetoop.camera.listview.MyViewPager;
import com.wetoop.camera.service.VideoService;
import com.wetoop.camera.ui.fragment.CameraListFragment;
import com.wetoop.camera.ui.fragment.SavedFragment;
import com.wetoop.cameras.R;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private TabHost mTabHost;
    private MyViewPager mViewPager; // 下方的可横向拖动的控件
    private ArrayList<Fragment> fragmentList;
    private int tab1_test=0;
    private static final String TAB1 = "TAB1";
    private static final String TAB2 = "TAB2";
    private static final String TAB3 = "TAB3";
    private MyBroadcastReceiver broadcastReceiverLive;

    @TargetApi(Build.VERSION_CODES.DONUT)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        broadcastReceiverLive = new MyBroadcastReceiver();
        IntentFilter intentFilterLive = new IntentFilter("videoPortChange");
        registerReceiver(broadcastReceiverLive, intentFilterLive);
        mViewPager = (MyViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());
        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setup();
        mTabHost.setOnTabChangedListener(listener);
        initializeTabs();
        inVariable();
        mTabHost.setCurrentTab(1);
        //设置点击之后背景颜色变化
        setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(0), false);
        setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(1), true);
        setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
        tab1_test=1;
        SetViewData();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void SetViewData(){
        Intent intent = getIntent();
        String scheme = intent.getScheme();
        Uri uri = intent.getData();
        if(uri != null){
            String host = uri.getHost();
            String dataString = intent.getDataString();
            if(dataString.indexOf("#GALA")>0){
                final ProgressDialog catchTheOrderDialog = new ProgressDialog(MainActivity.this);
                catchTheOrderDialog.setTitle("添加共享摄像头");
                catchTheOrderDialog.setMessage("检测到分享码，正在添加···");
                catchTheOrderDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                    }
                });
                catchTheOrderDialog.setCancelable(false);
                catchTheOrderDialog.setCanceledOnTouchOutside(false);
                catchTheOrderDialog.show();
                String[] s = dataString.split("#");
                App app = (App) getApplication();
                app.getApiService().share_add(app.getToken(), s[1], new Callback<ResultMessage>() {
                    @Override
                    public void success(ResultMessage resultMessage, Response response) {
                        catchTheOrderDialog.dismiss();
                        if(resultMessage.getCode()==200){
                            Toast.makeText(MainActivity.this,resultMessage.getErrorMessage(),Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this,resultMessage.getErrorMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        catchTheOrderDialog.dismiss();
                        Toast.makeText(MainActivity.this,"连接服务器失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        ClipboardManager clipboardmanager =(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData =clipboardmanager.getPrimaryClip();
        if(clipData!=null) {
            String name = clipData.getItemAt(0).getText().toString();
            System.out.println("name=" + name);
        }
        App app = (App)getApplication();
        app.addActivity(this);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
            }
        }
        /*String fcmToken = FirebaseInstanceId.getInstance().getToken();
        app.getApiService().FCMPost(app.getToken(), fcmToken, "android", new Callback<ResultMessage>() {
            @Override
            public void success(ResultMessage resultMessage, Response response) {
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });*/
    }

    /**
     * 显示四个页面的布局
     */
    private void inVariable() {
        fragmentList = new ArrayList<>();
        Fragment tab1 = new SavedFragment();
        Fragment tab2 = new CameraListFragment();
        Fragment tab3 = new AccountInfoFragment();
        fragmentList.add(tab1);
        fragmentList.add(tab2);
        fragmentList.add(tab3);
        mViewPager.setAdapter(new MyPageAdapter(getSupportFragmentManager()));
    }
    private class MyPageAdapter extends FragmentStatePagerAdapter {
        FragmentManager fm;

        public MyPageAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {

            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }

    /**
     * 找到标题栏按钮
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem shareItem = menu.findItem(R.id.action_settings);
        shareItem.setVisible(true);//设置标题栏按钮可见
        return super.onCreateOptionsMenu(menu);
    }

    //初始化tabhost
    @TargetApi(Build.VERSION_CODES.DONUT)
    private void initializeTabs() {
        TabHost.TabSpec spec;
        //已保存
        spec = mTabHost.newTabSpec(TAB1);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(R.mipmap.tab21,  getString(R.string.tab2)));
        mTabHost.addTab(spec);
        //直播
        spec = mTabHost.newTabSpec(TAB2);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(R.mipmap.tab11, getString(R.string.tab1)));
        mTabHost.addTab(spec);
        //帐号信息
        spec = mTabHost.newTabSpec(TAB3);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(R.mipmap.tab31, getString(R.string.tab3)));
        mTabHost.addTab(spec);
    }
    private View createTabView(final int id, final String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_tabs_bar, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        imageView.setImageDrawable(getResources().getDrawable(id));//setImageDrawable是最省内存高效的,
        // 如果担心图片过大或者图片过多影响内存和加载效率,可以自己解析图片然后通过调用setImageDrawable方法进行设置
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        textView.setText(text);
        return view;
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiverLive);
    }

    private void setTabBarItemIndicator1(View view, boolean active) {
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        if (active) {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.tab12));
            textView.setTextColor(getResources().getColor(R.color.action_blue));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.tab11));
            textView.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    private void setTabBarItemIndicator2(View view, boolean active) {
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        if (active) {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.tab22));
            textView.setTextColor(getResources().getColor(R.color.action_blue));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.tab21));
            textView.setTextColor(getResources().getColor(R.color.grey));
        }
    }
    private void setTabBarItemIndicator3(View view, boolean active) {
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        if (active) {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.tab32));
            textView.setTextColor(getResources().getColor(R.color.action_blue));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.tab31));
            textView.setTextColor(getResources().getColor(R.color.grey));
        }
    }

    /**
     * ViewPager的PageChangeListener(页面改变的监听器)
     */
    private class MyPagerOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub
        }

        /**
         * 滑动ViewPager的时候,让上方的HorizontalScrollView自动切换
         */
        @TargetApi(Build.VERSION_CODES.DONUT)
        @Override
        public void onPageSelected(int position) {
            // TODO Auto-generated method stub

            if (position == 0) {
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
            } else if (position == 1) {
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(1), true);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
            } else if (position == 2) {
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), true);
            }
        }
    }

    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
        @TargetApi(Build.VERSION_CODES.DONUT)
        @Override
        public void onTabChanged(String tabId) {
            if (tabId.equals(TAB1)) {
                mViewPager.setCurrentItem(0);
                if (tab1_test == 1) {
                    setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                    setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                    setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
                }
            } else if (tabId.equals(TAB2)) {
                mViewPager.setCurrentItem(1);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(1), true);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), false);
            } else if (tabId.equals(TAB3)) {
                mViewPager.setCurrentItem(2);
                setTabBarItemIndicator2(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator1(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                setTabBarItemIndicator3(mTabHost.getTabWidget().getChildTabViewAt(2), true);
            }
        }

    };

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras().getString("killTheService")!=null){
                if("true".equals(intent.getExtras().getString("killTheService"))){
                    intent = new Intent(MainActivity.this, VideoService.class);
                    intent.setPackage(getPackageName());
                    stopService(intent);
                    ActivityManager manager1 = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
                    manager1.killBackgroundProcesses(getPackageName());
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            App app = (App)getApplication();
            app.setAudioCome(0);
            Intent intent = new Intent(MainActivity.this, VideoService.class);
            intent.setPackage(getPackageName());
            MainActivity.this.stopService(intent);
            //CameraJni.tcpStop();
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
