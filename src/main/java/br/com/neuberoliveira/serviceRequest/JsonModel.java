package br.com.neuberoliveira.serviceRequest;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * Created by neuber on 29/08/15.
 */
public abstract class JsonModel{
	public static final String LOG_ID = "jm_field_not_found"; 
	
	public void fromJson(Object jsonRaw) {
		JSONObject json = (JSONObject)jsonRaw;
		String property, propertyCamel;
		Iterator<?> keys = json.keys();

		while (keys.hasNext()) {
			boolean seted;
			property = (String) keys.next();
			propertyCamel = toCamelCase(property);
			seted = setField(propertyCamel, property, json);
			
			if(!seted && !property.equals(propertyCamel)) {
				setField(property, property, json);
			}
		}
	}

	private boolean setField(String property, String key, JSONObject json) {
		boolean set = false;

		Field field;
		Class fieldType;
		Method callerJson;
		try {
			field = this.getClass().getDeclaredField(property);
			field.setAccessible(true);
			fieldType = field.getType();
			callerJson = json.getClass().getMethod(buildGetMethod(fieldType.getName()), String.class);

			if (!json.isNull(key)) {
				field.set(this, callerJson.invoke(json, key));
				set = true;
			}
			
		} catch(NoSuchFieldException e){
			handleFieldNotFound(e, property, key);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return set;
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
	
	protected void handleFieldNotFound(NoSuchFieldException exception, String property, String key){
		Log.w(LOG_ID, "Cannot find property '"+property+"' for key '"+key);
	}
}
