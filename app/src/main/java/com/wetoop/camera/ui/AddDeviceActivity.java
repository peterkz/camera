package com.wetoop.camera.ui;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.wetoop.camera.ui.fragment.AddDeviceFirstFragment;
import com.wetoop.camera.ui.fragment.AddDeviceSecondFragment;
import com.wetoop.camera.listview.MyViewPager;
import com.wetoop.cameras.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/21.
 */
public class AddDeviceActivity extends AppCompatActivity {
    private TabHost mTabHost;
    private MyViewPager mViewPager; // 下方的可横向拖动的控件
    private ArrayList<Fragment> fragmentList;// 用来存放下方滚动的layout(layout_1,layout_2,layout_3)
    private int firstComing=0;//在未初始化完两个Tab时，会先进入onTabChanged，如果不加限制会崩
    private static final String TAB1 = "First";
    private static final String TAB2 = "Second";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        RelativeLayout back = (RelativeLayout)findViewById(R.id.add_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mViewPager = (MyViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());

        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setup();
        mTabHost.setOnTabChangedListener(listener);

        initializeTabs();
        inVariable();

        //设置点击之后背景颜色变化
        setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
        setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
        firstComing=1;
    }
    /**
     * 显示四个页面的布局
     */
    private void inVariable() {
        fragmentList = new ArrayList<>();
        Fragment addDeviceFirstFragment = new AddDeviceFirstFragment();
        Fragment addDeviceSecondFragment = new AddDeviceSecondFragment();
        fragmentList.add(addDeviceFirstFragment);
        fragmentList.add(addDeviceSecondFragment);
        mViewPager.setAdapter(new myPageAdapter(getSupportFragmentManager()));
    }
    private class myPageAdapter extends FragmentStatePagerAdapter {
        FragmentManager fm;

        public myPageAdapter(FragmentManager fm) {
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
    private void initializeTabs() {
        TabHost.TabSpec spec;
        //局域网寻找模式
        spec = mTabHost.newTabSpec(TAB1);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView(getString(R.string.tabadd1)));
        mTabHost.addTab(spec);
        //单设备寻找模式
        spec = mTabHost.newTabSpec(TAB2);
        spec.setContent(new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return findViewById(R.id.tab_content_frame);
            }
        });
        spec.setIndicator(createTabView( getString(R.string.tabadd2)));
        mTabHost.addTab(spec);


    }
    private View createTabView( final String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.tabs_bar_add_device, null);
        // 如果担心图片过大或者图片过多影响内存和加载效率,可以自己解析图片然后通过调用setImageDrawable方法进行设置
        TextView textView = (TextView) view.findViewById(R.id.tab_text);
        textView.setText(text);
        return view;
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setTabBarItemIndicator(View view, boolean active) {
            TextView textView = (TextView) view.findViewById(R.id.tab_text);
        TextView view1 = (TextView)view.findViewById(R.id.view);
        if (active) {
            textView.setTextColor(getResources().getColor(R.color.action_blue));
            view1.setVisibility(View.VISIBLE);
        } else {
            textView.setTextColor(getResources().getColor(R.color.forget_pwd));
            view1.setVisibility(View.GONE);
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
        @Override
        public void onPageSelected(int position) {
            // TODO Auto-generated method stub

            if (position == 0) {
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
            } else if (position == 1) {
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), true);
            }
        }
    }

    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            if (tabId.equals(TAB1)) {
                mViewPager.setCurrentItem(0);
                if (firstComing == 1) {
                    setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), true);
                    setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), false);
                }
            } else if (tabId.equals(TAB2)) {
                mViewPager.setCurrentItem(1);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(0), false);
                setTabBarItemIndicator(mTabHost.getTabWidget().getChildTabViewAt(1), true);
            }
        }

    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent1 = new Intent(AddDeviceActivity.this, MainActivity.class);
            startActivity(intent1);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}