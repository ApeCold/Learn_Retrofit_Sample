package cn.bsd.learn.retrofit.sample;

import org.junit.Test;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class RetrofitUnitTest {

    //聚合
    private final static String IP = "144.34.161.97";
    private final static String KEY = "aa205eeb45aa76c6afe3c52151b52160";
    private final static String BASE_URL = "http://apis.juhe.cn/";

    //查询IP接口
    interface HOST{
        @GET("/ip/ipNew")
        Call<ResponseBody> get(@Query("ip") String ip,@Query("key") String key);

        @POST("/ip/ipNew")
        @FormUrlEncoded
        Call<ResponseBody> post(@Field("ip") String ip, @Field("key") String key);
    }

    @Test
    public void retrofitTest() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();

        //GET同步请求
        {
            Request request = new Request.Builder()
                    .url(String.format("http://apis.juhe.cn/ip/ipNew?ip=%s&key=%s",IP,KEY))
                    .build();

            okhttp3.Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            if(response!=null&&response.body()!=null){
                System.out.println("OkHttp GET同步请求 >>> " +response.body().string());
            }
        }

        //POST同步请求
        {
            Request request = new Request.Builder()
                    .url("http://apis.juhe.cn/ip/ipNew")
                    .post(new FormBody.Builder()
                    .add("ip",IP)
                    .add("key",KEY)
                    .build())
                    .build();
            okhttp3.Call call = okHttpClient.newCall(request);
//            Response response = call.execute();
//            if(response!=null&&response.body()!=null){
//                System.out.println("OkHttp POST同步请求 >>> " +response.body().string());
//            }

            //异步请求（子线程）
            call.enqueue(new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    System.out.println("OkHttp POST异步请求 >>> "+e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if(response!=null&&response.body()!=null){
                        System.out.println("OkHttp POST异步请求 >>> " +response.body().string());
                    }
                }
            });
        }

        //--------------------------------------Retrofit2-------------------------------
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                //.callFactory(okHttpClient)//自己试下和配置
        .build();
        HOST host = retrofit.create(HOST.class);

        //Retrofit GET同步请求
        {
            Call<ResponseBody> call = host.get(IP,KEY);

            retrofit2.Response<ResponseBody> response = call.execute();
            if(response!=null&&response.body()!=null){
                System.out.println("Retrofit GET同步请求 >>> " +response.body().string());
            }
        }

        //Retrofit POST同步请求
        {
            Call<ResponseBody> call = host.post(IP,KEY);

            retrofit2.Response<ResponseBody> response = call.execute();
            if(response!=null&&response.body()!=null){
                System.out.println("Retrofit POST同步请求 >>> " +response.body().string());
            }
        }
    }
}
