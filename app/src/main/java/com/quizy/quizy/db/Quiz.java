package com.quizy.quizy.db;

import java.util.List;

/**
 * Un quiz
 */
public class Quiz {

    public Quiz(Category category, String title, QuizType type, List<Question> questions, List<Double> results) {
        this.category = category;
        this.title = title;
        this.type = type;
        this.questions = questions;
        this.results = results;
    }

    /**
     * Les types de quiz
     *  TEXT : Questions à réponses textuelles.
     *  CHOICES : Questions à choic de réponses.
     */
    public enum QuizType {
        TEXT,
        CHOICES
    }

    /**
     * La catégorie du quiz.
     */
    public Category category;

    /**
     * Le titre du quiz.
     */
    public String title;

    /**
     * Le type de quiz.
     */
    public QuizType type;

    /**
     * Liste des questions du quiz.
     */
    public List<Question> questions;

    /**
     * Liste des résultats du quiz de 0 à 1.
     */
    public List<Double> results;
}
