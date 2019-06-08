package com.cnblogs.sjjg.tempview;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.Timer;
import java.util.TimerTask;


public class Thermograph extends View {

    float       viewWidth,viewHeight,contourWidth,contourHeight,ratio,bottomRadius,txtSize,borderSize,valueWidth,valueTop,valueBottom;
    float       value,max,min,step,hot,cold,warning;
    int         borderColor,hotColor,coldColor,txtColor,contourColor,valueColor;
    boolean     showValue,revert;
    Path        border,valueContour,bottom;
    Paint       paint;


    private     BlurMaskFilter  contourFilter;
    private     Timer           warningTimer;
    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("Thermograph", "onDraw: value = " + value );

        paint.setMaskFilter(null);
        if (showValue){
            paint.setColor(txtColor);
            float valueWidth = paint.measureText("" + (int)value);
            canvas.drawText("" + (int)value,getWidth() / 2 - valueWidth / 2,getHeight() / 2 + contourWidth * 2 + borderSize ,paint);
        }

        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderSize);
        canvas.drawPath(border,paint);

        if (value > hot){
            paint.setMaskFilter(contourFilter);
            paint.setColor(hotColor);
        }else if (value < cold){
            paint.setMaskFilter(contourFilter);
            paint.setColor(coldColor);
        }else{
            paint.setMaskFilter(null);
            paint.setColor(borderColor);
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(bottom,paint);
        canvas.drawPath(valueContour,paint);

    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();

        width = resolveSizeAndState(width, widthMeasureSpec, 0);

        int height = getSuggestedMinimumHeight() + getPaddingBottom() + getPaddingTop();

        height = resolveSizeAndState(height, heightMeasureSpec, 0);

        setMeasuredDimension(width, height);

        viewWidth = width;
        viewHeight = height;

        float r1 = viewWidth / viewHeight;
        float visibleWidth,visibleHeight;
        if (r1 > ratio){
            visibleHeight = viewHeight;
            visibleWidth = visibleHeight * ratio;
        }else{
            visibleWidth = viewWidth;
            visibleHeight = visibleWidth / ratio;
        }

        txtSize = visibleWidth;
        borderSize = visibleWidth / 8;
        contourWidth = visibleWidth - borderSize  ;
        contourHeight = visibleHeight - txtSize;
        paint.setTextSize(txtSize);

        valueWidth = borderSize * 2;
        bottomRadius = contourWidth - borderSize * 3;

        measureContour();
    }
    private void measureContour(){
        float x,y;
        float offset = viewWidth / 2.0f - contourWidth / 2.0f + borderSize * (1 + ratio) ;

        x = offset;
        y = viewHeight / 2.0f - contourWidth * 2 + borderSize * 2;
        border = new Path();
        border.moveTo(x,y);
        x = viewWidth / 2.0f + contourWidth / 2.0f - borderSize * (1 + ratio) ;
        border.cubicTo(viewWidth / 2 - contourWidth / 3,y - borderSize * 3.3f,viewWidth / 2 + contourWidth / 3,y - borderSize * 3.3f,x,y);
        y += contourWidth * 2 - borderSize;
        border.lineTo(x,y);

        x = viewWidth / 2.0f - contourWidth / 2.0f;
        RectF rectF = new RectF(x , y, x + contourWidth, y + contourWidth);
        border.arcTo(rectF,310,280);
        x = offset;

        border.lineTo(x,y);
        border.close();

        bottom = new Path();
        x = viewWidth / 2 - bottomRadius / 2;
        y += borderSize /** 0.90*/;
        bottom.moveTo(x,y);

        y = rectF.top + rectF.height() / 2 - bottomRadius / 2;
        RectF bottomRect = new RectF(x, y ,x + bottomRadius,y + bottomRadius);
        bottom.addOval(bottomRect,Path.Direction.CW);
        bottom.close();

        valueContour = new Path();
        valueTop = viewHeight / 2.0f - contourWidth * 2 + borderSize * 2 ;
        valueBottom = bottomRect.top + valueWidth / 2;
        step = (valueBottom - valueTop) / (max - min);

        setValue(value);

    }

    public void setValue(float v){
        Log.e("Thermograph", "setValue: value = " + value + " v = " + v );
        if (valueContour == null) return;
        value = v;
        valueContour.reset();

        if (v > hot || v < cold){
            startWarning();
        }else{
            stopWarning();
        }
        float x = viewWidth / 2 - valueWidth / 2;
        RectF valueRect = new RectF(x,valueTop + (max - value) * step,viewWidth / 2 + borderSize ,valueBottom);
        valueContour.addRect(valueRect,Path.Direction.CW);
        valueContour.moveTo(valueRect.left,valueRect.top );
        valueContour.cubicTo(viewWidth / 2 - valueWidth / 2,valueRect.top - valueWidth * 0.75f ,viewWidth / 2 + valueWidth / 2,valueRect.top - valueWidth * 0.75f,valueRect.right,valueRect.top);
        valueContour.close();

        invalidate();
    }
    private void startWarning(){
        warning = 0;
        if (warningTimer != null){
            stopWarning();
        }
        if (warningTimer == null){
            warningTimer = new Timer();
            warningTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    contourFilter = null;
                    float step = contourWidth / 50;
                    if (!revert){
                        warning += step;
                        if (warning > contourWidth  / 7){
                            revert = true;
                        }
                    }else{
                        warning -= step;
                        if (warning <= 0){
                            warning = 0.1f;
                            revert = false;
                        }
                    }
                    Log.e("Thermograph", "run: warning = " + warning + " contourWidth = " + contourWidth + " step = " + step);

                    contourFilter = new BlurMaskFilter(warning, BlurMaskFilter.Blur.NORMAL);
                    postInvalidate();
                }
            },200 * 1,200 * 1);
        }
    }
    private void stopWarning(){
        warning = 0;
        if (warningTimer != null){
            warningTimer.cancel();
            warningTimer = null;
        }
    }
    //----========-------==========---------========---------=========--------------------------=---

    private void init(Context context,AttributeSet attrs){

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        setLayerType(View.LAYER_TYPE_SOFTWARE,paint);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.thermograph);
        min = ta.getFloat(R.styleable.thermograph_minValue, -50);
        max = ta.getFloat(R.styleable.thermograph_maxValue,150);
        value = ta.getFloat(R.styleable.thermograph_thermoValue,0);
        hot = ta.getFloat(R.styleable.thermograph_hotValue,60);
        cold = ta.getFloat(R.styleable.thermograph_coldValue,0);
        ratio = ta.getFloat(R.styleable.thermograph_ratio,0.25f);

        txtColor = ta.getColor(R.styleable.thermograph_textColor,Color.WHITE);

        int c = ContextCompat.getColor(context,R.color.temp_color);
        borderColor = ta.getColor(R.styleable.thermograph_borderColor,c);

        c = ContextCompat.getColor(context,R.color.temp_cold_color);
        contourColor = ta.getColor(R.styleable.thermograph_contourColor,c);

        c = ContextCompat.getColor(context,R.color.temp_hot_color);
        hotColor = ta.getColor(R.styleable.thermograph_hotColor,c);

        c = ContextCompat.getColor(context,R.color.temp_cold_color);
        coldColor = ta.getColor(R.styleable.thermograph_coldColor,c);

        showValue = ta.getBoolean(R.styleable.thermograph_showValue,true);

    }

    public Thermograph(Context context) {
        super(context);
        init(context,null);
    }

    public Thermograph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public Thermograph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Thermograph(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }
}
