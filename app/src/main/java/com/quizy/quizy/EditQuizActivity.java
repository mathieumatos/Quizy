package com.quizy.quizy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.quizy.quizy.db.Category;
import com.quizy.quizy.db.Data;
import com.quizy.quizy.db.Question;
import com.quizy.quizy.db.Quiz;

import java.util.ArrayList;
import java.util.List;

/**
 * L'Activity pour modifier un quiz.
 */
public class EditQuizActivity extends AppCompatActivity {

    /**
     * Référence au <code>Data</code> global.
     */
    Data db;

    /**
     * Le quiz à modifier.
     */
    Quiz quiz;

    /**
     * Un LayoutInflater à réutiliser.
     */
    LayoutInflater inflater;

    /**
     * Le dialog pour modifier le titre et la catégorie du quiz.
     */
    AlertDialog editAlert;

    /**
     * Le dialog pour créer une nouvelle catégorie.
     */
    AlertDialog newCategoryAlert;

    /**
     * Le titre du quiz.
     */
    TextView title;

    /**
     * La catégorie du quiz
     */
    TextView cat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quiz);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        db = Data.getInstance(this);

        Intent myIntent = getIntent();
        int quizIndex = myIntent.getIntExtra("quizIndex", 0);
        quiz = db.getQuizes().get(quizIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Écrire le titre du quiz et sa catégorie
        title = findViewById(R.id.edit_quiz_title);
        title.setText(quiz.title);
        cat = findViewById(R.id.edit_quiz_category);
        cat.setText(quiz.category.name);

        // Écrire la liste des questions
        ListView listView = findViewById(R.id.edit_quiz_list_questions);
        ListViewAdapter listAdapter = new ListViewAdapter();
        listView.setAdapter(listAdapter);
    }

    /**
     * Créer une nouvelle question.
     *
     * @param view View donné par un bouton.
     */
    public void newQuestion(View view) {
        Question question = db.newQuestion(quiz);
        editQuestion(question);
    }

    /**
     * Ouvre le dialog d'édition de titre et de catégorie.
     *
     * @param view View donné par un bouton.
     */
    public void openEditDialog(View view) {
        // Créer un Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ViewGroup nullParent = null;
        final View dialogView = inflater.inflate(R.layout.alert_edit_quiz, nullParent);
        builder.setView(dialogView);

        // Écrire le titre courant du quiz.
        EditText editTitle = dialogView.findViewById(R.id.alert_edit_quiz_name);
        editTitle.setText(quiz.title);

        // Écrire la liste des catégories existantes
        final List<Category> categories = db.getCategories();
        List<String> categoriesNamesList = new ArrayList<>();
        categoriesNamesList.add("Aucune catégorie");
        for (int i = 0; i < categories.size(); i++) {
            if (!categories.get(i).name.equals("")) {
                categoriesNamesList.add(categories.get(i).name);
            }
        }
        categoriesNamesList.add("Nouvelle catégorie");
        String[] categoriesNames = new String[categoriesNamesList.size()];
        categoriesNames = categoriesNamesList.toArray(categoriesNames);
        Spinner spinner = dialogView.findViewById(R.id.alert_edit_quiz_category);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoriesNames);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(db.getCategories().indexOf(quiz.category));

        // Ajoute un Listener sur la liste de catégories pour la sélection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i >= categories.size()) { // Si c'est une nouvelle catégorie.
                    openNewCategoryDialog(view);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Ajoute des listeners pour les boutons du dialog
        dialogView.findViewById(R.id.alert_edit_quiz_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Enregistre le nouveau titre
                EditText editTitle = dialogView.findViewById(R.id.alert_edit_quiz_name);
                quiz.title = editTitle.getText().toString();
                title.setText(quiz.title);

                // Enregistre la nouvelle catégorie
                Spinner categorySpinner = dialogView.findViewById(R.id.alert_edit_quiz_category);
                int catIndex = categorySpinner.getSelectedItemPosition();
                Category newCategory = categories.get(catIndex);
                db.moveQuizToCategory(quiz, newCategory);
                cat.setText(quiz.category.name);

                // Sauvegarde les changement sur le disque
                db.save(getApplicationContext());

                closeEditDialog();
            }
        });
        dialogView.findViewById(R.id.alert_edit_quiz_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeEditDialog();
            }
        });

        editAlert = builder.create();
        editAlert.show();
    }

    /**
     * Supprimer le quiz.
     *
     * @param view View donné par un bouton.
     */
    public void deleteQuiz(View view) {
        db.delete(quiz);
        db.save(this);
        finish();
    }

    /**
     * Ouvre le dialog de création de catégorie.
     *
     * @param view View donné par un bouton.
     */
    private void openNewCategoryDialog(View view) {
        // Créer un Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ViewGroup nullParent = null;
        final View dialogView = inflater.inflate(R.layout.alert_new_category, nullParent);
        builder.setView(dialogView);

        // Ajouter des listener sur les hboutons
        dialogView.findViewById(R.id.alert_new_category_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String categoryName = ((EditText) dialogView.findViewById(R.id.alert_new_category_name)).getText().toString();
                Category newCategory = new Category(categoryName, new ArrayList<Quiz>());
                db.getCategories().add(newCategory);
                db.save(getApplicationContext());
                closeNewCategoryDialog();
            }
        });
        dialogView.findViewById(R.id.alert_new_category_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeNewCategoryDialog();
            }
        });

        newCategoryAlert = builder.create();
        newCategoryAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                closeEditDialog();
                openEditDialog(null);
            }
        });
        newCategoryAlert.show();
    }

    /**
     * Modifier une question.
     *
     * @param question La questions à modifier.
     */
    public void editQuestion(Question question) {
        Intent myIntent;
        if (quiz.type == Quiz.QuizType.CHOICES)
            myIntent = new Intent(this, EditQuestionChoiceActivity.class);
        else
            myIntent = new Intent(this, EditQuestionTextActivity.class);

        myIntent.putExtra("questionIndex", db.getQuestions().indexOf(question));
        startActivity(myIntent);
    }

    /**
     * Ferme le dialog d'édition de titre et de catégorie.
     */
    private void closeEditDialog() {
        editAlert.dismiss();
    }

    /**
     * Ferme le dialog de nouvelle catégorie.
     */
    private void closeNewCategoryDialog() {
        newCategoryAlert.dismiss();
    }

    /**
     * L'Adapter pour la liste de questions.
     */
    private class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return quiz.questions.size();
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
                view = inflater.inflate(R.layout.listviewelement_question, viewGroup, false);
            }

            // Aller chercher les éléments
            TextView tv = view.findViewById(R.id.listviewelement_question_text);
            ImageButton edit = view.findViewById(R.id.listviewelement_question_edit);
            ImageButton delete = view.findViewById(R.id.listviewelement_question_delete);

            // Écrire la question
            tv.setText(quiz.questions.get(i).question);

            // Ajouter les Listener psur les boutons
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editQuestion(quiz.questions.get(i));
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.delete(quiz.questions.get(i));
                    db.save(getApplicationContext());

                    notifyDataSetChanged();
                }
            });

            return view;
        }
    }
}
