package com.androbotus.client.robot.common.modules.script;

import java.nio.charset.Charset;

import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.map.ObjectMapper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import android.util.Base64;

import com.androbotus.client.contract.Sensors;
import com.androbotus.mq2.contract.ControlMessage;
import com.androbotus.mq2.contract.Message;
import com.androbotus.mq2.contract.ScriptControlMessage;
import com.androbotus.mq2.contract.SensorMessage;
import com.androbotus.mq2.log.Logger;
import com.androbotus.mq2.log.Logger.LogType;
import com.androbotus.mq2.module.AsyncModule;

/**
 * <p>
 * This is a "scripting support" for the robot. This module provides simple
 * javascript interface. It allows to write scripts via publishing messages to
 * topics and listening for other topics
 * </p>
 * <p>
 * It supports 2 control commands:
 * <li>Start script execution</li>
 * <li>Stop script execution</li>
 * </p>
 * 
 * @author maximlukichev
 * 
 */
public class RhinoModule extends AsyncModule {
	private Thread scriptThread;
	private MyContext cx;
	private Scriptable scope;
	private ScriptExecutor executor;

	private final static String ORIENTATION_X = "ORIENTATION_X";
	private final static String ORIENTATION_Y = "ORIENTATION_Y";
	private final static String ORIENTATION_Z = "ORIENTATION_Z";
	private final static String GYRO_X = "GYRO_X";
	private final static String GYRO_Y = "GYRO_Y";
	private final static String GYRO_Z = "GYRO_Z";

	public RhinoModule(Logger logger) {
		super(logger);
		ContextFactory.initGlobal(new MyFactory());
	}

	private long lastUpdatedOrientation = System.currentTimeMillis();
	private long lastUpdatedGyro = System.currentTimeMillis();

	private void processSensorMessage(SensorMessage message) {
		if (executor != null) {
			// receive new sensor data
			if (Sensors.ROTATION_ANGLE.getCode() == message.getSensorCode()
					&& expired(lastUpdatedOrientation)) {
				// roll
				executor.getValuesMap().put(ORIENTATION_X, message.getxValue());
				executor.getValuesMap().put(ORIENTATION_Y, message.getyValue());
				executor.getValuesMap().put(ORIENTATION_Z, message.getzValue());
				lastUpdatedOrientation = System.currentTimeMillis();
			} else if (Sensors.GYRO.getCode() == message.getSensorCode()
					&& expired(lastUpdatedGyro)) {
				executor.getValuesMap().put(GYRO_X, message.getxValue());
				executor.getValuesMap().put(GYRO_Y, message.getyValue());
				executor.getValuesMap().put(GYRO_Z, message.getzValue());
				lastUpdatedGyro = System.currentTimeMillis();
			}
		}
	}

	private boolean expired(long time) {
		return System.currentTimeMillis() - time > 1000;
	}

	private void processScriptControlMessage(ScriptControlMessage message) {
		String controlName = message.getControlName();

		if (!controlName.equals("SCRIPT")) {
			return;
		}
		float value = message.getValue();
		String encodedScript = message.getScript();
		String script = encodedScript;//new ObjectMapper().convertValue(encodedScript, String.class);;
		if (value > 0) {
			// starting new script
			stopCurrentScript();
			if (script == null || script.length() == 0) {
				resumeCurrentScript();
				getLogger().log(LogType.DEBUG,
						String.format("Resuming current script"));
			} else {
				startNewScript(script);
				getLogger().log(LogType.DEBUG,
						String.format("Starting new script"));
			}
		} else if (value == 0) {
			pauseCurrentScript();
			getLogger().log(LogType.DEBUG,
					String.format("Pausing current script"));
		} else if (value < 0) {
			stopCurrentScript();
			getLogger().log(LogType.DEBUG,
					String.format("Stopping current script"));
		}

	}

	private void processControlMessage(ControlMessage message) {
		// TODO:
	}

	@Override
	protected void processMessage(Message message) {
		if (message instanceof ScriptControlMessage) {
			processScriptControlMessage((ScriptControlMessage) message);
		} else if (message instanceof ControlMessage) {
			processControlMessage((ControlMessage) message);
		} else if (message instanceof SensorMessage) {
			processSensorMessage((SensorMessage) message);
		} else {
			// TODO: process other messages
		}
	}

	protected void tearDown() {
		// Ideally there should be a logic how to tear down the current scipt.
		// I.e. if robot is flying this
		// function shouldn't just shut off engines, but land the device safely
		// and then shut off
	}

	private void stopCurrentScript() {
		if (cx != null){
			cx.stopped = true;
		}
		if (scriptThread != null) {
			scriptThread.interrupt();
		}
		try {
			Context.exit();
		} catch (IllegalStateException e) {
			// do nothing
		}
		cx = null;
		scope = null;
		executor = null;
		scriptThread = null;
		
		tearDown();
		getLogger().log(LogType.DEBUG, "Script stopped");
	}

	private void startNewScript(String script) {
		stopCurrentScript();
		// TODO: using ThreadGroup is not desired. Try to work around with
		// context debugger
		scriptThread = new Thread(new ScriptRunner(script));
		scriptThread.start();
		getLogger().log(LogType.DEBUG, "Script started");
	}

	private void pauseCurrentScript() {
		// TODO: to be done
	}

	private void resumeCurrentScript() {
		// TODO: to be done
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	public void stop() {
		if (executor != null) {
			stopCurrentScript();
		}
		super.stop();
	}

	private class ScriptRunner implements Runnable {
		private String script;

		public ScriptRunner(String script) {
			this.script = script;
		}

		@Override
		public void run() {
			cx = (MyContext)Context.enter();
			try {
				scope = cx.initStandardObjects();
				cx.setOptimizationLevel(-1);
				cx.setInstructionObserverThreshold(1);
				
				// Use the Counter class to define a Counter constructor
				// and prototype in JavaScript.
				ScriptableObject.defineClass(scope, ScriptExecutor.class);

				Object[] args = {};
				executor = (ScriptExecutor) cx.newObject(scope,
						ScriptExecutor.class.getSimpleName(), args);
				executor.setContext(cx);
				executor.setScope(scope);
				executor.setLogger(getLogger());
				executor.setMessageBroker(getBroker());
				scope.put("robot", scope, executor);

				Object result = cx.evaluateString(scope, script, "<cmd>", 1,
						null);

				getLogger().log(LogType.DEBUG,
						"Script execution ended: " + result);
			} catch (StopExecution e){
				getLogger().log(LogType.DEBUG,
						"Script execution stopped");				
			} catch (Exception e) {
				getLogger().log(LogType.ERROR,
						"Unable to start script: " + e.getMessage());
				e.printStackTrace();
			} finally {
				Context.exit();
			}
		}
	}
	/**
	 * Custom context is the mechanism to stop script execution
	 * @author maximlukichev
	 *
	 */
	private static class MyContext extends Context {
		boolean stopped = false;
	}
	
	/**
	 * This error will be thrown to stop script execution
	 * @author maximlukichev
	 *
	 */
	private static class StopExecution extends Error{}
	
	/**
	 * Custom ContextFactory to create custom context
	 * @author maximlukichev
	 *
	 */
	private static class MyFactory extends ContextFactory {
				
		@Override
		protected Context makeContext(){
			MyContext cx = new MyContext();
			return cx;
		}
				
		@Override
		protected void observeInstructionCount(Context cx, int instructionCount){
			MyContext mcx = (MyContext)cx;
			if (mcx.stopped) {
				mcx.stopped = false;
				throw new StopExecution();
			}
		}
	}

}