package com.quizy.quizy.db;

import java.util.List;

/**
 * Une question à choix de réponse.
 */
public class QuestionChoice extends Question {
    /**
     * Liste des choix de réponse.
     */
    public List<String> choices;
    /**
     * Index du choix de réponse dans <code>choices</code>
     */
    public int answer;

    public QuestionChoice(String question, List<String> choices, int answer) {
        super(question);
        this.choices = choices;
        this.answer = answer;
    }
}
