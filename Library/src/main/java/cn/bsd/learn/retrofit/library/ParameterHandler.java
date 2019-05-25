package cn.bsd.learn.retrofit.library;

import android.support.annotation.Nullable;

/**
 * (@Field("xxx") String yyy)
 * 保存参数的注解值、参数值，用于拼接请求
 */
abstract class ParameterHandler {
    abstract void apply(RequestBuilder builder, @Nullable String value);

    static final class Query extends ParameterHandler {
        private final String name;

        //注意：传过来的是注解的值，并非参数值
        public Query(String name) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name == null");
            }
            this.name = name;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable String value){
            //拼接Query参数，此处的name为参数的注解值，value为参数值
            builder.addQueryParam(name,value);
        }
    }

    static final class Field extends ParameterHandler {
        private final String name;

        //注意：传过来的是注解的值，并非参数值
        Field(String name) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name == null");
            }
            this.name = name;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable String value){
            //拼接Field参数，此处的name为参数注解的值，value为参数值
            builder.addFieldParam(name,value);
        }
    }
}
