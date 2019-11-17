package com.quizy.quizy.db;

import java.util.List;

/**
 * Une catégorie de quiz
 */
public class Category {
    /**
     * Nom de la catégorie
     */
    public String name;

    /**
     * Liste des quiz faisant parties de la catégorie.
     */
    public List<Quiz> quizes;

    public Category(String name, List<Quiz> quizes) {
        this.name = name;
        this.quizes = quizes;
    }
}
