package com.eyecreate.droidde;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

public class AndroidRunner implements ProjectRunner {
	
	Project localProject;

	public boolean runProject(Project project,Activity activity) {
		AssetManager assetM = activity.getAssets();
		localProject = project;
		//Copy assets to project folder
		copyFile(assetM,"0_build.bsh",project.getProjectDir()+"/0_build.bsh");
		copyFile(assetM,"1_aapt.bsh",project.getProjectDir()+"/1_aapt.bsh");
		copyFile(assetM,"2_compile-main.bsh",project.getProjectDir()+"/2_compile-main.bsh");
		copyFile(assetM,"3_dx.bsh",project.getProjectDir()+"/3_dx.bsh");
		copyFile(assetM,"4_apkbuilder.bsh",project.getProjectDir()+"/4_apkbuilder.bsh");
		copyFile(assetM,"5_signjar.bsh",project.getProjectDir()+"/5_signjar.bsh");
		//Rewrite scripts to use correct project values
		try {
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/1_aapt.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/1_aapt.bsh")).replace("name = \"HelloAndroid\";", "name = \""+project.getProjectName()+"\";"));
			if(!(new File(project.getProjectDir()+File.separator+"assets").exists())) FileUtils.writeStringToFile(new File(project.getProjectDir()+"/1_aapt.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/1_aapt.bsh")).replace("-A \"+stSourcePath+\"assets", ""));
			String bootlibs = "";
			for(File f: localProject.getProjectLibs())
			{
				bootlibs+=f.getAbsolutePath()+":";
			}
			String sourcefiles = "";
			for(File f : localProject.getProjectFiles())
			{
				if(f.getName().toLowerCase().endsWith(".java")) sourcefiles+=f.getAbsolutePath()+" ";
			}
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/2_compile-main.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/2_compile-main.bsh")).replace("-bootclasspath \"+stSourcePath+\"android.jar","-bootclasspath "+bootlibs));
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/2_compile-main.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/2_compile-main.bsh")).replace("stSourcePath+stJavafile",sourcefiles));
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/2_compile-main.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/2_compile-main.bsh")).replace("name = \"HelloAndroid\";", "name = \""+project.getProjectName()+"\";"));
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/2_compile-main.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/2_compile-main.bsh")).replace("stJavafile = \"src/com/t_arn/HelloAndroid/\";", "stJavafile = \""+getMainFile(project)+"\";"));
			String extLibs = "";
			String extLibsApk = "";
			if(localProject.getProjectLibs().size()>1)
			{
				for(File f: localProject.getProjectLibs())
				{
					if(!f.getName().toLowerCase().equals("android.jar"))
					{
						extLibs+=f.getAbsolutePath()+" ";
						extLibsApk+="-rj "+f.getAbsolutePath()+" ";
					}
				}
			}
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/3_dx.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/3_dx.bsh")).replace("// args += \" \"+stSourcePath+\"libs\\*.jar\";", "args += \" "+extLibs+"\";"));
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/3_dx.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/3_dx.bsh")).replace("name = \"HelloAndroid\";", "name = \""+project.getProjectName()+"\";"));
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/4_apkbuilder.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/4_apkbuilder.bsh")).replace("// args += \" -rj \"+stSourcePath+\"libs\";", "args += \" "+extLibsApk+"\";"));
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/4_apkbuilder.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/4_apkbuilder.bsh")).replace("name = \"HelloAndroid\";", "name = \""+project.getProjectName()+"\";"));
			FileUtils.writeStringToFile(new File(project.getProjectDir()+"/5_signjar.bsh"), FileUtils.readFileToString(new File(project.getProjectDir()+"/5_signjar.bsh")).replace("name = \"HelloAndroid\";", "name = \""+project.getProjectName()+"\";"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		//Create intent and check for good return 
		ComponentName cn = new ComponentName("com.t_arn.JavaIDEdroid","com.t_arn.JavaIDEdroid.MainActivity");
		Intent intent = new Intent("android.intent.action.SEND");
		intent.setComponent(cn);
		intent.putExtra("android.intent.extra.ScriptPath", project.getProjectDir()+"/0_build.bsh");
		intent.putExtra("android.intent.extra.ScriptAutoRun",true);
		//intent.putExtra("android.intent.extra.ScriptAutoExit", true);
		try
		{
			activity.startActivityForResult(intent, 42);
		}
		catch(ActivityNotFoundException ex)
		{
			Toast.makeText(activity.getApplicationContext(), "Java IDE droid app not found. Cannot compile!", Toast.LENGTH_LONG).show();
		}
		//later decide what to do with return values
		return true;
	}
	
	public void handleRunResult(Intent data)
	{
		//remove scripts
		removeScripts(localProject);
	}
	
	private void removeScripts(Project project)
	{
		FileUtils.deleteQuietly(new File(project.getProjectDir()+"/0_build.bsh"));
		FileUtils.deleteQuietly(new File(project.getProjectDir()+"/1_aapt.bsh"));
		FileUtils.deleteQuietly(new File(project.getProjectDir()+"/2_compile-main.bsh"));
		FileUtils.deleteQuietly(new File(project.getProjectDir()+"/3_dx.bsh"));
		FileUtils.deleteQuietly(new File(project.getProjectDir()+"/4_apkbuilder.bsh"));
		FileUtils.deleteQuietly(new File(project.getProjectDir()+"/5_signjar.bsh"));
	}
	
	//This method gets the main java file listed by the project and adapts it to script file.
	private String getMainFile(Project project)
	{
		String path = ((AndroidProject)project).getMainFile();
		return new File(path).getAbsolutePath().replace(project.getProjectDir().getAbsolutePath()+"/","");
	}
	
	private void copyFile(AssetManager assetM, String fromFilename, String toFilename) {
	    InputStream in = null;
	    OutputStream out = null;
	    try {
	        in = assetM.open(fromFilename);
	        out = new FileOutputStream(toFilename);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        Log.e("tag", e.getMessage());
	    }

	}

}
