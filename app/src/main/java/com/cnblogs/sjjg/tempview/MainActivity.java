package com.cnblogs.sjjg.tempview;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    @BindView(R.id.seekBar)         SeekBar         tempSeek;
    @BindView(R.id.thermograph1)    Thermograph     thermograph1;
    @BindView(R.id.thermograph2)    Thermograph     thermograph2;
    @BindView(R.id.original_value)  TextView        originalValue;


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (thermograph1.min < 0){
            thermograph1.setValue(progress + thermograph1.min);
        }else{
            thermograph1.setValue(progress - thermograph1.min);
        }
        if (thermograph2.min < 0){
            thermograph2.setValue(progress + thermograph2.min);
        }else{
            thermograph2.setValue(progress - thermograph2.min);
        }
        if (thermograph2.min < 0){
            thermograph2.setValue(progress + thermograph2.min);
            originalValue.setText("" + (progress + (int)thermograph2.min));
        }else{
            originalValue.setText("" + (progress - (int)thermograph2.min));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        tempSeek.setOnSeekBarChangeListener(this);
    }

}
