package com.androbotus.client.robot.common.modules.script;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.annotations.JSFunction;

import com.androbotus.client.contract.Topics;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.core.MessageBroker;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;

public class ScriptExecutor extends ScriptableObject {
	private static final long serialVersionUID = 438270592527335642L;
    
    private long methodCallsCount = 0;
    private ObjectMapper om = new ObjectMapper();
    
    private Context context;
    private Scriptable scope;
    private Logger logger;
    private Map<String, Object> values = new HashMap<String, Object>(); 
    private MessageBroker messageBroker;
    
    // The zero-argument constructor used by Rhino runtime to create instances
    public ScriptExecutor() { 

    }	    
    
    public void setContext(Context context) {
		this.context = context;
	}
    
    public void setScope(Scriptable scope) {
		this.scope = scope;
	}
    
    public void setLogger(Logger logger) {
		this.logger = logger;
	}
    
    public Logger getLogger() {
		return logger;
	}
    
    public void setMessageBroker(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
	}
    
    public Map<String, Object> getValuesMap(){
    	return values;
    }

    // The class name is defined by the getClassName method
    @Override
    public String getClassName() {
    	return ScriptExecutor.class.getSimpleName(); 
    }

    private void logMethodCall(String msg){
    	if (msg != null){
    		methodCallsCount++;
    		//System.out.println(String.format("Call #%s: %s", methodCallsCount, msg));
    	}
    }
        
    // The method getCount defines the count property.
    @JSFunction
    public Object getValues() {
    	logMethodCall("getValues()");
    	String sValues = "{}";
    	try {
    		sValues = om.writeValueAsString(values);
    	} catch (Exception e){
    		//do nothing
    	}
    	Object res = NativeJSON.parse(context, scope, sValues, nullCallable);
    	return res;
    }
    
    // The method getCount defines the count property.
    @JSFunction
    public Object getValue(String topic) {
    	logMethodCall("getValue(" + topic + ")");
    	String sValue = "{}";
    	Object value = values.get(topic);
    	if (value != null){
    		try {
    			sValue = om.writeValueAsString(value);
    		} catch (Exception e){
    			getLogger().log(LogType.ERROR, "Can't write object: " + value.toString());
    		}
    	}	
    	Object res = NativeJSON.parse(context, scope, sValue, nullCallable);
    	return res;
    }
    
    @Override
    public Object get(String name, Scriptable start) {
    	Object res = super.get(name, start);
    	if (res instanceof FunctionObject 
    			|| UniqueTag.NOT_FOUND.equals(res)
    			|| UniqueTag.NULL_VALUE.equals(res)){
    		return res;
    	}
    	return getValue(name);
    }
    
    @Override
    public void put(String name, Scriptable start, Object value) {    	
    	super.put(name, start, value);
    	if (value instanceof FunctionObject){
    		return;
    	}
    	sendControlMessage(name, value.toString());
    }
    
    /**
     * Maps given json object to {@link ControlMessage} and sends it to corresponding topic 
     * @param topic the topic to publish the message to
     * @param value the message json
     */
    @JSFunction
    public void sendMessage(String topic, Object value){
    	logMethodCall(String.format("sendMessage(%s,%s)", topic, value));
    	String sValue = (String)NativeJSON.stringify(context, scope, value, null, null);
    	ControlMessage v = null;
    	try {
    		v = om.readValue(sValue, ControlMessage.class);
    	} catch (Exception e){
    		getLogger().log(LogType.ERROR, "Can't read object: " + sValue);
    	}
    	if (v != null){
    		try {
    			messageBroker.pushMessage(topic, v);
    		} catch (Exception e){
    			getLogger().log(LogType.ERROR, "RhinoModule: Can't send message: " + e.getMessage());
    		}
    	}
    }
    
    /**
     * Maps given json object to {@link ControlMessage} and sends it to corresponding topic 
     * @param controlName the name of the control
     * @param value the new value for the control
     */
    @JSFunction
    public void sendControlMessage(String controlName, String value){
    	logMethodCall(String.format("sendControlMessage(%s,%s)", controlName, value));
    	ControlMessage cm = new ControlMessage();
    	cm.setControlName(controlName);
    	Float fv;
    	try {
    		fv = Float.valueOf(value);
    	} catch (NumberFormatException e){
    		getLogger().log(LogType.ERROR, "ScriptExecutor.sendControlMessage(). Illegal argument: " + e.getMessage());
    		return;
    	}
    	cm.setValue(fv);
    	try {
    		messageBroker.pushMessage(Topics.CONTROL.name(), cm);
    	} catch (Exception e){
    		getLogger().log(LogType.ERROR, "RhinoModule: Can't send control message: " + e.getMessage());
    	}
    }
    
    /**
     * Sleep execution for N millisec
     * @param timeout the integer number of millisec to sleep
     */
    @JSFunction
    public void sleep(String timeout){
    	Long t;
    	try {
    		t = Long.valueOf(timeout);
    	} catch (NumberFormatException e){
    		getLogger().log(LogType.ERROR, "ScriptExecutor.sleep(). Illegal argument: " + e.getMessage());
    		return;
    	}
    	try {
    		Thread.sleep(t);
    	} catch (InterruptedException e){
    		//do nothing
    	}
    }
    
    private final Callable nullCallable = new NullCallable();
    private class NullCallable implements Callable {
    	@Override
    	public Object call(Context cx, Scriptable scope, Scriptable thisObj,
    			Object[] args) {
    		return args[1];
    	}
    }
}
