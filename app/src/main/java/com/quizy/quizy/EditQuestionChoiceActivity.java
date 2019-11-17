package com.quizy.quizy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;

import com.quizy.quizy.db.Data;
import com.quizy.quizy.db.QuestionChoice;

import java.util.ArrayList;
import java.util.List;

/**
 * L'Activity pour modifier une question à choix de réponses.
 */
public class EditQuestionChoiceActivity extends AppCompatActivity {

    /**
     * Référence au <code>Data</code> global.
     */
    Data db;

    /**
     * La question à modifier.
     */
    QuestionChoice question;

    /**
     * L'index de la réponse à la question
     */
    int answer;

    /**
     * Une liste des choix de réponses.
     */
    List<String> choices;

    /**
     * L'Adapter du ListView des choix de réponse.
     */
    ListViewAdapter listAdapter;

    /**
     * Le texte de la question.
     */
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_question_choice);

        db = Data.getInstance(this);

        // Aller chercher les informations du Intent
        Intent myIntent = getIntent();
        int questionIndex = myIntent.getIntExtra("questionIndex", 0);

        // Aller chercher la question
        question = (QuestionChoice) db.getQuestions().get(questionIndex);

        // Initialisation des variables
        choices = new ArrayList<>(question.choices);
        answer = question.answer;

        // Écrire la question
        editText = findViewById(R.id.activity_edit_question_choice_text);
        editText.setText(question.question);

        // Écrire la liste des choix de réponse
        ListView listView = findViewById(R.id.activity_edit_question_choice_list);
        listAdapter = new ListViewAdapter();
        listView.setAdapter(listAdapter);
    }

    /**
     * Créer un nouveau choix de réponse.
     *
     * @param view View donné par un bouton.
     */
    public void newChoice(View view) {
        choices.add("");
        listAdapter.notifyDataSetChanged();
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
        question.question = editText.getText().toString();
        question.choices = choices;
        question.answer = answer;
        db.save(this);
        finish();
    }

    /**
     * L'Adapter de la liste de choix de réponses.
     */
    private class ListViewAdapter extends BaseAdapter {

        LayoutInflater inflater;

        ListViewAdapter() {
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
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
                view = inflater.inflate(R.layout.listviewelement_answer_edit, viewGroup, false);
            }

            // Va chercher les éléments
            final EditText tv = view.findViewById(R.id.listviewelement_answer_edit_text);
            RadioButton radio = view.findViewById(R.id.listviewelement_answer_edit_radio);
            ImageButton delete = view.findViewById(R.id.listviewelement_answer_edit_delete);

            // Écrit le contenu du choix de réponse
            tv.setText("☭"); // On utilise ça pour avertir le listener de s'enlever
            tv.setText(choices.get(i));

            // Coche le bouton radio si nécéssaire
            if (answer == i) {
                radio.setChecked(true);
            } else {
                radio.setChecked(false);
            }

            // Ajoute des Listener pour la zone de texte, le bouton et le bouton radio
            tv.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i_prim, int i1, int i2) {
                    if (charSequence.toString().equals("☭")) {
                        tv.removeTextChangedListener(this);
                        return;
                    }
                    choices.set(i, charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            radio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    answer = i;
                    notifyDataSetChanged();
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    choices.remove(i);
                    notifyDataSetChanged();
                }
            });

            return view;
        }
    }
}
