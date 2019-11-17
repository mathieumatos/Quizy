package com.quizy.quizy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.quizy.quizy.db.Data;
import com.quizy.quizy.db.QuestionText;

/**
 * L'Activity pour modifier une question à réponse textuelle.
 */
public class EditQuestionTextActivity extends AppCompatActivity {

    /**
     * Référence au <code>Data</code> global.
     */
    Data db;

    /**
     * La question à modifier.
     */
    QuestionText question;

    /**
     * Le texte de la question.
     */
    EditText questionText;

    /**
     * La réponse à la question.
     */
    EditText answerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_question_text);

        db = Data.getInstance(this);

        Intent myIntent = getIntent();
        int questionIndex = myIntent.getIntExtra("questionIndex", 0);
        question = (QuestionText) db.getQuestions().get(questionIndex);

        questionText = findViewById(R.id.activity_edit_question_choice_text);
        questionText.setText(question.question);

        answerText = findViewById(R.id.activity_edit_question_choice_list);
        answerText.setText(question.answer);
    }

    /**
     * Annule l'édition de la question.
     *
     * @param view View donné par un bouton.
     */
    public void cancelEdit(View view) {
        finish();
    }

    /**
     * Sauvegarde l'édition de la question.
     *
     * @param view View donné par un bouton.
     */
    public void saveEdit(View view) {
        question.question = questionText.getText().toString();
        question.answer = answerText.getText().toString();
        db.save(this);
        finish();
    }
}
