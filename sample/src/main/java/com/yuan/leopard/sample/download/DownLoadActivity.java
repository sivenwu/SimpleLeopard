package com.yuan.leopard.sample.download;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yuan.leopard.sample.R;

import cn.yuan.simple.leopard.download.DownLoadHelper;
import cn.yuan.simple.leopard.download.interfaces.IdownLoadProgress;


public class DownLoadActivity extends AppCompatActivity {

    private Button downloadBtn,closeBtn;
    private TextView addressTv,progressTv,resultTv;

    private DownLoadHelper downLoadHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);

        initView();
        initListener();
    }

    private void initView(){
        addressTv = (TextView) findViewById(R.id.address_tv);
        progressTv = (TextView) findViewById(R.id.progress_tv);
        resultTv = (TextView) findViewById(R.id.result_tv);
        downloadBtn = (Button) findViewById(R.id.download_btn);
        closeBtn = (Button) findViewById(R.id.close_download);
    }

    private void initListener(){
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                justDownLoading("http://a5.pc6.com/pc6_soure/2016-3/cn.wsy.travel.apk"
                        , Environment.getExternalStorageDirectory() +"/yuan/"
                        ,"iRecord.apk");
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressTv.setText("--");
                resultTv.setText("Stop Download");
                downLoadHelper.colseDownLoad();
            }
        });
    }

    public void justDownLoading(String url, String savePath, String fileName){
        addressTv.setText("下载地址：\n"+url);
        resultTv.setText("Start Download");

        downLoadHelper = new DownLoadHelper();
        downLoadHelper.downLoad(url, savePath, fileName, new IdownLoadProgress() {
            @Override
            public void onProgress(long progress, long total, boolean done) {
                Log.d("yuan","progress: " + progress +"total : "+total);
                progressTv.setText("progress: " + progress +" total : "+total +" isDone? "+done);
            }

            @Override
            public void onSucess(String result) {
                Toast.makeText(DownLoadActivity.this,"保存成功！！保存地址请看视图！", Toast.LENGTH_SHORT).show();
                resultTv.setText("保存地址："+result);
            }

            @Override
            public void onFailed(Throwable e, String reason) {
                resultTv.setText("下载失败："+reason);
            }
        });
    }
}
