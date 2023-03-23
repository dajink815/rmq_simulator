package com.uangel.reflection;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dajin kim
 */
@Slf4j
public class JarReflection {

    private final String jarPath;
    private URLClassLoader classLoader;

    public JarReflection(String jarPath) {
        this.jarPath = jarPath;
    }

    public boolean loadJarFile() {
        boolean result = false;
        File jarFile = new File(jarPath);
        try {
            URL classURL = new URL("jar:" + jarFile.toURI().toURL() + "!/");
            classLoader = new URLClassLoader(new URL[]{classURL});
            result = true;
            log.info("Jar File Loading Completed [{}]", jarPath);
        } catch (Exception e) {
            log.error("JarReflection.loadJarFile.Exception ", e);
        }
        return  result;
    }

    /**
     * @fn getClass
     * @brief load Class Type from jar
     * @param name 클래스 이름
     * */
    public Class<?> getClass(String name) {
        try {
            return classLoader.loadClass(name);
        } catch (Exception e) {
            log.error("JarReflection.getClass.Exception ", e);
        }
        return null;
    }

    /**
     * @fn getClassObject
     * @brief load Class from jar & Create Object
     * @param name 객체를 생성할 클래스 이름
     * */
    public Object getClassObject(String name) {
        try {
            Class<?> c = classLoader.loadClass(name);
            Constructor<?> constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            log.error("JarReflection.getClassObject.Exception ", e);
        }
        return null;
    }

    /**
     * @fn invokeMethod
     * @brief invoke method without Parameter
     * @param methodName 실행할 메서드 이름
     * @param obj 메서드가 있는 클래스 인스턴스
     *            클래스를 "new"로 생성하거나, "newInstance()"로 생성된 Object
     * */
    public Object invokeMethod(String methodName, Object obj) {
        try {
            // 매개 변수 없는 메소드 호출 및 실행
            Method method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);
        } catch (Exception e) {
            log.error("JarReflection.invokeMethod.Exception ", e);
        }
        return null;
    }
    public Object invokeMethod(String className, String methodName) {
        Object obj = getClassObject(className);
        return invokeMethod(methodName, obj);
    }

    /**
     * @fn invokeMethodWparam
     * @brief invoke method with Parameter
     * @param methodName 실행할 메서드 이름
     * @param obj 메서드가 있는 클래스의 객체
     * @param parameter 메서드 실행에 필요한 매개 변수
     * @param paramType 매개 변수의 타입
     * */
    private Object invokeMethodWparam(String methodName, Object obj, Object parameter, Class<?> paramType) {
        try {
            // paramType 매개 변수 있는 methodName 이름의 메소드 호출
            Method method = obj.getClass().getMethod(methodName, paramType);
            // 호출한 메소드 실행 with parameter
            return method.invoke(obj, parameter);
        } catch (InvocationTargetException e) {
            log.error("JarReflection.invokeMethodWparam.InvocationTargetException [Method:{}, ParamType:{}, Obj:\r\n{}]",
                    methodName, paramType.getName(), obj, e.getCause());
        } catch (Exception e) {
            log.error("JarReflection.invokeMethodWparam.Exception [Method:{}.{}, ParamType:{}, Obj:\r\n{}]",
                    obj.getClass().getName(), methodName, paramType.getName(), obj, e);
        }
        return null;
    }

    /**
     * @fn invokeStrMethod
     * @brief invoke method with String Parameter
     * @param methodName 실행할 메서드 이름
     * @param obj 메서드가 있는 클래스의 객체
     * @param parameter 메서드 실행에 필요한 String 타입 매개 변수
     * */
    public Object invokeStrMethod(String methodName, Object obj, String parameter) {
        // String 타입의 매개 변수 있는 methodName 이름의 메소드 호출, 실행
        return invokeMethodWparam(methodName, obj, parameter, String.class);
    }

    /**
     * @fn invokeIntMethod
     * @brief invoke method with int Parameter
     * @param methodName 실행할 메서드 이름
     * @param obj 메서드가 있는 클래스의 객체
     * @param parameter 메서드 실행에 필요한 int 타입 매개 변수
     * */
    public Object invokeIntMethod(String methodName, Object obj, int parameter) {
        // int 타입의 매개 변수 있는 methodName 이름의 메소드 호출
        // 호출한 메소드 실행 with int parameter
        return invokeMethodWparam(methodName, obj, parameter, int.class);
    }

    /**
     * @fn invokeLongMethod
     * @brief invoke method with long Parameter
     * @param methodName 실행할 메서드 이름
     * @param obj 메서드가 있는 클래스의 객체
     * @param parameter 메서드 실행에 필요한 long 타입 매개 변수
     * */
    public Object invokeLongMethod(String methodName, Object obj, long parameter) {
        return invokeMethodWparam(methodName, obj, parameter, long.class);
    }

    /**
     * @fn invokeBoolMethod
     * @brief invoke method with boolean Parameter
     * @param methodName 실행할 메서드 이름
     * @param obj 메서드가 있는 클래스의 객체
     * @param parameter 메서드 실행에 필요한 boolean 타입 매개 변수
     * */
    public Object invokeBoolMethod(String methodName, Object obj, boolean parameter) {
        return invokeMethodWparam(methodName, obj, parameter, boolean.class);
    }

    /**
     * @fn invokeByteMethod
     * @brief invoke method with Byte Array Parameter
     * @param className 메서드가 있는 클래스 이름
     * @param methodName 실행할 메서드 이름
     * @param parameter 메서드 실행에 필요한 Byte Array 타입(Object 로 전달) 매개 변수
     * */
    public Object invokeByteMethod(String className, String methodName, Object parameter) {
        Object obj = getClassObject(className);
        // byte[] 타입의 매개 변수 있는 methodName 이름의 메소드 호출
        return invokeMethodWparam(methodName, obj, parameter, byte[].class);
    }

    /**
     * @fn invokeObjMethod
     * @brief invoke method with Object Parameter
     * @param methodName 실행할 메서드 이름
     * @param obj 메서드가 있는 클래스의 객체
     * @param parameter 메서드 매개 변수
     * */
    public Object invokeObjMethod(String methodName, Object obj, Object parameter) {
        // parameter.class 타입의 매개 변수 있는 methodName 이름의 메소드 호출
        // 호출한 메소드 실행 with object parameter
        return invokeMethodWparam(methodName, obj, parameter, parameter.getClass());
    }


    public Object getNewBuilder(String className) {
        return invokeMethod(className, "newBuilder");
    }

    public byte[] toByteArray(Object msgObj) {
        Object msgByteObj = invokeMethod("toByteArray", msgObj);
        return (byte[]) msgByteObj;
    }

    public Object build(Object builderObj) {
        return invokeMethod("build", builderObj);
    }

    public Object getAllFields(Object msgObj) {
        return invokeMethod("getAllFields", msgObj);
    }

    public Object parseFrom(String className, byte[] bytes) {
        return invokeByteMethod(className, "parseFrom", bytes);
    }

    public String getSetterMethodName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public Map<String, String> getAllFieldsMap(Object msgObj) throws InvalidProtocolBufferException {
        Map<Descriptors.FieldDescriptor, Object> fields = (Map<Descriptors.FieldDescriptor, Object>) getAllFields(msgObj);

        Map<String, String> result = new HashMap<>();
        for (Object value : fields.values()) {
            Map<String, String> valueMap = ProtoUtil.parse(ProtoUtil.buildProto(value), HashMap.class);
            result.putAll(valueMap);
        }
        return result;
    }

}
