package com.example.pathfinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import dialogs.AlgorithmSelector;
import dialogs.ColorPickDialog;
import dialogs.GridSizeDialog;
import dialogs.ThemeDialog;
import dialogs.WeightDialog;
import dialogs.AlgorithmSelector.AlgorithmListener;
import dialogs.ColorPickDialog.ColorPickListener;
import dialogs.WeightDialog.WeightListener;

public class SettingsActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	
	// Компоненты UI
	private Button saveAndExit;
	private TextView gridStatus;
	private Spinner themeSpin;
	private SeekBar gSizeSb;
	
	// Другое
	private Intent intent;
	private int gridSize = 16;
	private int themeCode = 0;
	private String[] themesArray = {"Космическая", "Классическая"};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config_activity);
		
		intent = getIntent();
		gridSize = intent.getIntExtra("gridSize", 16);
		saveAndExit = (Button) findViewById(R.id.saveAndExit);
		saveAndExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("gSize", gridSize);
				intent.putExtra("tcode", themeCode);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		gridStatus = (TextView) findViewById(R.id.selectedGSize);
		gridStatus.setText("Выбранный размер: " + gridSize);
		
		gSizeSb = (SeekBar) findViewById(R.id.gridSizeSb);
		gSizeSb.setOnSeekBarChangeListener(this);
		gSizeSb.setProgress(gridSize - 16);
		
		themeSpin = (Spinner) findViewById(R.id.themeSpin);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, themesArray);
        themeSpin.setAdapter(adapter);
        
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	themeCode = position;
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            	themeCode = 0;
            }
        };
        themeSpin.setOnItemSelectedListener(itemSelectedListener);
	}
	
	protected void onPause() {
		super.onPause();
		Intent intent = new Intent();
		intent.putExtra("gSize", gridSize);
		intent.putExtra("tcode", themeCode);
		setResult(RESULT_OK, intent);
	}
	
	public String formatHex(String hexCode) {
		if(hexCode.length() <= 1)
			return "0" + hexCode;
		return hexCode;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		gridStatus.setText("Выбранный размер: " + (gridSize = progress + 16));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}

}
