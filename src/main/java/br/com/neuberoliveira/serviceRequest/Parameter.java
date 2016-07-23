package br.com.neuberoliveira.serviceRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neuber on 22/07/16.
 */
public class Parameter{
	protected String name;
	protected Object value;
	protected List<Object> values = new ArrayList<>();
	
	private Parameter(){}
	
	public static Parameter create(String name, List<Object> values){
		Parameter instance = new Parameter();
		instance.setName(name);
		instance.setValue(values);
		
		return instance;
	}
	
	protected static Parameter createParam(String name, Object value){
		Parameter instance = new Parameter();
		instance.setName(name);
		instance.setValue(value);
		
		return instance;
	}
	
	public static Parameter create(String name, String value){
		return createParam(name, value);
	}
	
	public static Parameter create(String name, int value){
		return createParam(name, value);
	}
	
	public static Parameter create(String name, double value){
		return createParam(name, value);
	}
	
	public static Parameter create(String name, boolean value){
		return createParam(name, value);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public Object getValue(){
		return value;
	}
	
	public List<Object> getValues(){
		return values;
	}
	
	public void setValue(Object value){
		this.value = value;
	}
	
	public void setValue(List<Object> values){
		this.values = values;
	}
	
	public boolean isMultiValue(){
		return !values.isEmpty();
	}
}
