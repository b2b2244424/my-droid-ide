package com.eyecreate.droidde;

import java.util.List;
import java.util.Arrays;

//This enum stores values about each project type so the Project class knows how to load and deal with files in the project.
public enum ProjectTypes {
	ANDROID (Arrays.asList("java", "xml"),Arrays.asList("android.jar"));
	
	private final List<String> acceptableTypes;
	private final List<String> defaultLibs;
	
	ProjectTypes(List<String> types,List<String> libs){
		this.acceptableTypes = types;
		this.defaultLibs = libs;
	}
	
	
	public boolean isAcceptedFile(String extension){
		if(acceptableTypes.contains(extension)) return true;
		return false;
	}
	
	public List<String> getDefaultLibs(){
		return defaultLibs;
	}
	
	public List<String> getAcceptedFileTypes(){
		return acceptableTypes;
	}
}
