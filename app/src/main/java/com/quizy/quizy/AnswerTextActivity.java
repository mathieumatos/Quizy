package com.quizy.quizy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quizy.quizy.db.Data;
import com.quizy.quizy.db.Question;
import com.quizy.quizy.db.QuestionText;
import com.quizy.quizy.db.Quiz;

/**
 * L'Activity pour répondre à une questions à réponse textuelle.
 */
public class AnswerTextActivity extends AppCompatActivity {

    /**
     * La question à laquelle il faut répondre.
     */
    QuestionText question;

    /**
     * Le score du quiz avant cette question.
     */
    int currentScore = 0;

    /**
     * Référence au <code>Data</code> global.
     */
    Data db;

    @Override
    public void onBackPressed() {
        // Enlève la possiblité de retourner en arrière.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_text);

        // Aller chercher les informations du Intent
        Intent myIntent = getIntent();
        int questionIndex = myIntent.getIntExtra("questionIndex", 0);
        currentScore = myIntent.getIntExtra("currentScore", 0);

        // Aller chercher la question
        db = Data.getInstance(this);
        question = (QuestionText) db.getQuestions().get(questionIndex);

        // Écrire la question
        TextView questionText = findViewById(R.id.activity_answer_text_question);
        questionText.setText(question.question);
    }

    /**
     * Vérifie la réponse et passe à la questions suivante.
     *
     * @param v View passé par le bouton.
     */
    public void next(View v) {
        EditText answerText = findViewById(R.id.activity_answer_text_answer);

        Question nextQuestion = getNextQuestion();

        // Création du Intent dépendemment s'il y a une prochaine question
        Intent nextIntent;
        if (nextQuestion == null) {
            // S'il n'y a pas de prochaine questions, alors c'est la fin du quiz
            nextIntent = new Intent(this, QuizDoneActivity.class);
            nextIntent.putExtra("quizIndex", db.getQuizes().indexOf(question.quiz));
        } else {
            nextIntent = new Intent(this, AnswerTextActivity.class);
            nextIntent.putExtra("questionIndex", db.getQuestions().indexOf(nextQuestion));
        }
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        nextIntent.putExtra("currentScore", (answerText.getText().toString().equals(question.answer)) ? (currentScore + 1) : (currentScore));
        startActivity(nextIntent);
    }

    /**
     * Méthode utilitaire pour aller chercher la prochaine question.
     *
     * @return La prochaine question.
     */
    private Question getNextQuestion() {
        try {
            for (int i = 0; i < question.quiz.questions.size(); i++) {
                if (question == question.quiz.questions.get(i))
                    return question.quiz.questions.get(i + 1);
            }
        } catch (Exception e) {
            // C'est la dernière question, nous retournons alors null
        }
        return null;
    }
}
