package com.example.gymtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LineGraphView extends View {
    private List<Integer> distances;
    private List<String> dates;
    private Paint paint;

    int paddingTop = 30;
    int paddingBottom = 60;
    int paddingLeft = 60;
    int paddingRight = 20;

    public LineGraphView(Context context) {
        super(context);
        distances = new ArrayList<>();
        dates = new ArrayList<>();
        init();
    }

    public LineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineGraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
    }

    public void setData(List<Integer> newDistances, List<String> newDates) {
        if (newDistances != null && newDates != null) {
            int maxDataPoints = 8;

            if (distances == null) {
                distances = new ArrayList<>();
                dates = new ArrayList<>();
            }


            distances.addAll(newDistances);
            dates.addAll(newDates);

            if (distances.size() > maxDataPoints) {
                distances = performDataSampling(distances, maxDataPoints);
                dates = performDataSampling(dates, maxDataPoints);
            }

            invalidate();
        }
    }

    private <T> List<T> performDataSampling(List<T> data, int maxDataPoints) {
        if (data.size() <= maxDataPoints) {
            return data;
        }

        List<T> sampledData = new ArrayList<>();
        sampledData.add(data.get(0));

        int step = (data.size() - 2) / (maxDataPoints - 2);

        for (int i = 1; i < maxDataPoints - 1; i++) {
            int index = 1 + i * step;
            sampledData.add(data.get(index));
        }

        sampledData.add(data.get(data.size() - 1));

        return sampledData;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int desiredWidth = 10;
        int desiredHeight = 400;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (distances != null && dates != null && distances.size() > 0) {
            int numDataPoints = distances.size();
            int maxDistance = Collections.max(distances);


            float xScaleFactor = (float) (getWidth() - paddingLeft - paddingRight) / (numDataPoints - 1);
            float yScaleFactor = (float) (getHeight() - paddingTop - paddingBottom) / maxDistance;


            float xAxisY = getHeight() - paddingBottom;
            canvas.drawLine(paddingLeft, xAxisY, getWidth() - paddingRight, xAxisY, paint);


            canvas.drawLine(paddingLeft, paddingTop, paddingLeft, getHeight() - paddingBottom, paint);


            drawXAxisLabels(canvas, xScaleFactor, getHeight() - paddingBottom - 10);


            drawYAxisLabels(canvas, yScaleFactor);

            for (int i = 0; i < numDataPoints; i++) {
                float x = i * xScaleFactor + paddingLeft;
                float y = getHeight() - paddingBottom - (distances.get(i) * yScaleFactor);
                int dataPointRadius = 10;


                x += dataPointRadius / 2;


                paint.setColor(Color.BLUE);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x, y, dataPointRadius, paint);
                paint.setStyle(Paint.Style.STROKE);


                if (i > 0) {
                    float prevX = (i - 1) * xScaleFactor + paddingLeft + dataPointRadius / 2;
                    float prevY = getHeight() - paddingBottom - (distances.get(i - 1) * yScaleFactor);

                    paint.setColor(Color.BLUE);
                    canvas.drawLine(prevX, prevY, x, y, paint);
                }
            }
        }
    }

    private void drawXAxisLabels(Canvas canvas, float xScaleFactor, float y) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);

        int maxLabels = getWidth() / 50;
        int step = (int) Math.ceil((float) distances.size() / maxLabels);

        for (int i = 0; i < distances.size(); i += step) {
            float x = i * xScaleFactor + 40;
            float xValueY = getHeight() - 10;


            if (i == 0) {
                x += xScaleFactor * 0.2f;
            }
            if (i == 7) {
                x += xScaleFactor * 0.2f;
            }

            String date = formatShortDate(dates.get(i));
            float textWidth = paint.measureText(date);
            float dateX = x - textWidth / 2;

            if (i == distances.size() - 1) {

                dateX = x - textWidth;
            }

            canvas.drawText(date, dateX, xValueY, paint);
        }
    }

    private void drawYAxisLabels(Canvas canvas, float yScaleFactor) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);

        int maxDistance = Collections.max(distances);
        int maxDigits = String.valueOf(maxDistance).length();

        int labelIncrement = 1;

        if (maxDistance >= 10 && maxDistance < 100) {
            labelIncrement = 10;
        }
        else if (maxDistance >= 100 && maxDistance < 300) {
            labelIncrement = 20;
        } else if (maxDistance >= 300) {
            labelIncrement = 50;
        }

        for (int i = 0; i <= maxDistance; i += labelIncrement) {
            float y = getHeight() - paddingBottom - i * yScaleFactor;
            float yValueX = paddingLeft - 40;


            if (maxDigits == 3) {
                yValueX -= 20;
            }

            int alpha = 50;
            paint.setColor(Color.argb(alpha, Color.red(paint.getColor()), Color.green(paint.getColor()), Color.blue(paint.getColor())));


            canvas.drawLine(paddingLeft, y, getWidth() - paddingRight, y, paint);


            paint.setColor(Color.BLACK);


            canvas.drawText(String.valueOf(i), yValueX, y + 20, paint);
        }
    }
    private int parseDate(String date) {

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Date parsedDate = fullDateFormat.parse(date);
            return (int) parsedDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    private String formatDate(int date) {

        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return fullDateFormat.format(new Date(date));
    }

    private String formatShortDate(String fullDate) {
        try {
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = fullDateFormat.parse(fullDate);
            SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd/MM");
            return shortDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return fullDate;
        }
    }
}