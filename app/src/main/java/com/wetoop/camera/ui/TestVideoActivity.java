package com.wetoop.camera.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.wetoop.camera.tools.UlawEncoderInputStream;
import com.wetoop.cameras.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/7.
 */
public class TestVideoActivity extends Activity implements View.OnClickListener {
    private static AudioRecord mRecord;
    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 8000;// 44100;32000
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;// AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 音频大小
    private int bufSize;
    Boolean isRecord = false;
    boolean isLongClick = false;
    private ArrayList<byte[]> buffers = new ArrayList<byte[]>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_voice);
        Button speakButton = (Button) findViewById(R.id.btn_speak);
        Button back = (Button) findViewById(R.id.back);

        speakButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TestVideo.this.flag = "talk";
                //TestVideo.this.setImageButtonBackground();
                System.out.println("长按。。。。。。。。。。。");
                isRecord = true;
                isLongClick = true;
                bufSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig,
                        audioFormat);
                mRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig,
                        audioFormat, bufSize);
                readVideo();
                //开始录音
                return true;
            }
        });

        back.setOnClickListener(this);
        speakButton.setOnTouchListener(new MyClickListener());

    }

    private void readVideo() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //OutputStream os = null;
                String fd = getSDPath() + "/hei.pcm";
                File f = new File(fd);

                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                mRecord.startRecording();
                buffers.clear();
                byte audiodata[] = new byte[3200];
                int audioDataSize=0;
                int readsize = 0;
                while (isRecord) {
                    readsize = mRecord.read(audiodata, 0, 3200);
                    System.out.println("-------------->audiodata.length" + audiodata.length);
                        /*for (int i = 0; i < readsize; i++) {
                            //dout.writeShort(audiodata[i]);
                            //数据处理
                            System.out.println("-------------->进入"+i);
                        }*/
                    System.out.println("-------------->readsize" + readsize);
                    if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                        for(int j=0;j<5;j++){

                            byte[] data = new byte[640];
                            for (int i = 0; i < 640; i++) {
                                data[i]=audiodata[i+640*j];
                                //j++;
                            }
                            //System.arraycopy(audiodata, 0, data, 0, 640);
                            //buffers.add(data);
                            InputStream is = new ByteArrayInputStream(data);
                            //InputStream is = new ByteArrayInputStream(readData);
                            UlawEncoderInputStream uis = null;
                            try {
                                uis = new UlawEncoderInputStream(is, 0);
                                int len = uis.read(data);
                                System.out.println("-------------->len" + len);
                                while (len > 0) {
                                    fOut.write(data, 0, len);
                                    fOut.flush();
                                    len = uis.read(data);
                                    System.out.println("-------------->data" +len);
                                    //System.out.println("-------------->len" + len);
                                }
                            } catch (Exception e) {
                            } finally {
                                try {
                                    uis.close();
                                } catch (Exception e) {
                                }
                            }
                        }

                    }
                }
                mRecord.stop();
                audiodata = null;
            }
        });
        t.start();
    }


    public void onClick(View v) {    //OnClickListener中的要override的函数
        if (v.getId() == R.id.back) {
            Intent intent = new Intent(TestVideoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    class MyClickListener implements View.OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {

            if (isLongClick)
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:

                        System.out.println("抬起。。。。。。。。。。。。。。。。");
                        isRecord = false;
                        isLongClick = false;

                        break;
                    default:
                        break;
                }
            return false;
        }
    }

    //获取sd卡路径
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

}

