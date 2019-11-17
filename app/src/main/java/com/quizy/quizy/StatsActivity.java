package com.quizy.quizy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.quizy.quizy.db.Data;
import com.quizy.quizy.db.Quiz;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * L'Activity pour afficher les statistique sur un quiz.
 */
public class StatsActivity extends AppCompatActivity {

    /**
     * Référence au <code>Data</code> global.
     */
    Data db;

    /**
     * Le quiz à partir duquel afficher les statistiques.
     */
    Quiz quiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        db = Data.getInstance(this);

        // Aller chercher les informations du Intent
        Intent myIntent = getIntent();
        int quizIndex = myIntent.getIntExtra("quizIndex", 0);

        // Aller chercher le quiz et les résultats
        quiz = db.getQuizes().get(quizIndex);
        List<Double> results = quiz.results;

        // Aller chercher les éléments
        TextView title = findViewById(R.id.activity_stats_title);
        TextView category = findViewById(R.id.activity_stats_category);
        BarChart chart = findViewById(R.id.activity_stats_chart);
        TextView average = findViewById(R.id.activity_stats_average);
        TextView median = findViewById(R.id.activity_stats_median);
        TextView best = findViewById(R.id.activity_stats_best);

        // Écrire le titre et la catégorie du quiz
        title.setText(quiz.title);
        category.setText(quiz.category.name);

        // Initialiser les paramètres du diagramme
        chart.setDrawBarShadow(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(true);
        chart.getAxisLeft().setValueFormatter(new PercentFormatter());
        chart.getAxisLeft().setAxisMaximum(100f);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.setTouchEnabled(false);

        // Ajouter les données au diagramme
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            entries.add(new BarEntry(i, results.get(i).floatValue() * 100));
        }
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setValueTextSize(14);
        BarData barData = new BarData(dataSet);
        chart.setData(barData);
        chart.invalidate();

        // Écrire certaines statistiques de base
        average.setText("Moyenne : " + average(results).toString() + "%");
        median.setText("Médiane : " + median(results).toString() + "%");
        best.setText("Meilleur : " + best(results).toString() + "%");
    }

    /**
     * Efface tout les résultats sauvegarder jusqu'à maintenant.
     *
     * @param view View donné par un bouton.
     */
    public void resetStats(View view) {
        quiz.results.clear();
        db.save(this);

        // Refresh view
        finish();
        startActivity(getIntent());
    }

    /**
     * Calcul la moyenne d'une liste de valeurs.
     *
     * @param values La liste de valeurs.
     * @return La moyenne de la liste de valeurs.
     */
    private Double average(List<Double> values) {
        if (values.size() == 0)
            return (double)0;

        double sum = 0;
        for (Double d : values) {
            sum += d;
        }

        double f = (sum / values.size()) * 100;
        return (Math.round(f*100)/100.0d);
    }

    /**
     * Calcul la médianne d'une liste de valeurs.
     *
     * @param values La liste de valeurs.
     * @return La médianne de la liste de valeurs.
     */
    private Double median(List<Double> values) {
        if (values.size() == 0)
            return (double)0;

        ArrayList<Double> copy = new ArrayList<>(values);

        Collections.sort(copy);

        if (copy.size() % 2 == 0) {
            return (copy.get(copy.size() / 2) + copy.get(copy.size() / 2 - 1)) / 2;
        }
        double f = copy.get(copy.size() / 2) * 100;
        return (Math.round(f*100)/100.0d);
    }

    /**
     * Trouve la plus haut valeur d'une liste de valeurs.
     *
     * @param values La liste de valeurs.
     * @return La plus haute valeur de la liste de valeurs.
     */
    private Double best(List<Double> values) {
        if (values.size() == 0)
            return (double)0;

        double best = 0;
        for (Double d : values) {
            if (d > best)
                best = d;
        }

        double f = best * 100;
        return (Math.round(f*100)/100.0d);
    }
}
