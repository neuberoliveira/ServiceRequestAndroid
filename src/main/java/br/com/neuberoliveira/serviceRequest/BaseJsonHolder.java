package com.neuberdesigns.ServiceRequest;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * Created by neuber on 29/08/15.
 */
public abstract class BaseJsonHolder {
    private BaseJsonHolder instance;
    private Class instanceClass;

    public void fillFromJson(JSONObject json) throws JSONException {
        String property, propertyCamel;
        Iterator<?> keys = json.keys();

        while (keys.hasNext()) {
            boolean seted = false;
            property = (String) keys.next();
            propertyCamel = toCamelCase(property);

            try {
                seted = setField(propertyCamel, property, json);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            if (!seted) {
                try {
                    setField(property, property, json);
                } catch (NoSuchFieldException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private boolean setField(String property, String key, JSONObject json) throws NoSuchFieldException {
        boolean set = false;

        Field field;
        Class fieldType;
        Method callerJson;
        try {
            field = getSuperClass().getDeclaredField(property);
            field.setAccessible(true);
            fieldType = field.getType();
            callerJson = json.getClass().getMethod(buildGetMethod(fieldType.getName()), String.class);

            if (!json.isNull(key)) {
                field.set(getSuperInstance(), callerJson.invoke(json, key));
                set = true;
            }

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return set;
    }

    public BaseJsonHolder getSuperInstance() {
        return instance;
    }

    protected void setSuperInstance(BaseJsonHolder inst) {
        instance = inst;
        instanceClass = inst.getClass();
    }

    public Class getSuperClass() {
        return instanceClass;
    }

    protected String toCamelCase(String name) {
        String out = name;
        if (name != null && !name.isEmpty()) {
            out = name.toLowerCase().replaceAll("\\s+|_+", " ");
            String[] words = out.split(" ");

            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (i == 0) {
                    out = word;
                    continue;
                }


                char upCase = Character.toUpperCase(word.charAt(0));
                out += new StringBuilder(word.substring(1))
                        .insert(0, upCase)
                        .toString();
            }

            out = out.replaceAll(" ", "");
        }

        return out;
    }

    protected String buildMethodName(String typeName, boolean isGet) {
        int start = TextUtils.lastIndexOf(typeName, '.') + 1;
        String name = typeName.substring(start);
        String method = (isGet ? "get" : "set") + " " + name;

        return toCamelCase(method);
    }

    private String buildGetMethod(String typeName) {
        return buildMethodName(typeName, true);
    }

    private String buildSetMethod(String typeName) {
        return buildMethodName(typeName, false);
    }
}
