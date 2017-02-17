package com.yuan.leopard.sample.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yuan.leopard.sample.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.yuan.simple.leopard.upload.UploadHelper;
import cn.yuan.simple.leopard.upload.interfaces.IProgress;
import cn.yuan.simple.leopard.upload.model.FileUploadEnetity;
import cn.yuan.simple.leopard.upload.model.UploadModel;

public class UploadActivity extends AppCompatActivity implements UploadAdapter.IUploadGetPic{

    public static int RESULT_LOAD_IMAGE = 1000;

    private RecyclerView recyclerView;
    private UploadAdapter adapter;
    private TextView addressTv,progressTv,resultTv;
    List<UploadModel> data = new ArrayList<>();

    private Button uploadBtn;
    private ProgressDialog progressDialog;

    private UploadHelper uploadHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initView();
        initData();
        initListener();
        uploadHelper = new UploadHelper();
    }


    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.upload_recyclerview);
        uploadBtn = (Button) findViewById(R.id.upload_btn);
        addressTv = (TextView) findViewById(R.id.adress_tv);
        progressTv = (TextView) findViewById(R.id.progress_tv);
        resultTv = (TextView) findViewById(R.id.result_tv);

        adapter = new UploadAdapter(data, this, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

        //dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在上传中");
    }

    private void initListener(){
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.size() <=1){
                    Toast.makeText(UploadActivity.this,"未选择图片！", Toast.LENGTH_SHORT).show();
                    return ;
                }
                List<File> fileList = new ArrayList<File>();
                for (int i =0;i<data.size();i++){
                    if (i!=0){
                        fileList.add(data.get(i).getFile());
                    }
                }
                progressDialog.show();
                justUploading(fileList,"http://192.168.1.1:8090/fms/imagesUpload");
            }
        });
    }

    public void justUploading(List<File> fileList, String url){
        addressTv.setText("上传地址为： "+url);
        uploadHelper.upLoad(new FileUploadEnetity(url, fileList), new IProgress() {
            @Override
            public void onProgress(long progress, long total,int index,boolean done) {
                Log.i("yuan",progress + " "  + total +" "+done);
                progressTv.setText("正在上传 第 "+index+" 个文件"+" \n总进度： "+"progress: "+progress + " total: "+total +" isDone? " + done);
//                if (done){
//                    progressDialog.dismiss();
//                    Toast.makeText(UploadActivity.this,"所有图片上传成功！！",Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onSucess(String result) {
                progressDialog.dismiss();
                Toast.makeText(UploadActivity.this,"所有图片上传成功！！\n\n"+result, Toast.LENGTH_SHORT).show();
                resultTv.setText("onSucess\n\n"+result);
            }

            @Override
            public void onFailed(Throwable e, String reason) {
                progressDialog.dismiss();
                Toast.makeText(UploadActivity.this,reason, Toast.LENGTH_SHORT).show();
                resultTv.setText("onFailed\n\n"+reason);
            }
        });
    }

    private void initData() {
        if (data.size() <= 0)
            for (int i = 0; i < 1; i++) {
                File file = new File("");
                UploadModel model = new UploadModel(file);
                data.add(model);
            }

        adapter.notifyDataSetChanged();
    }

    private void addData(String filePath){
        File file = new File(filePath);
        UploadModel model = new UploadModel(file);
        data.add(model);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = this.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Log.i("yuan", "" + picturePath);
            addData(picturePath);
        }
    }

    @Override
    public void getPic () {
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

}
