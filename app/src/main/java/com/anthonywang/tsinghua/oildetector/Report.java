package com.anthonywang.tsinghua.oildetector;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.anthonywang.tsinghua.oildetector.R;

public class Report extends Activity {

    private WebView mWebView;
    private Button btnjs;
    private TextView tv;
    private MyApplication app;
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
}
