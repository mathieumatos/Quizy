package com.quizy.quizy.db;

/**
 * Une questions d'un quiz
 */
public abstract class Question {
    /**
     * Le quiz dans lequel la quesion est.
     */
    public Quiz quiz;
    /**
     * L'énoncé de la question.
     */
    public String question;

    public Question(String question) {
        this.question = question;
    }
}
