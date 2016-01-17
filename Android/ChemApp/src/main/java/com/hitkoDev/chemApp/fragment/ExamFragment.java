/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import com.hitkoDev.chemApp.rest.SendJSONDataTask;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class ExamFragment extends ExercisesFragment {

    private double totalScore = 0;
    private double[] scores = {};
    private boolean started = false;
    private int correctColor = 0;
    private int incorrectColor = 0;
    private int partiallyCorrectColor = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState); //To change body of generated methods, choose Tools | Templates.
        correctColor = ContextCompat.getColor(getContext(), R.color.correct);
        incorrectColor = ContextCompat.getColor(getContext(), R.color.incorrect);
        partiallyCorrectColor = ContextCompat.getColor(getContext(), R.color.partially_correct);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswers();
            }
        });
        if (listener != null) {
            listener.setFragmentDescription("Test");
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (started) {
            showExercise(exerciseNumber);
        } else if (progressListener != null) {
            settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
            prefEditor = settings.edit();
            totalScore = 0;
            JSONObject o = new JSONObject();
            JSONArray a = new JSONArray();
            for (int i : progressListener.getUnlocked()) {
                a.put(i);
            }
            try {
                o.put("max", 15);
                o.put("sections", a);
                o.put("level", Integer.parseInt(settings.getString("level", "0")));
                new SendJSONDataTask(getContext(), new OnJSONResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            parseExercises(response.getJSONObject("object").getJSONArray("exercises"));
                        } catch (Exception ex) {

                        }
                        scores = new double[exercises.size()];
                        if (!exercises.isEmpty()) {
                            started = true;
                            showExercise(0);
                        }
                    }

                    @Override
                    public void onFail(String response) {
                        System.out.println(response);
                    }
                }).execute(o.toString(), "exam");
            } catch (Exception ex) {

            }
        }
    }

    @Override
    public void onValidated(String status, double score) {
        super.onValidated(status, score);
        scores[exerciseNumber] = score;
        totalScore += score;
        if (exerciseNumber + 1 < exercises.size()) {
            showExercise(exerciseNumber + 1);
        } else {
            action.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
            sections.setVisibility(View.GONE);
            started = false;
            adapter = new ResultAdapter(totalScore, scores);
            layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void showExercise(int ex) {
        super.showExercise(ex); //To change body of generated methods, choose Tools | Templates.
        action.setVisibility(View.GONE);
        next.setVisibility(View.VISIBLE);
        sections.setVisibility(View.GONE);
    }

    class ResultHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView icon;
        TextView content;
        TextView exercise;
        TextView score;
        LinearLayout wrap;

        public ResultHolder(View v) {
            super(v);
            view = v;
            content = (TextView) v.findViewById(R.id.exercise_text);
            exercise = (TextView) v.findViewById(R.id.exercise_explanation_answer);
            score = (TextView) v.findViewById(R.id.exercise_score);
            wrap = (LinearLayout) v.findViewById(R.id.exercise_answer_explanation);
            icon = (ImageView) v.findViewById(R.id.exercise_valid_icon);
        }

    }

    class ResultAdapter extends RecyclerView.Adapter<ResultHolder> {

        private final double score;
        private final double[] partialScores;

        public ResultAdapter(double score, double[] partialScores) {
            this.score = score;
            this.partialScores = partialScores;
        }

        @Override
        public ResultHolder onCreateViewHolder(ViewGroup vg, int i) {
            View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.exam_result, vg, false);
            ResultHolder rh = new ResultHolder(v);
            return rh;
        }

        @Override
        public void onBindViewHolder(ResultHolder vh, int i) {
            if (i == 0) {
                System.out.println(String.format("%.2f / %d (%.2f%%)", score, partialScores.length, (100.0 * score) / ((double) partialScores.length)));
            }
            vh.wrap.setVisibility(View.VISIBLE);
            vh.content.setVisibility(View.GONE);
            vh.exercise.setText("Naloga " + (i + 1));
            vh.score.setText(String.format("%.2f / 1", partialScores[i]));
            if (Math.abs(1 - partialScores[i]) < 0.001) {
                vh.icon.setImageResource(R.drawable.ic_correct);
                vh.score.setTextColor(correctColor);
                vh.exercise.setTextColor(correctColor);
            } else if (Math.abs(0 - partialScores[i]) < 0.001) {
                vh.icon.setImageResource(R.drawable.ic_incorrect);
                vh.score.setTextColor(incorrectColor);
                vh.exercise.setTextColor(incorrectColor);
            } else {
                vh.icon.setImageResource(R.drawable.ic_partially_correct);
                vh.score.setTextColor(partiallyCorrectColor);
                vh.exercise.setTextColor(partiallyCorrectColor);
            }
        }

        @Override
        public int getItemCount() {
            return partialScores.length;
        }

    }

}
