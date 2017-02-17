package cn.yuan.simple.leopard.download.model;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Yuan on 2016/11/1.
 * Detail 下载拦截器
 */

public class DownLoadFileFactory implements Interceptor {

    private DownLoadResponseBody.DownLoadBodyListener bodyListener;
    private DownloadInfo downloadInfo;

    public DownLoadFileFactory(DownLoadResponseBody.DownLoadBodyListener bodyListener, DownloadInfo downloadInfo) {
        this.bodyListener = bodyListener;
        this.downloadInfo = downloadInfo;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder().addHeader("RANGE", "bytes=" + 0 + "-").build();//直接下载完整
        Response originalResponse = chain.proceed(request);
        downloadInfo.setTotal(originalResponse.body().contentLength());
        DownLoadResponseBody body = new DownLoadResponseBody(bodyListener,this.downloadInfo ,originalResponse.body());
        Response response = originalResponse.newBuilder().body(body).build();
        return response;
    }


    public static DownLoadFileFactory create(DownLoadResponseBody.DownLoadBodyListener bodyListener, DownloadInfo downloadInfo) {
        return new DownLoadFileFactory(bodyListener,downloadInfo);
    }

}
