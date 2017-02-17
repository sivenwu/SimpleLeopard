package cn.yuan.simple.leopard.download.model;

/**
 * Created by Yuan on 2016/8/29.
 * Detail 下载信息实体类
 */
public class DownloadInfo {

    private String savePath;
    private String fileName;
    private long progress;
    private long total;
    private int state; // 状态 0 下载状态 1停止状态

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
