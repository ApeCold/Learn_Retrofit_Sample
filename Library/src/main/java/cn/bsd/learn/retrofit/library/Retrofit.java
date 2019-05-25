package cn.bsd.learn.retrofit.library;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class Retrofit {
    //缓存请求的方法
    //key:请求方法，如host.get() value: 该方法的属性封装，如：方法名、方法注解、参数注解、参数
    private final Map<Method,ServiceMethod> serviceMethodCache = new ConcurrentHashMap<>();

    //接口请求地址
    private HttpUrl baseUrl;

    //OKHttpClient
    private Call.Factory callFactory;
    private Retrofit(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.callFactory = builder.callFactory;
    }

    /**
     * The factory used to create {@linkplain okhttp3.Call OkHttp calls} for sending a HTTP requests.
     * Typically an instance of {@link OkHttpClient}.
     */
    public okhttp3.Call.Factory callFactory() {
        return callFactory;
    }

    /** The API base URL. */
    public HttpUrl baseUrl() {
        return baseUrl;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //动态代理和反射的最底层原理是：$Proxy4
                ServiceMethod serviceMethod = loadServiceMethod(method);
                return new OkHttpCall(serviceMethod,args);
            }
        });
    }

    //获取方法所有内容：方法名、方法注解、参数注解、参数
    ServiceMethod loadServiceMethod(Method method) {
        ServiceMethod result = serviceMethodCache.get(method);
        if (result != null) return result;

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(this, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    public static class Builder{
        //接口请求地址
        private HttpUrl baseUrl;

        //OKHttpClient
        private Call.Factory callFactory;

        public Builder callFactory(okhttp3.Call.Factory factory) {
            this.callFactory=callFactory;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }
            this.baseUrl = httpUrl;
            return this;
        }

        //属性的校验和初始化
        public Retrofit build(){
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            return new Retrofit(this);
        }
    }
}
