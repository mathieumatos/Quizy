package com.quizy.quizy.db;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class JSONParser {

    /**
     * Méthode statique traduisant du JSON en listes de <code>Category</code>, <code>Quiz</code> et <code>Question</code>.
     *
     * @param json       Le <code>String</code> contenant le JSON.
     * @param categories Une liste où stocker les catégories.
     * @param quizes     Une liste où stocker les quiz.
     * @param questions  Une liste où stocker les questions.
     */
    public static void jsonToData(String json, List<Category> categories, List<Quiz> quizes, List<Question> questions) {
        try {
            JSONObject jData = new JSONObject(json);
            JSONArray jCategories = jData.getJSONArray("categories");
            for (int i = 0; i < jCategories.length(); i++) {
                // For each category
                parseCategory(jCategories.getJSONObject(i), categories, quizes, questions);
            }
        } catch (Exception e) {
            Log.e("ERROR", "Error reading JSON : " + e);
        }
    }

    private static void parseCategory(JSONObject jCategory, List<Category> categories, List<Quiz> quizes, List<Question> questions) throws JSONException {
        Category category = new Category(jCategory.getString("name"), new ArrayList<Quiz>());
        JSONArray jQuizes = jCategory.getJSONArray("quizes");
        for (int i = 0; i < jQuizes.length(); i++) {
            // For each quiz in category
            parseQuiz(jQuizes.getJSONObject(i), category, quizes, questions);
        }
        categories.add(category);
    }

    private static void parseQuiz(JSONObject jQuiz, Category category, List<Quiz> quizes, List<Question> questions) throws JSONException {
        Quiz.QuizType type = (jQuiz.getString("type").equals("choices")) ? Quiz.QuizType.CHOICES : Quiz.QuizType.TEXT;
        JSONArray jResults = jQuiz.getJSONArray("results");
        List<Double> results = new ArrayList<>();
        for (int i = 0; i < jResults.length(); i++) {
            results.add(jResults.getDouble(i));
        }
        Quiz quiz = new Quiz(category, jQuiz.getString("title"), type, new ArrayList<Question>(), results);
        JSONArray jQuestions = jQuiz.getJSONArray("questions");
        for (int i = 0; i < jQuestions.length(); i++) {
            // For each question in quiz
            parseQuestion(jQuestions.getJSONObject(i), quiz, questions);
        }
        quizes.add(quiz);
        category.quizes.add(quiz);
    }

    private static void parseQuestion(JSONObject jQuestion, Quiz quiz, List<Question> questions) throws JSONException {
        Question question;
        if (quiz.type == Quiz.QuizType.CHOICES) {
            question = new QuestionChoice(jQuestion.getString("question"), new ArrayList<String>(), jQuestion.getInt("answer"));
            JSONArray jChoices = jQuestion.getJSONArray("choices");
            for (int i = 0; i < jChoices.length(); i++) {
                // For each choice in question with choices
                ((QuestionChoice) question).choices.add(jChoices.getString(i));
            }
        } else {
            question = new QuestionText(jQuestion.getString("question"), jQuestion.getString("answer"));
        }
        question.quiz = quiz;
        questions.add(question);
        quiz.questions.add(question);
    }

    /**
     * Méthode statique convertissant une list de <code>Category</code> en JSON.
     *
     * @param categories Liste de <code>Category</code>
     * @return Chaine de caractère en format JSON représentant les données.
     */
    public static String dataToJson(List<Category> categories) {
        try {
            JSONObject jData = new JSONObject();
            JSONArray jCategories = new JSONArray();
            jData.put("categories", jCategories);
            for (int i = 0; i < categories.size(); i++) {
                // For each category
                categoryToJson(jCategories, categories.get(i));
            }
            return jData.toString();
        } catch (Exception e) {
            Log.e("ERROR", "Error writing JSON : " + e);
        }
        return null;
    }

    private static void categoryToJson(JSONArray jCategories, Category category) throws JSONException {
        JSONObject jCategory = new JSONObject();
        JSONArray jQuizes = new JSONArray();
        jCategories.put(jCategory);
        jCategory.put("name", category.name);
        jCategory.put("quizes", jQuizes);
        for (int i = 0; i < category.quizes.size(); i++) {
            // For each quiz
            quizToJson(jQuizes, category.quizes.get(i));
        }
    }

    private static void quizToJson(JSONArray jQuizes, Quiz quiz) throws JSONException {
        JSONObject jQuiz = new JSONObject();
        JSONArray jQuestions = new JSONArray();
        JSONArray jResults = new JSONArray();
        jQuizes.put(jQuiz);
        jQuiz.put("title", quiz.title);
        jQuiz.put("type", ((quiz.type == Quiz.QuizType.CHOICES) ? ("choices") : ("text")));
        jQuiz.put("questions", jQuestions);
        jQuiz.put("results", jResults);
        for (int i = 0; i < quiz.results.size(); i++) {
            jResults.put(quiz.results.get(i));
        }
        for (int i = 0; i < quiz.questions.size(); i++) {
            // For each question
            questionToJson(jQuestions, quiz.questions.get(i), quiz.type);
        }
    }

    private static void questionToJson(JSONArray jQuestions, Question question, Quiz.QuizType type) throws JSONException {
        JSONObject jQuestion = new JSONObject();
        jQuestion.put("question", question.question);
        jQuestions.put(jQuestion);
        if (type == Quiz.QuizType.CHOICES) {
            jQuestion.put("answer", ((QuestionChoice) question).answer);
            JSONArray jChoices = new JSONArray();
            jQuestion.put("choices", jChoices);
            for (int l = 0; l < ((QuestionChoice) question).choices.size(); l++) {
                jChoices.put(((QuestionChoice) question).choices.get(l));
            }
        } else {
            jQuestion.put("answer", ((QuestionText) question).answer);
        }
    }

    // Nous créons un constructeur privé afin d'éviter d'instancier par erreur un JSONParser
    private JSONParser() {
    }
}
