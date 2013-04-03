package com.eyecreate.droidde;

import android.app.Activity;
import android.content.Intent;

public interface ProjectRunner {

	public boolean runProject(Project project, Activity activity);
	
	public void handleRunResult(Intent data);
}
