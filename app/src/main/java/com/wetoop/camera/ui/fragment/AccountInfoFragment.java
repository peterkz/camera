package com.wetoop.camera.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.wetoop.camera.ui.UserInfoActivity;
import com.wetoop.camera.App;
import com.wetoop.camera.service.VideoService;
import com.wetoop.camera.ui.LoginActivity;
import com.wetoop.cameras.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/4/18.
 */
public class AccountInfoFragment extends Fragment {
    private ListView listView;
    private ArrayList<HashMap<String, Object>> listItem;
    private RelativeLayout login;
    private App app;
    private TextView login_state;
    private int count_login=0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView;
        rootView = inflater.inflate(R.layout.fragment_account_info, container, false);
        listView = (ListView)rootView.findViewById(R.id.tab3_listView);
        listView.addHeaderView(LayoutInflater.from(getActivity()).inflate(
                R.layout.tab3_head, null));
        SimpleAdapter mSimpleAdapter = new SimpleAdapter(getActivity(),getData(),
                R.layout.tab3_item,new String[]{"image","textView","versions","imageView"},new int[]{R.id.image,R.id.textView,R.id.versions,R.id.imageView});
        listView.setAdapter(mSimpleAdapter);//为ListView绑定适配器

        app = (App) getActivity().getApplication();
        login_state = (TextView)rootView.findViewById(R.id.login_state);
        if(app.getToken()!=null){
            if(!app.getToken().equals("")){
                login_state.setText("已登录");
                count_login=1;
            }else{
                login_state.setText("未登录");
            }
        }

            login = (RelativeLayout)rootView.findViewById(R.id.r2);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(count_login==1){
                        Intent intent = new Intent(getActivity(),UserInfoActivity.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getActivity(), VideoService.class);
                        intent.setPackage(getActivity().getPackageName());
                        getActivity().stopService(intent);
                        Intent intentLogin = new Intent(getActivity(),LoginActivity.class);
                        startActivity(intentLogin);
                        App app = (App)getActivity().getApplication();
                        app.exit();
                    }
                }
            });

        return rootView;
    }
    private ArrayList<HashMap<String, Object>> getData(){
        listItem = new ArrayList<>();
        HashMap<String, Object> map1 = new HashMap<String, Object>();
        map1.put("image", R.mipmap.tab3_help);
        map1.put("textView","使用帮助");
        map1.put("versions", "");
        map1.put("imageView", R.mipmap.ic_next);
        listItem.add(map1);

        HashMap<String, Object> map2 = new HashMap<String, Object>();
        map2.put("image", R.mipmap.r23);
        map2.put("textView","关于我们");
        map2.put("versions", "");
        map2.put("imageView", R.mipmap.ic_next);
        listItem.add(map2);

        HashMap<String, Object> map3 = new HashMap<String, Object>();
        map3.put("image", R.mipmap.r24);
        map3.put("textView","客服电话");
        map3.put("versions", "0771-000000");
        listItem.add(map3);

        HashMap<String, Object> map4 = new HashMap<String, Object>();
        map4.put("image", R.mipmap.r25);
        map4.put("textView","应用版本");
        map4.put("versions", "1.0(1)");
        listItem.add(map4);

        return listItem;
    }

}
