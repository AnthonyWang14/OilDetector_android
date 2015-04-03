package com.anthonywang.tsinghua.oildetector;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class BlueTooth extends Activity implements OnClickListener {
    /** Called when the activity is first created. */


    //用来保存整个应用的全局变量
    private MyApplication app;


    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄
    TextView myTextView;
    Button sendButton;
    Button startButton;
    Button paintButton;
    MyReceiver receiver;
    IBinder serviceBinder;
    //    MyService mService;
    //用来标记是否开启连接服务
    boolean started;
    BTService btService;
    Intent intent;
    int value = 0;

    /**************service 命令*********/
    static final int CMD_START_BLUETOOTH = 0x00;
    static final int CMD_STOP_SERVICE = 0x01;
    static final int CMD_SEND_DATA = 0x02;
    static final int CMD_SYSTEM_EXIT =0x03;
    static final int CMD_SHOW_TOAST =0x04;


    final int HEIGHT=320;   //设置画图范围高度
    final int WIDTH=320;    //画图范围宽度
    final int X_OFFSET = 5;  //x轴（原点）起始位置偏移画图范围一点
    final int DataSetSize = 60;
    final float DX = (float)WIDTH/DataSetSize;    //一组数据共50个点
    final float DY = (float)HEIGHT/65535;
    private int cx = X_OFFSET;  //实时x的坐标
    int centerY = HEIGHT /2;  //y轴的位置
    private int dataNum;       //数据集中现在已有的个数：[0,50)，到50之后清空
    public int dataY[][];
    private int paintflag;        //是否绘图标志位，当func=9且有数据读入时，paintflag置为1，绘图线程就会绘图

    private SurfaceHolder holder = null;    //画图使用，可以控制一个SurfaceView
    private Paint paint = null;      //画笔
    SurfaceView surface = null;     //


    private Button btnTest, btnReset, btnState, btnLowLose, btnSpeed, btnPaint, btnRecvData;


    public AlgoWrapper algoWrapper = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);
        app = (MyApplication)getApplication();

//        started = false;

        //开始服务

        sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new SendButtonClickListener());
        sendButton.setEnabled(false);

        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        paintButton = (Button)findViewById(R.id.paintButton);
        paintButton.setOnClickListener(this);



        //初始化SurfaceHolder对象
        surface = (SurfaceView)findViewById(R.id.show);
        holder = surface.getHolder();
        holder.setFixedSize(WIDTH+10, HEIGHT+10);  //设置画布大小，要比实际的绘图位置大一点
        /*设置波形的颜色等参数*/
        paint = new Paint();
        paint.setColor(Color.GREEN);  //画波形的颜色是绿色的，区别于坐标轴黑色
        paint.setStrokeWidth(3);

        dataNum = 0;
        dataY = new int[4][DataSetSize+10];
        paintflag = 0;
        //建立algoWrapper
        algoWrapper = new AlgoWrapper();
//        algoWrapper.getShowData();
        int ans = algoWrapper.doCal();
        sendButton.setText(app.oilKind[ans]);
        System.out.println("result is " + ans);
//        paintButton.setText(algoWrapper.doCal());
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
                drawBack(holder);
                //如果没有这句话，会使得在开始运行程序，整个屏幕没有白色的画布出现
                //直到按下按键，因为在按键中有对drawBack(SurfaceHolder holder)的调用
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                if (!app.serviceRuning) {
                    Intent i = new Intent(BlueTooth.this, BTService.class);
                    startService(i);
                    Intent serverIntent = new Intent(this, DeviceList.class); //跳转程序设置
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
                }
                else {
                    Intent i = new Intent(BlueTooth.this, BTService.class);
                    stopService(i);
                    app.serviceRuning = false;
                    startButton.setText("连接蓝牙");
                    startButton.setBackgroundColor(Color.rgb(85,54,121));
                    paintButton.setEnabled(false);
                    paintButton.setBackgroundColor(Color.GRAY);
                    paintflag = 0;
                    dataNum = 0;
                }
                break;
            case R.id.paintButton:
                paintflag = 1;
                dataNum = 0;

                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    String address = data.getExtras()
                            .getString(DeviceList.EXTRA_DEVICE_ADDRESS);
                    app.serviceRuning = true;
                    startButton.setText("断开蓝牙");
                    paintButton.setEnabled(true);
                    paintButton.setBackgroundColor(Color.argb(255,40,255,252));
                    showToast("连接蓝牙成功");
                    Intent i = new Intent();
                    i.setAction("android.intent.action.cmd");
                    i.putExtra("cmd", CMD_START_BLUETOOTH);
                    i.putExtra("address", address);
                    sendBroadcast(i);
                }
                break;
            default:break;
        }
    }

    public class SendButtonClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
