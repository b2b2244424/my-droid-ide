package com.eyecreate.droidde;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;

public interface Project {

	public abstract boolean isValid();

	public abstract List<File> getProjectFiles();
	
	public abstract List<File> getProjectLibs();

	public abstract ProjectTypes getProjectType();
	
	public abstract String getProjectName();
	
	public abstract File getProjectDir();
	
	public abstract File getMainProjectFile();
	
	public abstract void triggerProjectStateSave();
	
	public abstract boolean runProject(Activity activity);
	
	public abstract void handleRunResult(Intent data); 
	
	public abstract void setMainProjectFile(File f);
	
	public abstract void addNewFileToProject(File f);
	
	public abstract void removeFileFromProject(File f);

}
