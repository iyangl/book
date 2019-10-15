package com.lemay.android.book;

import com.lemay.android.book.inter.ProxyInterface;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * author: ly
 * date  : 2019/10/9 14:39
 * desc  :
 */
public class DynamicProxy {

    public static void main(String[] args) {
        try {
            test1();
            test2();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void test1() throws Exception {

        ProxyInterface proxyInterface = new ProxyInterface() {

            private String value = "proxyInterface";

            @NotNull
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public void setValue(@NotNull String value) {
                this.value = value;
            }
        };

        InvocationHandler handler = new ProxyInvocationHandler(proxyInterface);
        Class<?> proxyClass = Proxy.getProxyClass(ProxyInterface.class.getClassLoader(), ProxyInterface.class);
        ProxyInterface proxy = (ProxyInterface) proxyClass.getConstructor(InvocationHandler.class).newInstance(handler);
        // 调方法的时候执行动态代理中的 invoke
        proxy.setValue("value");
        System.out.println(proxy.getValue());
    }

    private static void test2() throws Exception {
        ProxyInterface proxyInterface = new ProxyInterface() {

            private String value = "proxyInterface";

            @NotNull
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public void setValue(@NotNull String value) {
                this.value = value;
            }
        };
        ProxyInterface proxy = (ProxyInterface) Proxy.getProxyClass(ProxyInterface.class.getClassLoader(), ProxyInterface.class)
                .getConstructor(InvocationHandler.class)
                .newInstance(new ProxyInvocationHandler(proxyInterface));
        System.out.println(proxy.getValue());
    }

    static class ProxyInvocationHandler implements InvocationHandler {

        private Object obj;

        ProxyInvocationHandler(Object obj) {
            this.obj = obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("这代理有个锤子用");
            return method.invoke(obj, args);
        }
    }


}
