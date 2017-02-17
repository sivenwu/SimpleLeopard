package cn.yuan.simple.leopard.upload;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import cn.yuan.simple.leopard.utils.SupportApi;
import cn.yuan.simple.leopard.upload.interfaces.IProgress;
import cn.yuan.simple.leopard.upload.model.FileUploadEnetity;
import cn.yuan.simple.leopard.upload.model.UploadFileRequestBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Yuan on 2016/11/1.
 * Detail upload for retrofit
 */

public class UploadHelper {

    private final String TAG = "UploadHelper";
    private String locatUrl = "http://121.8.131.228:8090";

    //listener
    private IProgress iProgressLienter;

    //http
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private SupportApi api;
    private Subscription bodySubscriber;
    private MediaType dataMediaType = MediaType.parse("multipart/form-data");

    //control value
    private long curUploadProgress = 0;
    private int fileIndex = 1;//上传文件位置

    //handler for mainThread
    private final int HANDLER_CODE = 0X66;
    private final int HANDLER_DELAY = 100;
    private Handler handler  = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (iProgressLienter!=null && msg.what == HANDLER_CODE)
                iProgressLienter.onProgress(msg.arg1, msg.arg2, fileIndex,msg.arg1 >= msg.arg2);
        }
    };

    private final Observable.Transformer cfgTransformer = new Observable.Transformer() {
        @Override
        public Object call(Object observable) {
            return ((Observable) observable)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    };

    public UploadHelper() {// do no anything
    }

    private void initClient(String url){
        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .build();

        api = retrofit.create(SupportApi.class);
    }

    /**
     * upload 入口
     * @param enetity 上传包装类
     * @param callback 上传状态回调
     */
    public void upLoad(final FileUploadEnetity enetity, final IProgress callback){
        try {
            URL url = url = new URL(enetity.getUrl());
            Observable
                    .just(url)
                    .map(new Func1<URL, Integer>() {
                        @Override
                        public Integer call(URL url) {
                            return fiterUpload(url,callback);
                        }
                    })
                    .compose(cfgTransformer)
                    .onBackpressureBuffer()
                    .subscribe(new Action1<Integer>() {
                        @Override
                        public void call(Integer integer) {
                            if (integer != 404){
                                justUpLoad(enetity,callback);
                            }else{
                                callback.onFailed(null,"HTTP 404 not found!");
                            }
                        }
                    });
        } catch (MalformedURLException e) {
            callback.onFailed(e,e.getMessage().toString());
            e.printStackTrace();
        }
    }

    public void closeUpload(){
        resetValue();
        if (bodySubscriber != null && !bodySubscriber.isUnsubscribed()){
            bodySubscriber.unsubscribe();
        }
    }

    private Message getMessage(long progress, long total){
        Message message = new Message();
        message.what = HANDLER_CODE;
        message.arg1 = (int) progress;
        message.arg2 = (int) total;
        return message;
    }

    /**
     * method for close
     */
    private void resetValue(){
        this.curUploadProgress = 0;
        this.fileIndex = 1;
    }

    private void justUpLoad(final FileUploadEnetity enetity, final IProgress callback){
        try {
            this.iProgressLienter = callback;
            URL url = new URL(enetity.getUrl());

            if (url.getPort() != -1) {
                initClient(url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/");
            }else{
                initClient(url.getProtocol() + "://" + url.getHost()+ "/");
            }
            resetValue();

            HashMap<String, RequestBody> params  = displayFile(enetity);
            if (params!=null)
                uploading(url.getPath(),params);
        } catch (MalformedURLException e) {
            this.iProgressLienter.onFailed(e,e.getMessage().toString());
            e.printStackTrace();
        }
    }

    private void uploading(String url, HashMap<String, RequestBody> params){
        bodySubscriber =  api.uploadFile(url,params)
                .compose(cfgTransformer)
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() { // do no any things
                        closeUpload();//完成关闭
                    }

                    @Override
                    public void onError(Throwable e) { // do no any things
                        iProgressLienter.onFailed(e,e.getMessage().toString());
                        closeUpload();//失败关闭
                        Log.e(TAG,"[Subscriber] "+"onError: "+e.getMessage().toString());
                    }

                    @Override
                    public void onNext(ResponseBody o) { // do no any things
                        try {
                            String result = new String(o.bytes(),"utf-8");
                            iProgressLienter.onSucess(result);
                            Log.d(TAG,"[Subscriber] "+"onNext: "+result);
                        } catch (IOException e) {
                            e.printStackTrace();
                            iProgressLienter.onFailed(e,e.getMessage().toString());
                        }
                    }
                });
    }

    // 过滤状态码
    private int fiterUpload(URL url, IProgress callback){
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            return conn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailed(e,e.getMessage().toString());
            return 404;
        }
    }

    /**
     * to config the parameters of request
     * @param enetity
     * @return
     */
    private HashMap<String, RequestBody> displayFile(final FileUploadEnetity enetity){
        HashMap<String, RequestBody> params = null;
        final List<File> files = enetity.getFiles();

        if (files.size() <= 0){
            Log.d(TAG,"[displayFile] "+"upload no found file!");
            return null;
        }

        params = new HashMap<>();
        for (int i = 0; i < files.size(); i++) {
            final File file = files.get(i);
            RequestBody body =
                    RequestBody.create(dataMediaType, file);

            UploadFileRequestBody body_up = new UploadFileRequestBody(body, new UploadFileRequestBody.UploadBodyListener() {
                @Override
                public void onProgress(long progress, long total, boolean done) {//每次done表示一个文件完成
                    if (done) {
                        fileIndex++;
                        if (fileIndex <= files.size()) {
                            curUploadProgress += total;
                        }else{
                            fileIndex --;//为了防止最后一次done自加了
                        }
                    }
                    handler.sendMessageDelayed(getMessage(curUploadProgress + (progress),enetity.getFilesTotalSize()),HANDLER_DELAY);
                }
            });
            params.put("file[]\"; filename=\"" + file.getName(), body_up);
        }
        return params;
    }
}
