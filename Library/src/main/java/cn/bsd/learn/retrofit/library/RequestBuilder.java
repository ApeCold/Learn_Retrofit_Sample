package cn.bsd.learn.retrofit.library;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RequestBuilder {
    //方法的请求方式（"GET","POST"）
    private final String method;
    //接口请求地址
    private final HttpUrl baseUrl;
    //方法的注解的值("/ip/ipNew")
    private String relativeUrl;
    //请求url的构建者（构建完整请求url）
    private HttpUrl.Builder urlBuilder;
    //Form表单构建者
    private FormBody.Builder formBuilder;
    //构建完整请求(包含url、method、body)
    private final Request.Builder requestBuilder;

    public RequestBuilder(String method, HttpUrl baseUrl, String relativeUrl, boolean hasBody) {
        this.method = method;
        this.baseUrl = baseUrl;
        this.relativeUrl = relativeUrl;

        //初始化请求
        requestBuilder = new Request.Builder();
        //根据是否有请求体实例化Form表单构建者
        if (hasBody) formBuilder = new FormBody.Builder();
    }

    public void addQueryParam(String name, String value) {
        if (relativeUrl != null) {
            // Do a one-time combination of the built relative URL and the base URL.
            urlBuilder = baseUrl.newBuilder(relativeUrl);
            if (urlBuilder == null) {
                throw new IllegalArgumentException(
                        "Malformed URL. Base: " + baseUrl + ", Relative: " + relativeUrl);
            }
            relativeUrl = null;
        }
        urlBuilder.addQueryParameter(name, value);
    }

    public void addFieldParam(String name, String value) {
        formBuilder.add(name, value);
    }

    public Request build() {
        HttpUrl url;
        if (urlBuilder != null) {
            url = urlBuilder.build();
        } else {
            url = baseUrl.resolve(relativeUrl);
            if (url == null) {
                throw new IllegalArgumentException(
                        "Malformed URL. Base: " + baseUrl + ", Relative: " + relativeUrl);
            }
        }

        //如果有请求体，构造方法中会初始化Form表单构建者，然后再实例化请求体
        RequestBody body = null;
        if (formBuilder != null) {
            if (formBuilder != null) {
                body = formBuilder.build();
            }
        }
        return requestBuilder
                .url(url)
                .method(method, body)
                .build();
    }
}
