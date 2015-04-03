package com.anthonywang.tsinghua.oildetector;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.internal.view.menu.MenuView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.anthonywang.tsinghua.oildetector.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Report extends Activity {

    private WebView mWebView;
    private Button btnjs;
    private MyApplication app;
    private ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        app = (MyApplication)getApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        mWebView = (WebView) findViewById(R.id.webview);
        btnjs = (Button) findViewById(R.id.btnjs);
//        tv = (TextView) findViewById(R.id.tv);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        mWebView.loadUrl("file:///android_asset/rader.html");
        mWebView.addJavascriptInterface(this, "rader");
        btnjs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tv.setText("clicked");
//                String data = Double.toString(65.9);
                for (int i = 0; i < 40; i++) {
                    if (app.allShowDataNum < 1)
                        return;
                    double get_double = Double.parseDouble(String.format("%.10f",app.allShowData[app.allShowDataNum-1][i]));
                    String data = Double.toString(get_double);
                    mWebView.loadUrl("javascript:setData('" + Integer.toString(i/10) + "','" + Integer.toString(i%10) + "','" + data + "')");
                }
                mWebView.loadUrl("javascript:repaint()");
            }
        });
        lv = (ListView)findViewById(R.id.list);
    }

    @Override
    protected void onResume() {

        lv.setAdapter(new MyShowAdapter(this));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MyShowAdapter extends BaseAdapter {

        private MyApplication app;
        private Context context;
        private int length;
        private int[] resultId;
        private String[] timeStr;
        String [] oilKind;
        public MyShowAdapter(Context context) {
            app = (MyApplication)getApplication();
            this.context = context;
            length = app.allShowDataNum;
//            resultId = new int[100];
//            timeStr = new String[100];
//            oilKind = new String[100];
//            oilKind[0] = "花生油";
//            oilKind[1] = "橄榄油";
//            oilKind[2] = "鱼油";
//            oilKind[3] = "调和油";
//            oilKind[4] = "辣椒油";
//            oilKind[5] = "花生油地沟油";
//            oilKind[6] = "玉米油地沟油";
//            oilKind[7] = "鱼油地沟油";
//            oilKind[8] = "调和油地沟油";
//            oilKind[9] = "辣椒油地沟油";
//            //for test
//            resultId[0] = 1;
//            resultId[1] = 2;
//            timeStr[0] = "time0";
//            timeStr[1] = "time1";
        }



        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            if(convertView==null){
                convertView=LayoutInflater.from(context).inflate(R.layout.adater_line, null);

                ItemViewCache viewCache=new ItemViewCache();
                viewCache.result=(TextView)convertView.findViewById(R.id.result);
                viewCache.time=(TextView)convertView.findViewById(R.id.time);
                convertView.setTag(viewCache);
            }
            ItemViewCache cache=(ItemViewCache)convertView.getTag();
            cache.result.setText(app.oilKind[app.answer[position]]);
            cache.time.setText(app.time[position]);
            return convertView;
        }

        public class ItemViewCache
        {
            public TextView result;
            public TextView time;
        }
    }
}