//            // TODO Auto-generated method stub
//            byte command = 45;
//            int value = 0x12345;
//            sendCmd(command,value);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if(receiver!=null){
            BlueTooth.this.unregisterReceiver(receiver);
        }

    }



    //回到前台后的执行的函数
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (receiver == null) {
            receiver = new MyReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.back");
            BlueTooth.this.registerReceiver(receiver, filter);
        }
        if (app.serviceRuning) {
            paintButton.setEnabled(true);
            paintButton.setBackgroundColor(Color.argb(255,40,255,252));
            startButton.setText("断开蓝牙");
            startButton.setBackgroundColor(Color.RED);
        }
        else {
            paintButton.setEnabled(false);
            paintButton.setBackgroundColor(Color.GRAY);
            startButton.setText("连接蓝牙");
            startButton.setBackgroundColor(Color.rgb(85,54,121));
        }
    }

    public void showToast(String str){//显示提示信息
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }


    public class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if(intent.getAction().equals("android.intent.action.back")){
                int cmd = intent.getIntExtra("cmd", -1);
                switch (cmd) {
                    case CMD_SEND_DATA:
                        handleMessage(intent.getStringExtra("msg"));
                }
//                if(cmd == CMD_SHOW_TOAST){
//                    String str = bundle.getString("str");
//                    showToast(str);
//                }
//
//                else if(cmd == CMD_SYSTEM_EXIT){
//                    System.exit(0);
//                }
            }
        }
    }

    //消息处理队列
    public void handleMessage(String smsg){
        if (paintflag == 0)
            return;
        System.out.println("do handleMessage!");
//            dis.setText(smsg);   //显示数据
        System.out.println(smsg);

//            sv.scrollTo(0,dis.getMeasuredHeight()); //跳至数据最后一页
        //——————<简化版协议，eg：编号：212，数据1-4：45000，23000，23421，43333
        if (!smsg.equals("")) {
            String[] strarray = null;
            strarray = smsg.split(",");
            //测试收到的字符串已经可以分解成五个一组的形式形式
            if (strarray.length  >= 5) {

                //如果数据集已经满了，则将新进入的点从头开始画

                if (dataNum == DataSetSize) {
                    dataNum = 0;
                    paintflag = 0;
                    int ans = algoWrapper.doCal();
                    sendButton.setText(app.oilKind[ans]);
                    return;
//                        outputResult();
                }
                for (int i = 1; i < 5; i++) {
                    dataY[i-1][dataNum] = Integer.valueOf(strarray[i]);
                    System.out.println(dataY[i-1][dataNum]);
                }
                dataNum++;
                paintWave();
            }
        }
        //——————简化版协议>
    }

    //    设置画布背景色，设置XY轴的位置
    private void drawBack(SurfaceHolder holder){
        Canvas canvas = holder.lockCanvas(); //锁定画布
        //绘制白色背景
        canvas.drawColor(Color.BLACK);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStrokeWidth(2);

        //绘制坐标轴
        canvas.drawLine(X_OFFSET, HEIGHT, WIDTH, HEIGHT, p); //绘制X轴 前四个参数是起始坐标
        canvas.drawLine(X_OFFSET, 0, X_OFFSET, HEIGHT, p); //绘制Y轴 前四个参数是起始坐标

        holder.unlockCanvasAndPost(canvas);  //结束锁定 显示在屏幕上
        holder.lockCanvas(new Rect(0,0,0,0)); //锁定局部区域，其余地方不做改变
        holder.unlockCanvasAndPost(canvas);
    }


    public void paintWave() {
        int x0, y0, x1, y1;
        Canvas canvas;
        if (dataNum == 1) {
            drawBack(holder);
            for (int i = 0; i < 4; i++) {
                switch(i) {
                    case 0:
                        paint.setColor(Color.RED);
                        break;
                    case 1:
                        paint.setColor(Color.YELLOW);
                        break;
                    case 2:
                        paint.setColor(Color.BLUE);
                        break;
                    case 3:
                        paint.setColor(Color.GREEN);
                        break;
                }
                x0 = X_OFFSET;
                y0 = HEIGHT - (int) (dataY[i][0] * DY);
                System.out.println(x0);
                System.out.println(y0);
                canvas = holder.lockCanvas(new Rect(x0 - 1, y0 - 1, x0 + 1, y0 + 1));
                canvas.drawPoint(x0, y0, paint);
                System.out.println(dataY[i][0]);
                holder.unlockCanvasAndPost(canvas);  //解锁画布
            }
        }
        //dataNum > 1的情况
        else {
            x0 = X_OFFSET + (int) ((dataNum - 2) * DX);
            x1 = X_OFFSET + (int) ((dataNum - 1) * DX);
            canvas = holder.lockCanvas(new Rect(x0, 0, x1, HEIGHT));
            for (int i = 0; i < 4; i++) {
                switch(i) {
                    case 0:
                        paint.setColor(Color.YELLOW);
                        break;
                    case 1:
                        paint.setColor(Color.BLUE);
                        break;
                    case 2:
                        paint.setColor(Color.RED);
                        break;
                    case 3:
                        paint.setColor(Color.GREEN);
                        break;
                }

                System.out.println(DY);
                y0 = HEIGHT - (int) (dataY[i][dataNum - 2] * DY); //实时获取的temp数值，因为对于画布来说
                y1 = HEIGHT - (int) (dataY[i][dataNum - 1] * DY);
                System.out.println("i = " + i + "dataNum = " + dataNum + "x0: " + x0);
                System.out.println("i = " + i + "dataNum = " + dataNum + "y0: " + y0);
                System.out.println("i = " + i + "dataNum = " + dataNum + "x1: " + x1);
                System.out.println("i = " + i + "dataNum = " + dataNum + "y1: " + y1);
                canvas.drawPoint(x0, y0, paint);
                canvas.drawPoint(x1, y1, paint);
                canvas.drawLine(x0, y0, x1, y1, paint);
            }
            holder.unlockCanvasAndPost(canvas);  //解锁画布
        }
    }

    public class AlgoWrapper {
        public double showData[][];//4*10-->1*40
        public double nm[][];
        public double IWT[][];//40*50
        public double layer1[][];//1*50

        public double LWT[][];//50*10
        public double layer2[][];//1*10

        public int answer;

        public AlgoWrapper() {
            showData = new double[1][40];
            IWT = new double[40][50];
            layer1 = new double[1][50];
            LWT = new double[50][10];
            layer2 = new double[1][10];
            answer = 0;
            nm = new double[2][40];
            readW();
        }

        public int doCal() {
            getShowData();
            layer1 = matrixMult(showData, 1, 40, IWT, 40, 50);
            layer1 = sigmod(layer1, 50);
            for (int i = 0; i < 50; i++) {
                System.out.print(layer1[0][i]+" ");
                if ((i+1) %10 == 0)
                    System.out.println(" ");
            }
            layer2 = matrixMult(layer1, 1, 50, LWT, 50, 10);
            layer2 = sigmod(layer2, 10);
            for (int i = 0; i < 10; i++) {
                System.out.print(layer2[0][i]+" ");
                if ((i+1) %10 == 0)
                    System.out.println(" ");
            }

            int maxIndex = 0;
            for (int i = 1; i < 10; i++) {
                if (layer2[0][i] > layer2[0][maxIndex])
                    maxIndex = i;
            }
            //save showData
            for (int i = 0; i < 40; i++) {
                app.allShowData[app.allShowDataNum][i] = showData[0][i];
                System.out.print(showData[0][i] + " ");
                if ((i+1)%10 == 0)
                    System.out.println(" ");
            }
            app.answer[app.allShowDataNum++] = maxIndex;
            if (app.allShowDataNum >=100)
                app.allShowDataNum = 0;
            return maxIndex;
        }

        public void getShowData() {
            System.out.println("getshowdata");
            for (int i = 0; i < 4; i++) {
                //最大值最小值, 均值
                showData[0][10*i] = dataY[i][0];
                showData[0][10*i+1] = dataY[i][0];
                double sum = 0;
                for (int j = 0; j < DataSetSize; j++) {
                    sum += dataY[i][j];
                    if (dataY[i][j] > showData[0][10*i])
                        showData[0][10*i] = dataY[i][j];
                    if (dataY[i][j] < showData[0][10*i+1])
                        showData[0][10*i+1] = dataY[i][j];
                }
                //幅度
                showData[0][10*i+2] = showData[0][10*i] - showData[0][10*i+1];
                //均值
                showData[0][10*i+3] = sum/(double)DataSetSize;
                double ave = showData[0][10*i+3];
                //方差
                double DD = 0;
                for (int j = 0; j < DataSetSize; j++) {
                    double temp = ((double)dataY[i][j] - ave)*((double)dataY[i][j] - ave)/(double)DataSetSize;
                    DD += temp;
                }
                showData[0][10*i+4] = DD;
                //正脉冲因子
                showData[0][10*i+5] = showData[0][10*i]/ave;
                //负脉冲因子
                showData[0][10*i+6] = showData[0][10*i+1]/ave;
                //正峰值因子
                showData[0][10*i+7] = showData[0][10*i]/DD;
                //负峰值因子
                showData[0][10*i+8] = showData[0][10*i+1]/DD;
                //波形因子
                showData[0][10*i+9] = ave/DD;
            }
            for (int i = 0; i < 40; i++) {
                showData[0][i] = (showData[0][i] - nm[0][i])/(nm[1][i] - nm[0][i]);
            }
        }

        public double [][] matrixMult(double m1[][], int row1, int column1, double m2[][], int row2, int column2) {
            double [][] m = new double[row1][column2];
            for (int i = 0; i < row1; ++i)
                for (int j = 0; j < column2; ++j)
                    for (int k = 0; k < column1; ++k)
                        m[i][j] += m1[i][k] * m2[k][j];
            return m;
        }

        public void readW() {
            InputStream inputStreamTestY = getResources().openRawResource(R.raw.testdata);
            InputStream inputStreamIW = getResources().openRawResource(R.raw.iw);
            InputStream inputStreamLW = getResources().openRawResource(R.raw.lw);
            InputStream inputStreamNM = getResources().openRawResource(R.raw.normalization);

            reader(inputStreamIW, 0);
            reader(inputStreamLW, 1);
            reader(inputStreamTestY, 2);
            reader(inputStreamNM, 3);
        }

        public void reader(InputStream inputStream, int matrixIndex) {
            InputStreamReader inputStreamReader = null;
            inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuffer sb = new StringBuffer("");
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append(",");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(sb.toString());
            String[] strarray = null;
            strarray = sb.toString().split(",");

            switch(matrixIndex) {
                case 0://50*40的IW读入40*50的IWT
                    for (int i = 0; i < 50; i++) {
                        for (int j = 0; j < 40; j++) {
                            IWT[j][i] = Double.valueOf(strarray[i * 40 + j]);
                            System.out.print(IWT[j][i]+" ");
                        }
                        System.out.println("");
                    }
                    break;
                case 1:
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 50; j++) {
                            LWT[j][i] = Double.valueOf(strarray[i * 50 + j]);
                            System.out.print(LWT[j][i]+" ");
                        }
                        System.out.println("");
                    }
                    break;
                case 2:
                    for (int i = 0; i < 60; i++) {
                        for (int j = 0; j < 4; j++) {
                            dataY[j][i] = Integer.valueOf(strarray[i * 4 + j]);
                            System.out.print(dataY[j][i]+" ");
                        }
                        System.out.println("");
                    }
                    break;
                case 3:
                    for (int i = 0; i < 40; i++) {
                        for (int j = 0; j < 2; j++) {
                            nm[j][i] = Double.valueOf(strarray[i * 2 + j]);
                        }
                    }
                    break;
            }
        }

        public double [][] sigmod(double vector[][], int column) {
            for (int j = 0; j < column; j++)
                vector[0][j] = 1.0/(1+1.0/Math.exp(vector[0][j]));
            return vector;
        }
    }
    public void sendCmd(byte command, int value){
        Intent intent = new Intent();//创建Intent对象
        intent.setAction("android.intent.action.cmd");
        intent.putExtra("cmd", CMD_SEND_DATA);
        intent.putExtra("command", command);
        intent.putExtra("value", value);
        sendBroadcast(intent);//发送广播
    }
}
