package cn.yuan.simple.leopard.download.model;


import java.io.IOException;

import cn.yuan.simple.leopard.download.DownLoadHelper;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by Yuan on 2016/8/25.
 * Detail DownLoadResponseBody for retrofit
 */
public class DownLoadResponseBody extends ResponseBody {

    private DownLoadBodyListener bodyListener;

    private DownloadInfo downloadInfo;
    private ResponseBody mResponseBody;
    private BufferedSource bufferedSource;

    public DownLoadResponseBody(DownLoadBodyListener bodyListener, DownloadInfo downloadInfo, ResponseBody mResponseBody) {
        this.bodyListener = bodyListener;
        this.downloadInfo = downloadInfo;
        this.mResponseBody = mResponseBody;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return bufferedSource;
    }

    public Source source(Source source) {
        return new ForwardingSource(source) {

            //当前读取字节数
            long bytesRead = 0L;
            //总字节长度
            long totalLength = 0L;

            int i = 0;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {

                if (downloadInfo.getState() == DownLoadHelper.DONWINFO_STAE_STOP){
                    bytesRead = 0;
                    return bytesRead;
                }

                try {
                    bytesRead = super.read(sink, byteCount);
                } catch (Exception e){
                    e.printStackTrace();
                }
                if (bytesRead != -1) {

                } else {
                    bytesRead = 0;
                }

                totalLength += bytesRead;
                downloadInfo.setProgress(downloadInfo.getProgress() + bytesRead);//实时更新downloadinfo的进度

                long progress = downloadInfo.getProgress();
                long total = downloadInfo.getTotal();
                postMainThread(progress,total);
                return bytesRead;
            }
        };
    }

    private void postMainThread(long progress,long total){
        this.bodyListener.onProgress(progress,total,progress>=total);
    }

    // body内部回调
    public interface DownLoadBodyListener{
        public void onProgress(long progress, long total, boolean done);
    }
}
