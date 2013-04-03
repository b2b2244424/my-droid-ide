package com.eyecreate.droidde;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UpdateAppearance;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RichEditText extends EditText {

	int lastLines=0;
	long lastmill=SystemClock.uptimeMillis();
	String currentLang = "Java";
	SyntaxConfig syntaxRegex;
	boolean drawFail = false;
	
	public RichEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		syntaxRegex = new SyntaxConfig();
		// TODO Auto-generated constructor stub
	}
	
	public void setCurrentLangFromExt(String s){
		currentLang = syntaxRegex.languageFromFileType(s);
	}
	
	public void onTextChanged(CharSequence text, int start, int before, int after) {
		new Thread(new Runnable() {
			    public void run() {
			    	lastmill=SystemClock.uptimeMillis();
			    	getRootView().findViewById(R.id.editorcontent).postDelayed(new Runnable() {
			        public void run() {
			        	if(SystemClock.uptimeMillis()-lastmill>=500){
			        	  UpdateSyntax();
			          }
			        }
			      },500);
			    }
			  }).start();
		super.onTextChanged(text, start, before, after);
		if(text.toString().contains("\n")) {
			renumberLines(this.getLineCount());
		}
	}
	
	public void renumberLines(int lines) {
		TextView tv = (TextView) getRootView().findViewById(R.id.linenumbers);
		tv.setText("1");
		for (int i =2; i<=lines;i++){
			tv.setText(tv.getText()+"\n"+i);
		}
	}
	
	private void UpdateSyntax() {
		if(!currentLang.equals("")){
		Spannable erasespan=this.getText();
		Object[] spans = this.getText().getSpans(0, this.getText().length(), UpdateAppearance.class);
		for(int i = 0; i<spans.length;i++){
			erasespan.removeSpan(spans[i]);
		}
		//keywords
		Pattern p = Pattern.compile(syntaxRegex.regexValue(currentLang, "Keywords"),Pattern.MULTILINE);
		Matcher m = p.matcher(this.getText());
		while(m.find()){
			Spannable sp = this.getText();
			sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), m.toMatchResult().start(), m.toMatchResult().end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new ForegroundColorSpan(android.graphics.Color.BLUE), m.toMatchResult().start(), m.toMatchResult().end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		//comments
		Pattern p2 = Pattern.compile(syntaxRegex.regexValue(currentLang, "SingleLineComment"),Pattern.MULTILINE);
		Matcher m2 = p2.matcher(this.getText());
		while(m2.find()){
			Spannable sp = this.getText();
			Object[] spans2 = this.getText().getSpans(m2.toMatchResult().start(), m2.toMatchResult().end(), UpdateAppearance.class);
			for(int i = 0; i<spans2.length;i++){
				sp.removeSpan(spans2[i]);
			}
			sp.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), m2.toMatchResult().start(), m2.toMatchResult().end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new ForegroundColorSpan(android.graphics.Color.rgb(63, 127, 95)), m2.toMatchResult().start(), m2.toMatchResult().end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		Pattern p3 = Pattern.compile(syntaxRegex.regexValue(currentLang, "MultiLineComment"),Pattern.DOTALL);
		Matcher m3 = p3.matcher(this.getText());
		while(m3.find()){
			Spannable sp = this.getText();
			Object[] spans2 = this.getText().getSpans(m3.toMatchResult().start(), m3.toMatchResult().end(), UpdateAppearance.class);
			for(int i = 0; i<spans2.length;i++){
				sp.removeSpan(spans2[i]);
			}
			sp.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), m3.toMatchResult().start(), m3.toMatchResult().end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			sp.setSpan(new ForegroundColorSpan(android.graphics.Color.rgb(63, 127, 95)), m3.toMatchResult().start(), m3.toMatchResult().end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		}
	}
	
	 @Override
	 public void onDraw(Canvas c) {
		if(lastLines!=getLineCount()){
			 renumberLines(getLineCount());
			 lastLines=getLineCount();
		 }
		try{ 
		super.onDraw(c);
		drawFail=false;
		}
		catch(IndexOutOfBoundsException e)
		{
			e.printStackTrace();
			if(!drawFail)
			{
				Toast.makeText(getRootView().getContext(), "Due to a bug, drawing is temporaily halted.", Toast.LENGTH_SHORT).show();
				drawFail=true;
			}
		}
	 }

}
