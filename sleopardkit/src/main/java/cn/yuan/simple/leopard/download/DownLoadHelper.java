package cn.yuan.simple.leopard.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

import cn.yuan.simple.leopard.download.interfaces.IdownLoadProgress;
import cn.yuan.simple.leopard.download.model.DownLoadFileFactory;
import cn.yuan.simple.leopard.download.model.DownLoadResponseBody;
import cn.yuan.simple.leopard.download.model.DownloadInfo;
import cn.yuan.simple.leopard.utils.SupportApi;
import cn.yuan.simple.leopard.utils.Utils;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yuan on 2016/11/1.
 * Detail download for retrofit
 */

public class DownLoadHelper {

    private final String TAG = "DownLoadHelper";

    //listener
    private IdownLoadProgress idownLoadProgress;

    //http
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private SupportApi api;
    private Subscription bodySubscriber;

    //info
    private DownloadInfo downloadInfo;//下载信息
    public static final int DONWINFO_STAE_DOWNING = 0;//正在下载状态
    public static final int DONWINFO_STAE_STOP = 1;//停止下载状态

    //handler
    private final int HANDLER_CODE = 0X66;
    private final int HANDLER_DELAY = 100;
    private Handler handler  = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (idownLoadProgress!=null && msg.what == HANDLER_CODE)
                idownLoadProgress.onProgress(msg.arg1, msg.arg2,msg.arg1 >= msg.arg2);
        }
    };

    public DownLoadHelper() {
        downloadInfo = new DownloadInfo();
    }

    private void initClient(String url){
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(DownLoadFileFactory.create(new DownLoadResponseBody.DownLoadBodyListener() {
                    @Override
                    public void onProgress(long progress, long total, boolean done) {
                        handler.sendMessageDelayed(getMessage(progress,total),HANDLER_DELAY);
                    }
                },downloadInfo))
                .build();

        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(url)
                .build();

        api = retrofit.create(SupportApi.class);
    }

    /**
     * 下载入口
     * 只提供单一文件下载 and 不支持断点续传
     * @param url 下载链接
     * @param savePath 下载文件保存地址
     * @param fileName 下载文件名称
     * @param idownLoadProgress 下载状态回调
     */
    public void downLoad(String url, String savePath, String fileName, IdownLoadProgress idownLoadProgress){
        try {
            this.idownLoadProgress = idownLoadProgress;
            this.downloadInfo.setState(DONWINFO_STAE_DOWNING);
            this.downloadInfo.setFileName(fileName);
            this.downloadInfo.setSavePath(savePath);

            URL _url = new URL(url);
            if (_url.getPort() != -1) {
                initClient(_url.getProtocol() + "://" + _url.getHost() + ":" + _url.getPort() + "/");
            }else{
                initClient(_url.getProtocol() + "://" + _url.getHost()+ "/");
            }
            downLoading(url,idownLoadProgress);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载关闭入口
     */
    public void colseDownLoad(){
        this.downloadInfo.setState(DONWINFO_STAE_STOP);
        if (bodySubscriber != null && bodySubscriber.isUnsubscribed())
        bodySubscriber.unsubscribe();
    }

    private void downLoading(String url, final IdownLoadProgress idownLoadProgress){
        bodySubscriber =  api.downloadFile(url)
                .compose(cfgTransformer)
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {
                        if (downloadInfo.getState() != DONWINFO_STAE_STOP)
                            idownLoadProgress.onSucess(downloadInfo.getSavePath() + downloadInfo.getFileName());
                        colseDownLoad();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG,"[subscribe] "+"onError");
                        idownLoadProgress.onFailed(e,e.getMessage().toString());
                    }

                    @Override
                    public void onNext(ResponseBody o) {
                        if (downloadInfo.getState() != DONWINFO_STAE_STOP)
                        Utils.writeCache(downloadInfo,o.byteStream());
                    }
                });
    }

    private Message getMessage(long progress, long total){
        Message message = new Message();
        message.what = HANDLER_CODE;
        message.arg1 = (int) progress;
        message.arg2 = (int) total;
        return message;
    }

    private final Observable.Transformer cfgTransformer = new Observable.Transformer() {
        @Override
        public Object call(Object observable) {
            return ((Observable) observable)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    };


}
