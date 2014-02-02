package com.androbotus.mq2.contract;

public class ScriptControlMessage extends ControlMessage {
	private String script;
	
	public String getScript() {
		return script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}
}
