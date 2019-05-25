package cn.bsd.learn.retrofit.library;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import cn.bsd.learn.retrofit.library.http.Field;
import cn.bsd.learn.retrofit.library.http.GET;
import cn.bsd.learn.retrofit.library.http.POST;
import cn.bsd.learn.retrofit.library.http.Query;
import okhttp3.Call;
import okhttp3.HttpUrl;

public class ServiceMethod {

    //OkHttpClient唯一实现接口
    private final Call.Factory callFactory;

    //接口请求地址
    private final HttpUrl baseUrl;
    //方法的请求方式("GET"、"POST")
    private final String httpMethod;
    //方法的注解的值("/ip/ipNew")
    private final String relativeUrl;
    //方法参数的数组（每个对象包含：参数注解值、参数值）
    private final ParameterHandler[] parameterHandlers;
    //是否有请求体(GET方式没有)
    private final boolean hasBody;

    private ServiceMethod(Builder builder) {
        this.callFactory = builder.retrofit.callFactory();
        this.baseUrl = builder.retrofit.baseUrl();
        this.httpMethod = builder.httpMethod;
        this.relativeUrl = builder.relativeUrl;
        this.parameterHandlers = builder.parameterHandlers;
        this.hasBody = builder.hasBody;
    }

    //发起请求
    public Call toCall(Object[] args) {
        //实例化RequestBuilde对象，拼接完整请求url（包含参数名和参数值）
        //http://www.163.com?id=1&name="fdsa"
        RequestBuilder requestBuilder = new RequestBuilder(httpMethod, baseUrl, relativeUrl,hasBody);

        ParameterHandler[] handlers = parameterHandlers;

        int argumentCount = args != null ? args.length : 0;
        //Proxy方法的参数个数是否等于参数的数组（手动添加）的长度，此处理解为校验
        if (argumentCount != handlers.length) {
            throw new IllegalArgumentException("Argument count (" + argumentCount
                    + ") doesn't match expected count (" + handlers.length + ")");
        }


        //循环拼接每个参数名和参数值
        for (int i = 0; i < argumentCount; i++) {
            handlers[i].apply(requestBuilder, args[i].toString());
        }

        return callFactory.newCall(requestBuilder.build());
    }

    public static class Builder {

        //封装构建
        private final Retrofit retrofit;
        //带注解的方法
        private final Method method;
        //方法的所有注解
        private final Annotation[] methodAnnotations;
        //方法参数的所有注解
        private final Annotation[][] parameterAnnotationsArray;
        //方法的请求方式（"GET"、"POST"）
        private String httpMethod;
        //方法的注解的值("/ip/ipNew")
        private String relativeUrl;
        //方法的参数的数组（每个对象包含：参数注解值、参数值）
        private ParameterHandler[] parameterHandlers;
        //是否有请求体(GET方式没有)
        private boolean hasBody;

        public Builder(Retrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            this.parameterAnnotationsArray = method.getParameterAnnotations();
        }

        public ServiceMethod build() {
            //遍历方法的每个注解（我们只需要GET或者POST注解）
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }


            //定义方法参数的数组长度
            int parameterCount = parameterAnnotationsArray.length;
            //初始化方法参数的数组
            parameterHandlers = new ParameterHandler[parameterCount];
            //遍历方法的参数
            for (int i = 0; i < parameterCount; i++) {
                //获取每个参数的所有注解
                Annotation[] parameterAnnotations = parameterAnnotationsArray[i];
                if (parameterAnnotations == null) {
                    throw new IllegalArgumentException("No Retrofit annotation found.");
                }

                parameterHandlers[i] = parseParameter(i, parameterAnnotations);
            }

            return new ServiceMethod(this);
        }

        private ParameterHandler parseParameter(int i, Annotation[] annotations) {
            ParameterHandler result = null;
            //遍历参数的注解，如：（@Query("ip") @Field("ip") String ip）
            for (Annotation annotation : annotations) {
                //注解可能是Query或者Field
                ParameterHandler annotationAction = parseParameterAnnotation(annotation);
                //找不到继续找，可不写（增强代码健壮）
                if (annotationAction == null) {
                    continue;
                }

                //赋值
                result = annotationAction;
            }

            //如果该参数没有任何注解抛出异常
            if (result == null) {
                throw new IllegalArgumentException("No Retrofit annotation found.");
            }

            return result;
        }

        //解析参数的注解，可能是Query或者Field
        private ParameterHandler parseParameterAnnotation(Annotation annotation) {
            //参考源码
            if(annotation instanceof Query){
                Query query = (Query) annotation;
                String name = query.value();
                //注意：传过去的参数是注解的值，并非参数值，参数值有Proxy方法传入
                return new ParameterHandler.Query(name);
            } else if(annotation instanceof Field){
                Field filed = (Field) annotation;
                String name = filed.value();
                //注意：传过去的参数是注解的值，并非参数值，参数值有Proxy方法传入
                return new ParameterHandler.Field(name);
            }
            return null;
        }

        //解析方法的注解，可能是GET或者POST
        private void parseMethodAnnotation(Annotation annotation) {

            if (annotation instanceof GET) {
                //注意：GET方式没有请求体RequestBody
                parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
            }
        }

        //通过方法的注解，获取方法的请求方式、方法的注解的值
        private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
            //请求方式
            this.httpMethod = httpMethod;
            //方法注解的值（"ip/ipNew"）
            this.relativeUrl = value;
            //是否有请求体
            this.hasBody = hasBody;
        }
    }
}
