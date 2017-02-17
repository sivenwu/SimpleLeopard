package cn.yuan.simple.leopard.utils;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by Yuan on 2016/11/1.
 * Detail api for retrofit
 */

public interface SupportApi {

    /**
     * 上传文件ing
     * @param url
     * @param maps
     * @return
     */

    @Multipart
    @POST("{path}")
    Observable<ResponseBody> uploadFile(
            @Path(value = "path", encoded = true) String url,
            @PartMap() Map<String, RequestBody> maps);


    @GET
    Observable<ResponseBody> downloadFile(
            @Url String fileUrl);


}
