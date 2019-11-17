package com.quizy.quizy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quizy.quizy.db.Data;
import com.quizy.quizy.db.Quiz;

/**
 * L'Activity pour afficher le résultat d'un quiz.
 */
public class QuizDoneActivity extends AppCompatActivity {

    /**
     * L'index du quiz qui a été fait.
     */
    int quizIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_done);

        Data db = Data.getInstance(this);

        // Aller chercher les informations du Intent
        Intent myIntent = getIntent();
        quizIndex = myIntent.getIntExtra("quizIndex", 0);
        int currentScore = myIntent.getIntExtra("currentScore", 0);

        // Aller chercher le quiz
        Quiz quiz = db.getQuizes().get(quizIndex);

        // Calculer le résultat
        double result = ((double)currentScore / quiz.questions.size());
        double pourcentageResult = Math.round(result*10000)/100.0d;

        // Afficher le résultat
        TextView resultView = findViewById(R.id.activity_quiz_done_result);
        resultView.setText(currentScore + "/" + quiz.questions.size() + " (" + (pourcentageResult) + "%)");

        // Sauvegarder le résultat
        quiz.results.add(result);
        db.save(this);
    }

    /**
     * Retourner au menu principal.
     *
     * @param view View donné par un bouton.
     */
    public void mainMenu(View view) {
        Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(myIntent);
    }

    /**
     * Aller voir les statistiques.
     *
     * @param view View donné par un bouton.
     */
    public void stats(View view) {
        // On passe par le menu principal pour que le boutons "Back" dans l'écran de statistiques ramène
        // au menu principal.
        Intent myIntent = new Intent(this, MainActivity.class);
        myIntent.putExtra("quizStatsIndex", quizIndex);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(myIntent);
    }
}
