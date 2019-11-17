package com.quizy.quizy.db;

import android.content.Context;

import com.quizy.quizy.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Une classe singleton qui sert à stocker toutes les données des quiz.
 */
public class Data {
    private static Data _instance;

    /**
     * Va chercher l'instance courante de Data.
     *
     * @param ctx Un context pour permettre l'accès au système de fichier pour charger les données si nécéssaire.
     * @return L'instance de Data
     */
    public static Data getInstance(Context ctx) {
        if (Data._instance == null) {
            Data._instance = new Data(ctx);
        }
        return Data._instance;
    }

    /**
     * Une liste de toute les catégories.
     */
    private List<Category> categories;

    /**
     * Une liste de tout les quiz.
     */
    private List<Quiz> quizes;

    /**
     * Une liste de toute les questions.
     */
    private List<Question> questions;

    private Data(Context ctx) {
        categories = new ArrayList<>();
        quizes = new ArrayList<>();
        questions = new ArrayList<>();

        load(ctx);
    }

    /**
     * @return La liste de toutes les catégories.
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * @return La liste de tout les quiz.
     */
    public List<Quiz> getQuizes() {
        return quizes;
    }

    /**
     * @return La liste de toutes les questions.
     */
    public List<Question> getQuestions() {
        return questions;
    }

    /**
     * Déplace un quiz dans une nouvelle catégorie.
     *
     * @param quiz     Le quiz à changer de catégorie.
     * @param category La nouvelle catégorie dans laquelle mettre le quiz.
     */
    public void moveQuizToCategory(Quiz quiz, Category category) {
        quiz.category.quizes.remove(quiz);
        category.quizes.add(quiz);
        quiz.category = category;
    }

    /**
     * Supprimer une question et toutes ses références.
     *
     * @param question La questions à supprimer.
     */
    public void delete(Question question) {
        question.quiz.questions.remove(question);
        questions.remove(question);
    }

    /**
     * Supprimer un quiz et toutes ses références.
     *
     * @param quiz Le quiz à supprimer
     */
    public void delete(Quiz quiz) {
        for (Question q : quiz.questions) {
            questions.remove(q);
        }
        quiz.questions.clear();
        quiz.category.quizes.remove(quiz);
        quizes.remove(quiz);
    }

    /**
     * Créer un nouveau quiz. Ce quiz sera ajouté à aucune catégorie pour commencer.
     *
     * @param type  Le type de quiz à créer.
     * @param title Le titre du quiz à créer.
     * @return Le nouveau quiz créé.
     */
    public Quiz newQuiz(Quiz.QuizType type, String title) {
        Category cat = categories.get(0);
        Quiz quiz = new Quiz(cat, title, type, new ArrayList<Question>(), new ArrayList<Double>());
        cat.quizes.add(quiz);
        quizes.add(quiz);
        return quiz;
    }

    /**
     * Créer une nouvelle question à un quiz.
     *
     * @param quiz Le quiz dans lequel rajouter une questions.
     * @return La question créée.
     */
    public Question newQuestion(Quiz quiz) {
        Question question;
        if (quiz.type == Quiz.QuizType.CHOICES) {
            question = new QuestionChoice("", new ArrayList<String>(), 0);
        } else {
            question = new QuestionText("", "");
        }
        quiz.questions.add(question);
        question.quiz = quiz;
        questions.add(question);
        return question;
    }

    /**
     * Sauvegarde l'état actuelle de Data sur le disque.
     *
     * @param ctx Un contexte pour pouvoir accéder au système de fichier.
     */
    public void save(Context ctx) {
        String json = JSONParser.dataToJson(categories);
        writeFile(ctx, json);
    }

//    DEBUG : Sert à reset le fichier de sauvegarde au fichier par défaut.
//
//    public void load(Context ctx) {
//        copyDefaultFile(ctx);
//        String json = readFile(ctx);
//        JSONParser.jsonToData(json, categories, quizes, questions);
//    }

    /**
     * Charge un nouvel état de Data en lisant le fichier sur le disque.
     *
     * @param ctx Un contexte pour pouvoir accéder au système de fichier.
     */
    public void load(Context ctx) {
        String json = readFile(ctx);
        if (json == null || json.equals("")) {
            // Si le fichier n'existe pas, copier un fichier JSON par défaut.
            copyDefaultFile(ctx);
            load(ctx);
            return;
        }
        JSONParser.jsonToData(json, categories, quizes, questions);
    }

    /**
     * Lis le fichier de sauvegarde du disque.
     *
     * @param ctx Un contexte pour pouvoir accéder au système de fichier.
     * @return La chaine de caractère correspondant au contenu du fichier.
     */
    private String readFile(Context ctx) {
        try {
            FileInputStream fis = ctx.openFileInput("data.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException fileNotFound) {
            return null;
        } catch (IOException ioException) {
            return null;
        }
    }

    /**
     * Écris une chaine de caractère sur le disque.
     *
     * @param ctx     Un contexte pour pouvoir accéder au système de fichier.
     * @param content La chaine de caractère à écrire dans le fichier.
     */
    private void writeFile(Context ctx, String content) {
        try {
            FileOutputStream fos = ctx.openFileOutput("data.json", Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Copie un fichier par défaut dans le fichier de sauvegarde.
     *
     * @param ctx Un contexte pour pouvoir accéder au système de fichier.
     */
    private void copyDefaultFile(Context ctx) {
        try {
            InputStream databaseInputStream = ctx.getResources().openRawResource(R.raw.exemple);

            InputStreamReader isr = new InputStreamReader(databaseInputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            writeFile(ctx, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
