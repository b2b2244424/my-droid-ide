package com.eyecreate.droidde.android;

import com.eyecreate.droidde.interfaces.Project;
import com.eyecreate.droidde.interfaces.ProjectFactory;

public class AndroidProjectFactory implements ProjectFactory {
	public Project getProject() {
		return new AndroidProject();
	}
}
