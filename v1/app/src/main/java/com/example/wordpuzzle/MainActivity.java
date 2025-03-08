package com.example.wordpuzzle;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.graphics.Color;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;
import android.graphics.Typeface;

public class MainActivity extends AppCompatActivity {

    private static class Word {
        String english;
        String turkish;
        boolean isSolved;

        Word(String english, String turkish) {
            this.english = english;
            this.turkish = turkish;
            this.isSolved = false;
        }
    }

    private ArrayList<Word> words;
    private LinearLayout wordsContainer;
    private TextView guessTextView;
    private HexagonGridView hexagonGridView;
    private int currentWordIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeWords();
        initializeViews();
        setupGame();
    }

    private void initializeWords() {
        words = new ArrayList<>();
        words.add(new Word("example", "ALIŞTIRMA"));
        words.add(new Word("book", "KİTAP"));
        words.add(new Word("friend", "ARKADAŞ"));
    }

    private void initializeViews() {
        wordsContainer = findViewById(R.id.wordsContainer);
        guessTextView = findViewById(R.id.guessTextView);
        hexagonGridView = findViewById(R.id.hexagonGrid);
        
        hexagonGridView.setBackgroundColor(Color.WHITE);
        
        hexagonGridView.setOnWordSelectedListener(new HexagonGridView.OnWordSelectedListener() {
            @Override
            public void onWordSelected(String word) {
                checkWord(word);
            }

            @Override
            public void onSelectionStarted() {
                guessTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSelectionUpdated(String currentWord) {
                guessTextView.setText(currentWord);
            }
        });

        findViewById(R.id.clearButton).setOnClickListener(v -> clearSelection());
    }

    private void setupGame() {
        displayWords();
        setupLetters();
    }

    private void displayWords() {
        wordsContainer.removeAllViews();
        
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        for (Word word : words) {
            TextView wordView = new TextView(this);
            wordView.setText(word.english);
            wordView.setTextSize(16); 
            wordView.setPadding(8, 4, 8, 4);
            if (word.isSolved) {
                wordView.setPaintFlags(wordView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            rowLayout.addView(wordView);
            
            TextView separator = new TextView(this);
            separator.setText(" • ");
            separator.setTextSize(16);
            separator.setPadding(4, 4, 4, 4);
            rowLayout.addView(separator);
        }
        
        if (rowLayout.getChildCount() > 0) {
            rowLayout.removeViewAt(rowLayout.getChildCount() - 1);
        }
        
        wordsContainer.addView(rowLayout);
    }

    private void setupLetters() {
        Map<String, Integer> maxLetterCount = new HashMap<>();
        
        for (Word word : words) {
            Map<String, Integer> currentWordCount = new HashMap<>();
            for (char c : word.turkish.toCharArray()) {
                String letter = String.valueOf(c);
                currentWordCount.put(letter, currentWordCount.getOrDefault(letter, 0) + 1);
            }
            
            for (Map.Entry<String, Integer> entry : currentWordCount.entrySet()) {
                String letter = entry.getKey();
                int count = entry.getValue();
                maxLetterCount.put(letter, Math.max(maxLetterCount.getOrDefault(letter, 0), count));
            }
        }
        
        List<String> letters = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : maxLetterCount.entrySet()) {
            String letter = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                letters.add(letter);
            }
        }
        
        Collections.shuffle(letters);
        
        hexagonGridView.setLetters(letters);
    }

    private void checkWord(String guess) {
        boolean found = false;
        for (Word word : words) {
            String turkishWord = word.turkish.toUpperCase(new Locale("tr", "TR"));
            if (!word.isSolved && guess.equals(turkishWord)) {
                word.isSolved = true;
                found = true;
                showCorrectAnimation();
                displayWords();
                break;
            }
        }
        
        if (!found) {
            showWrongAnimation();
        }
    }

    private void showCorrectAnimation() {
        int colorFrom = Color.WHITE;
        int colorTo = Color.GREEN;
        
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(500);
        colorAnimation.addUpdateListener(animator -> 
            guessTextView.setBackgroundColor((int) animator.getAnimatedValue()));

        colorAnimation.start();

        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                guessTextView.setVisibility(View.INVISIBLE);
                guessTextView.setBackgroundColor(Color.TRANSPARENT);
            }
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        
        guessTextView.startAnimation(fadeOut);
    }

    private void showWrongAnimation() {
        int colorFrom = Color.WHITE;
        int colorTo = Color.RED;
        
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(500);
        colorAnimation.addUpdateListener(animator -> 
            guessTextView.setBackgroundColor((int) animator.getAnimatedValue()));

        colorAnimation.start();

        AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(500);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                guessTextView.setVisibility(View.INVISIBLE);
                guessTextView.setBackgroundColor(Color.TRANSPARENT);
            }
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        
        guessTextView.startAnimation(fadeOut);
    }

    private void clearSelection() {
        guessTextView.setText("");
        guessTextView.setVisibility(View.INVISIBLE);
    }
} 
