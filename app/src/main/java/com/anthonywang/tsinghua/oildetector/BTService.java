package com.anthonywang.tsinghua.oildetector;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BTService extends Service {

    /**************service 命令*********/
    static final int CMD_START_BLUETOOTH = 0x00;
    static final int CMD_STOP_SERVICE = 0x01;
    static final int CMD_SEND_DATA = 0x02;
    static final int CMD_SYSTEM_EXIT =0x03;
    static final int CMD_SHOW_TOAST =0x04;

    CommandReceiver cmdReceiver;
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄

    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号

    private InputStream is;    //输入流，用来接收蓝牙数据
    //private TextView text0;    //提示栏解句柄
//    private EditText edit0;    //发送数据输入句柄
//    private TextView dis;       //接收数据显示句柄
//    private ScrollView sv;      //翻页句柄
    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存
    private String tempmsg = "";
    public byte[] sendBuf;
    byte[] buffer;
    byte[] buffer_new;
    public String filename=""; //用来保存存储的文件名
    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    boolean _discoveryFinished = false;
    boolean bRun = false;
    boolean bThread = false;
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        cmdReceiver = new CommandReceiver();
        IntentFilter filter = new IntentFilter();//创建IntentFilter对象
        //注册一个广播，用于接收Activity传送过来的命令，控制Service的行为，如：发送数据，停止服务等
        filter.addAction("android.intent.action.cmd");
        //注册Broadcast Receiver
        registerReceiver(cmdReceiver, filter);
        System.out.println("BTservice started");
        // 设置设备可以被搜索
        new Thread(){
            public void run(){
                if(_bluetooth.isEnabled()==false){
                    _bluetooth.enable();
                }
            }
        }.start();

        buffer = new byte[1024];
        buffer_new = new byte[1024];

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("android.intent.action.cmd")){
                int cmd = intent.getIntExtra("cmd", -1);//获取Extra信息
                switch(cmd) {
                    case CMD_START_BLUETOOTH:
                        System.out.println("started");
                        String address = intent.getStringExtra("address");
                        System.out.println(address);
                        // 得到蓝牙设备句柄
                        _device = _bluetooth.getRemoteDevice(address);

                        // 用服务号得到socket
                        try{
                            _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                        }catch(IOException e){
//                            Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                        }
                        //连接socket
//                        Button btn = (Button) findViewById(R.id.Button03);
                        try{
                            _socket.connect();
                            System.out.println("连接"+_device.getName()+"成功");
//                            Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
//                            btn.setText("断开");
                        }catch(IOException e){
                            try{
//                                Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                                _socket.close();
                                _socket = null;
                            }catch(IOException ee){
//                                Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        //打开接收线程
                        try{
                            is = _socket.getInputStream();   //得到蓝牙数据输入流
                        }catch(IOException e){
//                            Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(bThread==false){
                            ReadThread.start();
                            System.out.println("read thread is running");
                            bThread=true;
                        }else{
                            bRun = true;
                        }
                        break;
                }
            }
        }
    }

    //接收数据线程
    Thread ReadThread=new Thread(){
        public void run(){
            boolean receiving = false;
            boolean check1 = false;
            int i = 0;
            int n = 0;
            bRun = true;
            int num = 0;
            //接收线程
            while(true){
                try{
                    while(is.available()==0){
                        while(bRun == false){}
                    }
                    while(true){
                        num = is.read(buffer);         //读入数据
                        System.out.println(num);
                        n=0;
                        //——————<按照协议分隔数据
//                        System.out.println( Integer.toString( ( buffer[0] & 0xff ), 16));
//                        if (receiving) {
//                            buffer_new[reNum++] = buffer[0];
//                        }
//                        if (buffer[0] == 0x7c) {
//                            receiving = true;
//                            buffer_new[0] = 0x7c;
//                            reNum++;
//                        }
//                        if (buffer[0] == (byte)0xaa) {
//                            check1 = true;
//                        }
//                        if (buffer[0] == 0x0d && receiving && check1) {
//                            checkRight = check();
//                            System.out.println(checkRight);
//                            handler.sendMessage(handler.obtainMessage());   //刷新界面
//                            receiving = false;
//                             check1 = false;
//                            reNum = 0;
//                            checkRight = false;
//                        }
                        //——————按照协议分隔数据>
                        String s0 = new String(buffer,0,num);
                        fmsg+=s0;    //保存收到数据
                        for(i=0;i<num;i++){
                            if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
                                buffer_new[n] = 0x0a;
                                i++;
                            }else{
                                buffer_new[n] = buffer[i];
                            }
                            n++;
                        }
                        String s = new String(buffer_new,0,n);
                        smsg+=s;   //写入接收缓存

                        if(is.available()==0) {
                            try {
                                sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (is.available() == 0)
                                break;
                        }
                    }
                    System.out.println("haha");
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.back");
                    intent.putExtra("cmd", CMD_SEND_DATA);
                    intent.putExtra("msg", smsg);
                    smsg = "";
                    sendBroadcast(intent);
//                    handler.sendMessage(handler.obtainMessage());
                    //发送显示消息，进行显示刷新
                }catch(IOException e){
                }
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            _socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _socket = null;
        super.onDestroy();
        System.out.println("service destroyed");
        if (cmdReceiver != null)
            this.unregisterReceiver(cmdReceiver);//取消注册的CommandReceiver
    }
}
