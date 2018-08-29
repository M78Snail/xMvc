package bean;

import java.util.Map;

public class Param {
	private Map<String, Object> paramMap;

	public Param(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}

	public Map<String, Object> getMap() {
		return paramMap;
	}
	
	public long getLong(String name){
		return Long.parseLong((String) paramMap.get(name));
	}
}
