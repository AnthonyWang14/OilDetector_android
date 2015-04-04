package com.anthonywang.tsinghua.oildetector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.TextView;

public class Report extends Activity {

    private WebView mWebView;
    private Button btnjs;
    private MyApplication app;
    private ListView lv;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        app = (MyApplication)getApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        mWebView = (WebView) findViewById(R.id.webview);
        btnjs = (Button) findViewById(R.id.btnjs);
        tv = (TextView) findViewById(R.id.tv);
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
        public MyShowAdapter(Context context) {
            app = (MyApplication)getApplication();
            this.context = context;
            length = app.allShowDataNum;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
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
            //给每个item设置点击事件
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv.setText("测试组"+position+": " + app.oilKind[app.answer[position]] + "\n" + app.time[position]);
                    TextView t = (TextView)findViewById(R.id.tvtest);
                    String temp = "";
                    System.out.println("第"+position);
                    for (int i = 0; i < 40; i++) {
                        if (app.allShowDataNum < 1)
                            return;
                        double get_double = Double.parseDouble(String.format("%.10f",app.allShowData[position][i]));
                        String data = Double.toString(get_double);
                        System.out.println(data);
                        temp = temp+data;
                        mWebView.loadUrl("javascript:setData('" + Integer.toString(i/10) + "','" + Integer.toString(i%10) + "','" + data + "')");
                    }
                    mWebView.loadUrl("javascript:repaint()");
                    t.setText(temp);
                }
            });
            return convertView;
        }
        public class ItemViewCache
        {
            public TextView result;
            public TextView time;
        }
    }
}
