package com.quizy.quizy;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.quizy.quizy.db.Quiz;

import java.util.ArrayList;
import java.util.List;

/**
 * L'Activity principale, la liste de tout les quiz.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Référence au <code>Data</code> global.
     */
    Data db;

    /**
     * Le dialog de création de quiz.
     */
    AlertDialog newQuizDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Créer le dialog de création de quiz pour pouvoir l'utiliser plus tard.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type de quiz")
                .setItems(new String[]{"Choix de réponse", "Texte"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Quiz newQuiz;
                        if(which == 0) {
                            newQuiz = db.newQuiz(Quiz.QuizType.CHOICES, "Nouveau Quiz");
                        } else {
                            newQuiz = db.newQuiz(Quiz.QuizType.TEXT, "Nouveau Quiz");
                        }
                        newQuizDialog.dismiss();
                        editQuiz(newQuiz);
                    }
                });
        newQuizDialog = builder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();

        db = Data.getInstance(this);
        final List<Category> categories = db.getCategories();

        // Regarder si on a un Intent avec des Extras
        // Si c'est le cas, c'est que c'est une redirection vers la page de statistique d'un quiz.
        Intent myIntent = getIntent();
        int quizStatsIndex = myIntent.getIntExtra("quizStatsIndex", -1);
        if (quizStatsIndex != -1) {
            Quiz q = db.getQuizes().get(quizStatsIndex);
            statsQuiz(q);
            return;
        }

        // Écrire la liste des quiz
        ListView listView = findViewById(R.id.main_list_quiz);
        final ListViewAdapter listAdapter = new ListViewAdapter();
        listView.setAdapter(listAdapter);

        // Ajouter un Listener sur la barre de recherche
        EditText searchbar = findViewById(R.id.main_search);
        searchbar.setText("");
        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                listAdapter.changeSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Écrire la liste des catégories
        List<String> categoriesNamesList = new ArrayList<>();
        categoriesNamesList.add("Toute catégories");
        for (int i = 0; i < categories.size(); i++) {
            if (!categories.get(i).name.equals("")) {
                categoriesNamesList.add(categories.get(i).name);
            }
        }
        String[] categoriesNames = new String[categoriesNamesList.size()];
        categoriesNames = categoriesNamesList.toArray(categoriesNames);
        Spinner spinner = findViewById(R.id.main_category);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoriesNames);
        spinner.setAdapter(spinnerAdapter);

        // Ajouter un Listener sur le Spinner de catégories
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                listAdapter.changeCategory(categories.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                listAdapter.changeCategory(null);
            }
        });
    }

    /**
     * Créer un nouveau quiz.
     *
     * @param view View donné par un bouton.
     */
    public void newQuiz(View view) {
        newQuizDialog.show();
    }

    /**
     * Jouer à un quiz.
     *
     * @param quiz Le quiz auquel jouer.
     */
    void playQuiz(Quiz quiz) {
        Intent myIntent;
        if (quiz.type == Quiz.QuizType.CHOICES) {
            myIntent = new Intent(this, AnswerChoiceActivity.class);
        } else {
            myIntent = new Intent(this, AnswerTextActivity.class);
        }
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        myIntent.putExtra("questionIndex", db.getQuestions().indexOf(quiz.questions.get(0))); // Start first question
        startActivity(myIntent);
    }

    /**
     * Aller regarder les statistiques d'un quiz.
     *
     * @param quiz Le quiz duquel afficher les statistiques.
     */
    void statsQuiz(Quiz quiz) {
        Intent myIntent = new Intent(this, StatsActivity.class);
        myIntent.putExtra("quizIndex", db.getQuizes().indexOf(quiz));
        startActivity(myIntent);
    }

    /**
     * Aller modifier un quiz.
     *
     * @param quiz Le quiz à modifier.
     */
    void editQuiz(Quiz quiz) {
        Intent myIntent = new Intent(this, EditQuizActivity.class);
        myIntent.putExtra("quizIndex", db.getQuizes().indexOf(quiz));
        startActivity(myIntent);
    }

    /**
     * L'Adapter de la liste de quiz.
     */
    private class ListViewAdapter extends BaseAdapter {

        LayoutInflater inflater;

        /**
         * La liste de tout les quiz.
         */
        List<Quiz> allQuizes;

        /**
         * La liste de tout les quiz affichées (selon les filtres de recherche).
         */
        List<Quiz> displayedQuizes;

        /**
         * Le terme de la recherche.
         */
        String searchTerm = "";

        /**
         * La catégorie présentement sélectionnée.
         */
        Category category = null;

        /**
         * Change le terme de la recherche.
         *
         * @param search Nouveau terme de la recherche.
         */
        void changeSearch(String search) {
            searchTerm = search;
            updateList();
        }

        /**
         * Changer la catégorie dans laquelle chercher.
         *
         * @param category La nouvelle catégorie à chercher.
         */
        void changeCategory(Category category) {
            this.category = category;
            updateList();
        }

        /**
         * Mettre à jour la liste de quiz à afficher.
         */
        private void updateList() {
            displayedQuizes.clear();

            for (Quiz q : allQuizes) {
                if ((category == null || category.name.equals("") || q.category == category) && q.title.contains(searchTerm))
                    displayedQuizes.add(q);
            }

            notifyDataSetChanged();
        }

        ListViewAdapter() {
            inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            Data db = Data.getInstance(getApplicationContext());
            allQuizes = db.getQuizes();
            displayedQuizes = new ArrayList<>();
            updateList();
        }

        @Override
        public int getCount() {
            return displayedQuizes.size();
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
                view = inflater.inflate(R.layout.listviewelement_quiz, viewGroup, false);
            }

            // Aller chercher les éléments
            TextView title = view.findViewById(R.id.listviewelement_quiz_title);
            TextView category = view.findViewById(R.id.listviewelement_quiz_category);
            TextView nb_questions = view.findViewById(R.id.listviewelement_quiz_nb_questions);
            Button playButton = view.findViewById(R.id.listviewelement_quiz_play);
            ImageButton statsButton = view.findViewById(R.id.listviewelement_quiz_stats);
            ImageButton editButton = view.findViewById(R.id.listviewelement_quiz_edit);

            // Écrire les informations sur le quiz
            title.setText(displayedQuizes.get(i).title);
            if (displayedQuizes.get(i).category.name.equals("")) {
                category.setText("Aucune catégorie");
            } else {
                category.setText(displayedQuizes.get(i).category.name);
            }
            nb_questions.setText(displayedQuizes.get(i).questions.size() + " questions");

            // Ajoute des listeners sur les boutons
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playQuiz(displayedQuizes.get(i));
                }
            });
            statsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    statsQuiz(displayedQuizes.get(i));
                }
            });
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editQuiz(displayedQuizes.get(i));
                }
            });

            return view;
        }
    }
}