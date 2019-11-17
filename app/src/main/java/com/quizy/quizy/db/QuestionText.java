package com.quizy.quizy.db;

/**
 * Une questions avec une réponse textuelle.
 */
public class QuestionText extends Question {
    /**
     * La réponse à la questions.
     */
    public String answer;

    public QuestionText(String question, String answer) {
        super(question);
        this.answer = answer;
    }
}
