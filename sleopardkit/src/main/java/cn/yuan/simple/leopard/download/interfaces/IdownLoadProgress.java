package cn.yuan.simple.leopard.download.interfaces;

/**
 * Created by Yuan on 2016/11/1.
 * Detail
 */

public interface IdownLoadProgress {

    public void onProgress(long progress, long total, boolean done);

    // 成功回调保存地址
    public void onSucess(String filePath);

    public void onFailed(Throwable e, String reason);

}
