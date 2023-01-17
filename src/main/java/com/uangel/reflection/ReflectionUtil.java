package com.uangel.reflection;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.IntStream;

/**
 *
 * @author kangmoo Heo
 */
public class ReflectionUtil {
    
    public static void main(String[] args) throws Exception {
        //System.out.println(exec("java.time.LocalDateTime.now()"));
        //System.out.println(exec("java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss.SSS\")"));

        System.out.println(exec("java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss.SSS\"))"));
        System.out.println(exec("java.util.UUID.randomUUID().toString()"));
        System.out.println(exec("lib.java.handler.sip.Utils.getInstance()"));
        System.out.println(exec("lib.java.handler.sip.Utils.getInstance().generateCallIdentifier(\"127.0.0.1\")"));
    }

    public static TypeValuePair exec(String expression) throws Exception {
        expression = expression.trim();
        if (expression.endsWith(";")) expression = expression.substring(0, expression.length() - 1);
        Expression expr = StaticJavaParser.parseExpression(expression);
        if (expr.isObjectCreationExpr()) {
            return execObjectCreationExpr(expr.asObjectCreationExpr());
        } else if (expr.isMethodCallExpr()) {
            return execMethodCallExpr(expr.asMethodCallExpr());
        } else if (expr.isCastExpr()) {
            return execCastExpr(expr.asCastExpr());
        } else if (expr.isFieldAccessExpr()) {
            return execFieldAccessExpr(expr.asFieldAccessExpr());
        } else if (expr.isNameExpr()) {
            return new TypeValuePair(Class.forName(expr.toString()), null);
        } else if (expr.isNullLiteralExpr()) {
            return new TypeValuePair(null, null);
        } else if (expr.isDoubleLiteralExpr()) {
            return new TypeValuePair(double.class, expr.asDoubleLiteralExpr().asDouble());
        } else if (expr.isIntegerLiteralExpr()) {
            return new TypeValuePair(int.class, expr.asIntegerLiteralExpr().asNumber().intValue());
        } else if (expr.isLongLiteralExpr()) {
            return new TypeValuePair(long.class, expr.asLongLiteralExpr().asNumber().longValue());
        } else if (expr.isBooleanLiteralExpr()) {
            return new TypeValuePair(boolean.class, expr.asBooleanLiteralExpr().getValue());
        } else if (expr.isStringLiteralExpr()) {
            return new TypeValuePair(String.class, expr.asStringLiteralExpr().asString());
        }
        return null;
    }

    public static TypeValuePair execMethodCallExpr(MethodCallExpr methodCallExpr) throws Exception {
        Class<?> clazz = null;
        TypeValuePair object = null;
        if (methodCallExpr.hasScope()) {
            Expression superExpression = methodCallExpr.getScope().get();
            object = exec(superExpression.toString());
            clazz = object == null ? null : object.type;
        }

        TypeValuePair[] params = exec(methodCallExpr.getArguments());
        Class<?>[] paramTypes = null;
        Object[] paramValues = null;
        if (params != null) {
            paramTypes = new Class[params.length];
            paramValues = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].type;
                paramValues[i] = params[i].value;
            }
        }

        Method method = clazz.getMethod(methodCallExpr.getName().toString(), paramTypes);
        return new TypeValuePair(method.getReturnType(), method.invoke(object == null ? null : object.value, paramValues));
    }

    public static TypeValuePair execObjectCreationExpr(ObjectCreationExpr objectCreationExpr) throws Exception {
        Class<?> clazz = Class.forName(objectCreationExpr.getType().toString());
        TypeValuePair[] params = exec(objectCreationExpr.getArguments());
        Class<?>[] paramTypes = null;
        Object[] paramValues = null;
        if (params != null) {
            paramTypes = new Class[params.length];
            paramValues = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                paramTypes[i] = params[i].type;
                paramValues[i] = params[i].value;
            }
        }
        return new TypeValuePair(clazz, clazz.getDeclaredConstructor(paramTypes).newInstance(paramValues));
    }

    public static TypeValuePair execFieldAccessExpr(FieldAccessExpr fieldAccessExpr) throws Exception {
        try {
            return new TypeValuePair(Class.forName(fieldAccessExpr.toString()), null);
        } catch (Exception ignored){
        }
        TypeValuePair object;
        try {
            object = new TypeValuePair(Class.forName(fieldAccessExpr.getScope().toString()), null);
        } catch (Exception e2){
            object = exec(fieldAccessExpr.getScope().toString());
        }
        Field field = object.type.getDeclaredField(fieldAccessExpr.getNameAsString());
        field.setAccessible(true);
        return new TypeValuePair(field.getType(), field.get(object.value));
    }

    public static TypeValuePair execCastExpr(CastExpr castExpr) throws Exception {
        String type = castExpr.getType().toString();
        String value = castExpr.getExpression().toString();
        try {
            if (castExpr.getType().isPrimitiveType()) {
                switch (type) {
                    case "int":
                        return value.contains(".") ? new TypeValuePair(int.class, (int) Double.parseDouble(value)) : new TypeValuePair(int.class, Integer.parseInt(value));
                    case "long":
                        return value.contains(".") ? new TypeValuePair(long.class, (long) Double.parseDouble(value)) : new TypeValuePair(long.class, Long.parseLong(value));
                    case "short":
                        return value.contains(".") ? new TypeValuePair(short.class, (short) Double.parseDouble(value)) : new TypeValuePair(short.class, Short.parseShort(value));
                    case "byte":
                        return value.contains(".") ? new TypeValuePair(short.class, (short) Double.parseDouble(value)) : new TypeValuePair(byte.class, Byte.parseByte(value));
                    case "float":
                        return new TypeValuePair(float.class, Float.parseFloat(value));
                    case "double":
                        return new TypeValuePair(double.class, Double.parseDouble(value));
                    case "char":
                        return new TypeValuePair(char.class, value.charAt(0));
                    case "boolean":
                        return new TypeValuePair(boolean.class, Boolean.parseBoolean(value));
                    default:
                        break;
                }
            } else {
                Class<?> typeClass = Class.forName(castExpr.getType().toString());
                new TypeValuePair(typeClass, typeClass.cast(castExpr.getType().asTypeParameter().getName().toString()));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static TypeValuePair[] exec(List<Expression> expressions) throws Exception {
        if (expressions == null) return null;
        TypeValuePair[] res = new TypeValuePair[expressions.size()];
        IntStream.range(0, res.length).forEach(index -> {
            try {
                res[index] = exec(expressions.get(index).toString());
            } catch (Exception e) {
                res[index] = null;
            }
        });
        return res;
    }

    public static class TypeValuePair {
        public final Class<?> type;
        public final Object value;

        public TypeValuePair(Class<?> type, Object value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }


}
