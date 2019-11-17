package com.quizy.quizy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.quizy.quizy.db.Data;
import com.quizy.quizy.db.Question;
import com.quizy.quizy.db.QuestionChoice;
import com.quizy.quizy.db.Quiz;

import java.util.List;

/**
 * L'Activity pour répondre à une questions à choix de réponse.
 */
public class AnswerChoiceActivity extends AppCompatActivity {

    /**
     * La question à laquelle il faut répondre.
     */
    QuestionChoice question;

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
        setContentView(R.layout.activity_answer_choice);

        // Aller chercher les informations du Intent
        Intent myIntent = getIntent();
        int questionIndex = myIntent.getIntExtra("questionIndex", 0);
        currentScore = myIntent.getIntExtra("currentScore", 0);

        // Aller chercher la question
        db = Data.getInstance(this);
        question = (QuestionChoice) db.getQuestions().get(questionIndex);

        // Créer la liste de choix réponses
        ListView listView = findViewById(R.id.activity_answer_choice_list);
        ListViewAdapter listAdapter = new ListViewAdapter(question.choices);
        listView.setAdapter(listAdapter);

        // Écrire la question
        TextView questionText = findViewById(R.id.activity_answer_choice_question);
        questionText.setText(question.question);
    }

    /**
     * Vérifie la réponse et passe à la questions suivante.
     *
     * @param choice Le choix de réponse sélectionné.
     */
    public void next(int choice) {
        Question nextQuestion = getNextQuestion();

        // Création du Intent dépendemment s'il y a une prochaine question
        Intent nextIntent;
        if (nextQuestion == null) {
            // S'il n'y a pas de prochaine questions, alors c'est la fin du quiz
            nextIntent = new Intent(this, QuizDoneActivity.class);
            nextIntent.putExtra("quizIndex", db.getQuizes().indexOf(question.quiz));
        } else {
            nextIntent = new Intent(this, AnswerChoiceActivity.class);
            nextIntent.putExtra("questionIndex", db.getQuestions().indexOf(nextQuestion));
        }
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        nextIntent.putExtra("currentScore", (choice == question.answer) ? (currentScore + 1) : (currentScore));
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

    /**
     * L'Adapter pour la liste de choix de réponses.
     */
    private class ListViewAdapter extends BaseAdapter {

        LayoutInflater inflater;
        List<String> choices;

        ListViewAdapter(List<String> choices) {
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            this.choices = choices;
        }

        @Override
        public int getCount() {
            return choices.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(R.layout.listviewelement_answer, viewGroup, false);
            }

            // Écrire le choix de réponse
            Button choice = view.findViewById(R.id.listviewelement_answer_choice);
            choice.setText(choices.get(i));

            // Ajout du listener
            choice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    next(i);
                }
            });

            return view;
        }
    }
}
