# SimpleLeopard
The fuction come from Leopard.

### **SimpleLeopard** supportion:
1、Download
Supporting to download multiple files,but not have the fuction of "Resume broken downloads".
2、Upload
Supporting to Upoload files.

### My:
E-mail : sy.wu@foxmail.com

Blog : http://www.jianshu.com/users/d388bcf9c4d3/  

 **KeyWord**  retrofit,okhttp3,RxJava,download,upload...
 
### How to use it?
 
#### First 
 
``` java
 repositories {
    maven { url = 'https://dl.bintray.com/yuancloud/maven/' }
    ...
}

 compile 'cn.yuancloud.app:sleopardkit:1.0'
```
 
#### How to download some files?
  
``` java
  DownLoadHelper downLoadHelper = new DownLoadHelper();
  downLoadHelper.downLoad(url, savePath, fileName, new IdownLoadProgress() {
            @Override
            public void onProgress(long progress, long total, boolean done) {
                Log.d("yuan","progress: " + progress +"total : "+total);
                progressTv.setText("progress: " + progress +" total : "+total +" isDone? "+done);
            }

            @Override
            public void onSucess(String result) {
                Toast.makeText(DownLoadActivity.this,"download success !", Toast.LENGTH_SHORT).show();
                resultTv.setText("saving address："+result);
            }

            @Override
            public void onFailed(Throwable e, String reason) {
                resultTv.setText("donload filed! ："+reason);
            }
        });
```
 
#### How to upload some files?
    
``` java
    UploadHelper  uploadHelper = new UploadHelper();
    uploadHelper.upLoad(new FileUploadEnetity(url, fileList), new IProgress() {
            @Override
            public void onProgress(long progress, long total,int index,boolean done) {
                Log.i("yuan",progress + " "  + total +" "+done);
                progressTv.setText(" "+index+" file"+" \n： "+"progress: "+progress + " total: "+total +" isDone? " + done);
//                if (done){
//                    progressDialog.dismiss();
//                    Toast.makeText(UploadActivity.this,"All files was uploaded successed！！",Toast.LENGTH_SHORT).show();
//                }
            }

            @Overri
            public void onSucess(String result) {
                progressDialog.dismiss();
                resultTv.setText("onSucess\n\n"+result);
            }

            @Override
            public void onFailed(Throwable e, String reason) {
                progressDialog.dismiss();
                Toast.makeText(UploadActivity.this,reason, Toast.LENGTH_SHORT).show();
                resultTv.setText("onFailed\n\n"+reason);
            }
        });
```
   
#### Enjoy
