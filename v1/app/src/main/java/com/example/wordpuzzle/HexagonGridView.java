package com.example.wordpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HexagonGridView extends View {
    private List<Hexagon> hexagons;
    private Paint hexPaint;
    private Paint textPaint;
    private Paint linePaint;
    private Path currentPath;
    private List<Hexagon> selectedHexagons;
    private OnWordSelectedListener listener;
    private float hexagonSize;
    private float spacing;

    public interface OnWordSelectedListener {
        void onWordSelected(String word);
        void onSelectionStarted();
        void onSelectionUpdated(String currentWord);
    }

    static class Hexagon {
        float centerX, centerY;
        String letter;
        Path path;
        boolean isSelected;

        Hexagon(float x, float y, String letter) {
            this.centerX = x;
            this.centerY = y;
            this.letter = letter;
            this.path = new Path();
            this.isSelected = false;
        }
    }

    public HexagonGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        hexagons = new ArrayList<>();
        selectedHexagons = new ArrayList<>();
        currentPath = new Path();

        hexPaint = new Paint();
        hexPaint.setColor(Color.rgb(200, 200, 200));
        hexPaint.setStyle(Paint.Style.FILL);
        hexPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.rgb(0, 150, 0));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        textPaint.setTypeface(typeface);

        linePaint = new Paint();
        linePaint.setColor(Color.rgb(0, 150, 0));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10);
        linePaint.setAntiAlias(true);
    }

    public void setLetters(List<String> letters) {
        hexagons.clear();
        
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        
        float availableSize = Math.min(viewWidth, viewHeight) * 0.8f;
        int totalHexagons = letters.size();
        
        hexagonSize = availableSize / (totalHexagons * 0.8f);
        spacing = hexagonSize * 0.1f;
        
        float centerX = viewWidth / 2f;
        float centerY = viewHeight / 2f;
        float radius = hexagonSize * 3f;
        
        double startAngle = -Math.PI / 2;
        double angleStep = 2 * Math.PI / totalHexagons;
        
        for (int i = 0; i < totalHexagons; i++) {
            double angle = startAngle + angleStep * i;
            float x = centerX + radius * (float)Math.cos(angle);
            float y = centerY + radius * (float)Math.sin(angle);
            createHexagon(x, y, letters.get(i));
        }

        textPaint.setTextSize(hexagonSize/1.8f);
        invalidate();
    }

    private void createHexagon(float centerX, float centerY, String letter) {
        Hexagon hexagon = new Hexagon(centerX, centerY, letter);
        Path hexPath = new Path();
        
        for (int i = 0; i < 6; i++) {
            float angle = (float) (Math.PI / 3 * i);
            float x = centerX + hexagonSize * (float) Math.cos(angle);
            float y = centerY + hexagonSize * (float) Math.sin(angle);
            
            if (i == 0) {
                hexPath.moveTo(x, y);
            } else {
                hexPath.lineTo(x, y);
            }
        }
        hexPath.close();
        hexagon.path = hexPath;
        hexagons.add(hexagon);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Hexagon hexagon : hexagons) {
            hexPaint.setColor(hexagon.isSelected ? Color.rgb(0, 200, 0) : Color.LTGRAY);
            canvas.drawPath(hexagon.path, hexPaint);
            canvas.drawText(hexagon.letter, 
                          hexagon.centerX, 
                          hexagon.centerY + textPaint.getTextSize() / 3, 
                          textPaint);
        }

        if (!currentPath.isEmpty()) {
            canvas.drawPath(currentPath, linePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath.reset();
                selectedHexagons.clear();
                for (Hexagon hexagon : hexagons) {
                    hexagon.isSelected = false;
                }
                if (listener != null) {
                    listener.onSelectionStarted();
                }
                checkHexagonSelection(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                checkHexagonSelection(x, y);
                if (listener != null && !selectedHexagons.isEmpty()) {
                    StringBuilder word = new StringBuilder();
                    for (Hexagon hexagon : selectedHexagons) {
                        word.append(hexagon.letter);
                    }
                    listener.onSelectionUpdated(word.toString());
                }
                break;

            case MotionEvent.ACTION_UP:
                if (listener != null && !selectedHexagons.isEmpty()) {
                    StringBuilder word = new StringBuilder();
                    for (Hexagon hexagon : selectedHexagons) {
                        word.append(hexagon.letter);
                    }
                    listener.onWordSelected(word.toString());
                }
                currentPath.reset();
                for (Hexagon hexagon : hexagons) {
                    hexagon.isSelected = false;
                }
                selectedHexagons.clear();
                break;
        }

        invalidate();
        return true;
    }

    private void checkHexagonSelection(float x, float y) {
        for (Hexagon hexagon : hexagons) {
            if (contains(hexagon.path, x, y) && !selectedHexagons.contains(hexagon)) {
                hexagon.isSelected = true;
                selectedHexagons.add(hexagon);
                if (selectedHexagons.size() == 1) {
                    currentPath.moveTo(hexagon.centerX, hexagon.centerY);
                } else {
                    currentPath.lineTo(hexagon.centerX, hexagon.centerY);
                }
            }
        }
    }

    private boolean contains(Path path, float x, float y) {
        android.graphics.RectF bounds = new android.graphics.RectF();
        path.computeBounds(bounds, true);
        bounds.inset(-20, -20);
        return bounds.contains(x, y);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!hexagons.isEmpty()) {
            List<String> letters = new ArrayList<>();
            for (Hexagon hexagon : hexagons) {
                letters.add(hexagon.letter);
            }
            setLetters(letters);
        }
    }

    public void setOnWordSelectedListener(OnWordSelectedListener listener) {
        this.listener = listener;
    }
} 
