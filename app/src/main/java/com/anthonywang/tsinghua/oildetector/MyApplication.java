package com.anthonywang.tsinghua.oildetector;

import android.app.Application;

/**
 * Created by apple on 15/3/21.
 */
public class MyApplication extends Application {
    public boolean serviceRuning;
    double[][] allShowData = null;
    public int allShowDataNum;
    public int []answer = null;
    String[] oilKind = null;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceRuning = false;
        allShowDataNum = 0;
        allShowData = new double[100][40];
        oilKind = new String[100];
        answer = new int[100];
        oilKind[0] = "花生油";
        oilKind[1] = "橄榄油";
        oilKind[2] = "鱼油";
        oilKind[3] = "调和油";
        oilKind[4] = "辣椒油";
        oilKind[5] = "花生油地沟油";
        oilKind[6] = "玉米油地沟油";
        oilKind[7] = "鱼油地沟油";
        oilKind[8] = "调和油地沟油";
        oilKind[9] = "辣椒油地沟油";
    }

}
