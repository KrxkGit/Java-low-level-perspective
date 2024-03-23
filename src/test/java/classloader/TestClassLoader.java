package classloader;

import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestClassLoader {
    class MyClassLoader extends ClassLoader {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            System.out.println("findClass");
            try {
                File file = new File(name);
                FileInputStream fileInputStream = new FileInputStream(file);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                fileInputStream.transferTo(byteArrayOutputStream);
                return defineClass("collection.linearList.vector.TestVector",
                        byteArrayOutputStream.toByteArray(),0,
                        byteArrayOutputStream.size());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Test
    public void test() {
        try {
            ClassLoader loader = new MyClassLoader();
            String className = "src/main/resources/TestVector-for-loader.class";
            Class clazz = loader.loadClass(className);
            Object obj = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getMethod("testVector");
            method.invoke(obj);

            // ClassLoader 实例不同，同名 Class 对象也不同
//            Class clazz1 = loader.loadClass(className); // 报错 attempted duplicate class definition
//            System.out.println(clazz == clazz1);

            ClassLoader loader1 = new MyClassLoader();
            Class clazz2 = loader1.loadClass(className);
            System.out.println(clazz == clazz2); // false 说明不同s
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    public void testContext() throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader();
//        System.out.println(loader);
//        System.out.println(String.class.getClassLoader());
//        System.out.println(DriverManager.class.getClassLoader());
        System.out.println(java.sql.Driver.class.getClassLoader());
//        System.out.println(java.util.ServiceLoader.class.getClassLoader());
//        System.out.println(java.util.ServiceLoader.class.getName());
        Class clazz = loader.loadClass(java.sql.Driver.class.getName());
        System.out.println(clazz);
        System.out.println(clazz.getClassLoader() == loader);
//        ServiceLoader
    }
}
