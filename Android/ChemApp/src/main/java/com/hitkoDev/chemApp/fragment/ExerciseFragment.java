/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.data.Exercise;
import com.hitkoDev.chemApp.data.Section;
import com.hitkoDev.chemApp.rest.LoadDataTask;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import com.hitkoDev.chemApp.tiles.ImageCanvas;
import com.hitkoDev.chemApp.tiles.LetterTileProvider;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class ExerciseFragment extends ExercisesFragment {

    private int loadedSection;
    private Section section;

    private ImageCanvas.Dimensions tileDimensions;
    private LetterTileProvider tileProvider;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        int d = getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
        tileDimensions = new ImageCanvas.Dimensions(d, d, 1, getResources().getDimensionPixelSize(R.dimen.tile_letter_font_size_medium));
        tileProvider = new LetterTileProvider(getContext());
        tileProvider.noCache = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        final int section = settings.getInt("section", 0);

        if (section > 0) {
            if (section != loadedSection) {
                exerciseNumber = -1;
                if (getContext() != null) {
                    new LoadDataTask(getContext(), new OnJSONResponseListener() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                ExerciseFragment.this.section = new Section(response.getJSONObject("object"), getContext(), new Section.OnIconLoaded() {
                                    @Override
                                    public void notifyLoaded(Section s) {
                                        setHeader();
                                    }
                                });
                                if (!ExerciseFragment.this.section.hasIcon()) {
                                    setHeader();
                                }
                                parseExercises(response.getJSONObject("object").getJSONArray("exercises"));
                            } catch (Exception ex) {
                            }
                            loadedSection = section;
                            if (!exercises.isEmpty()) {
                                showExercise(0);
                            }
                        }

                        @Override
                        public void onFail(String response) {
                            System.out.println(response);
                        }
                    }).execute("section", section + "");
                }
            } else {
                if (!exercises.isEmpty()) {
                    showExercise(exerciseNumber);
                }
                setHeader();
            }
        }
    }

    private void setHeader() {
        Resources.Theme th = getContext().getTheme();
        TypedValue val = new TypedValue();
        th.resolveAttribute(R.attr.colorPrimary, val, true);

        Bitmap b = section.loadedIcon() ? tileProvider.makeCircle(tileDimensions, section.getIcon()) : tileProvider.getLetterTile(tileDimensions, section.getName(), section.getId() + "");

        if (listener != null) {
            listener.setFragmentDescription(new ActivityManager.TaskDescription(section.getName(), b, val.data), section.getName());
        }
    }

    @Override
    public void onValidated(String status, double score) {
        super.onValidated(status, score);
        if (status.equals("correct")) {
            solved++;
            if (solved == exercises.size() && progressListener != null) {
                progressListener.setUnlocked(section.getId());
            }
        }
        if (exercise.getClass() == Exercise.Input.class) {
            Exercise.Input ex = (Exercise.Input) exercise;
            String expl = ex.getExplanationParsed().toString().trim();
            if (!expl.isEmpty()) {
                new AlertDialog.Builder(getContext())
                        .setMessage(ex.getExplanationParsed())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(status.equals("correct") ? R.drawable.ic_correct : (status.equals("incorrect") ? R.drawable.ic_incorrect : R.drawable.ic_partially_correct))
                        .show();
            }
        }
    }

}
