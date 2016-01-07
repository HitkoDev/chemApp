/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.MainActivity;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.data.Exercise;
import com.hitkoDev.chemApp.data.LoadedDrawable;
import com.hitkoDev.chemApp.rest.LoadDataTask;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class ExerciseFragment extends Fragment {

    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private StaggeredGridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    
    private int exerciseNumber = -1;
    private ArrayList<Exercise> exercises = new ArrayList();
    private int loadedSection;
    private Exercise exercise;
    private FloatingActionButton action;
    private RecyclerView.Adapter adapter;
    
    private int marginFull = 0;
    private int marginItems = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.exercise_content, container, false);
        
        settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
        prefEditor = settings.edit();
        settings.getInt("section", 0);
        
        action = (FloatingActionButton) v.findViewById(R.id.exercise_action);
        action.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswers();
            }
        });
        
        recyclerView = (RecyclerView) v.findViewById(R.id.exercise_recycler_view);
        
        loadExercises(settings.getInt("section", 0));
        
        marginFull = Math.round(getContext().getResources().getDimension(R.dimen.activity_vertical_margin));
        marginItems = Math.round(getContext().getResources().getDimension(R.dimen.activity_vertical_margin)/2);
        
        return v;
    }

    private void loadExercises(final int section) {
        exerciseNumber = -1;
        if(section > 0){
            if(section != loadedSection) {
                new LoadDataTask(getContext(), new OnJSONResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            JSONArray l = response.getJSONObject("object").getJSONArray("exercises");
                            exercises.clear();
                            for(int i = 0; i < l.length(); i++){
                                try {
                                    final int index = exercises.size();
                                    Exercise sl = null;
                                    switch(l.getJSONObject(i).getString("class_key")){
                                        case "caMultiselectQuestion":
                                            sl = new Exercise.Select(l.getJSONObject(i), getContext(), new LoadedDrawable.OnDrawableUpdatedListener(){
                                                @Override
                                                public void onDrawableUpdated() {
                                                    if(adapter != null && index == exerciseNumber) adapter.notifyItemChanged(0);
                                                }
                                            }, new Exercise.OnFieldUpdatedListener() {
                                                @Override
                                                public void OnFieldUpdated(int n) {
                                                    if(adapter != null && index == exerciseNumber) adapter.notifyItemChanged(n+1);
                                                }
                                            });
                                            break;
                                        case "caInputQuestion":
                                            sl = new Exercise.Input(l.getJSONObject(i), getContext(), new LoadedDrawable.OnDrawableUpdatedListener(){
                                                @Override
                                                public void onDrawableUpdated() {
                                                    if(adapter != null && index == exerciseNumber) adapter.notifyItemChanged(0);
                                                }
                                            }, new Exercise.OnFieldUpdatedListener() {
                                                @Override
                                                public void OnFieldUpdated(int n) {
                                                    if(adapter != null && index == exerciseNumber) adapter.notifyItemChanged(n+1);
                                                }
                                            });
                                            break;
                                    }
                                    if(sl != null) exercises.add(sl);
                                } catch (JSONException ex) {
                                    Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                                }
                            }
                        } catch (JSONException ex) {
                            Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                        loadedSection = section;
                        if(!exercises.isEmpty()) showExercise(0);
                    }

                    @Override
                    public void onFail(String response) {
                        System.out.println(response);
                    }
                }).execute("section", section+"");
            } else {
                if(!exercises.isEmpty()) showExercise(0);
            }
        }
    }
    
    public void checkAnswers() {
        if(exerciseNumber + 1 < exercises.size()) showExercise(exerciseNumber + 1);
    }
    
    public void showExercise(int ex){
        
        exerciseNumber = ex;
        exercise = exercises.get(exerciseNumber);
        
        if(exercise.getClass() == Exercise.Select.class){
            adapter = new AnswerAdapter(exercise);
        } else if(exercise.getClass() == Exercise.Input.class){
            adapter = new InputAdapter(exercise);
        } 
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
    
    public class AnswerHolder extends RecyclerView.ViewHolder {
        
        CompoundButton answer;
        View view;
        TextView content;
        Exercise.Select.Answer ans;
        LinearLayout wrap;
        
        public AnswerHolder(View v, final Exercise.Select ex) {
            super(v);
            view = v;
            wrap = (LinearLayout) v.findViewById(R.id.exercise_answer_wrap);
            answer = (CompoundButton) v.findViewById(R.id.exercise_answer);
            answer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(ans != null) ans.selected = isChecked;
                    if(isChecked && !ex.isMultiselect()) {
                        int i = 0;
                        for(Exercise.Select.Answer a : ex.getAnswers()){
                            if(a.selected && a != ans){
                                a.selected = false;
                                if(adapter != null) adapter.notifyItemChanged(i+1);
                            }
                            i++;
                        }
                    }
                }
            });
            content = (TextView) v.findViewById(R.id.exercise_text);
        }
        
    }
    
    class AnswerAdapter extends RecyclerView.Adapter<AnswerHolder> {
        
        private Exercise.Select exercise;
        private StaggeredGridLayoutManager.LayoutParams params;

        private AnswerAdapter(Exercise ex) {
            super();
            exercise = (Exercise.Select)ex;
        }

        @Override
        public AnswerHolder onCreateViewHolder(ViewGroup vg, int i) {
            View v = LayoutInflater.from(vg.getContext()).inflate(exercise.isMultiselect() ? R.layout.exercise_checkbox : R.layout.exercise_radio, vg, false);
            AnswerHolder vh = new AnswerHolder(v, exercise);
            return vh;
        }

        @Override
        public void onBindViewHolder(AnswerHolder vh, final int i) {
            params = new StaggeredGridLayoutManager.LayoutParams(vh.view.getLayoutParams());
            params.setFullSpan(i == 0);
            vh.view.setLayoutParams(params);
            if(params.isFullSpan()){
                vh.content.setText(exercise.getContentParsed());
                vh.ans = null;
                vh.content.setVisibility(View.VISIBLE);
                vh.answer.setVisibility(View.GONE);
                
                vh.wrap.setPadding(marginFull, marginFull, marginFull, marginItems);
                
            } else {
                Exercise.Select.Answer a = exercise.getAnswers().get(i-1);
                vh.ans = a;
                vh.answer.setText(a.getAnswerParsed());
                vh.answer.setChecked(a.selected);
                vh.answer.setVisibility(View.VISIBLE);
                vh.content.setVisibility(View.GONE);
                
                vh.wrap.setPadding(marginFull, marginItems, marginFull, i == exercise.getAnswers().size() ? marginFull : marginItems);
            }
        }

        @Override
        public int getItemCount() {
            return exercise.getAnswers().size() + 1;
        }
        
    }
    
    class InputHolder extends RecyclerView.ViewHolder {
        
        View view;
        TextView content;
        EditText input;
        Exercise.Input.Field field;
        LinearLayout wrap;
        
        public InputHolder(View v, final Exercise.Input ex) {
            super(v);
            view = v;
            input = (EditText) v.findViewById(R.id.exercise_input);
            content = (TextView) v.findViewById(R.id.exercise_text);
            wrap = (LinearLayout) v.findViewById(R.id.exercise_input_wrap);
            input.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(field != null) field.value = input.getText().toString();
                }
            });
        }
        
    }
    
    class InputAdapter extends RecyclerView.Adapter<InputHolder> {
        
        private Exercise.Input exercise;
        private StaggeredGridLayoutManager.LayoutParams params;

        private InputAdapter(Exercise ex) {
            super();
            exercise = (Exercise.Input)ex;
        }
        
        private OnEditorActionListener done = new OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                checkAnswers();
                return false;
            }
            
        };

        @Override
        public InputHolder onCreateViewHolder(ViewGroup vg, int i) {
            View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.exercise_input, vg, false);
            InputHolder vh = new InputHolder(v, exercise);
            return vh;
        }

        @Override
        public void onBindViewHolder(InputHolder vh, final int i) {
            params = new StaggeredGridLayoutManager.LayoutParams(vh.view.getLayoutParams());
            params.setFullSpan(i == 0);
            if(params.isFullSpan()){
                vh.content.setText(exercise.getContentParsed());
                vh.content.setVisibility(View.VISIBLE);
                vh.input.setVisibility(View.GONE);
                
                vh.wrap.setPadding(marginFull, marginFull, marginFull, marginItems);
                
                vh.field = null;
            } else {
                Exercise.Input.Field fl = exercise.getFields().get(i-1);
                vh.field = fl;
                vh.input.setHint(fl.getLabelParsed() == null ? "" : fl.getLabelParsed());
                vh.input.setVisibility(View.VISIBLE);
                vh.input.setText(fl.value);
                
                if(i == exercise.getFields().size()){
                    vh.input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    vh.input.setOnEditorActionListener(done);
                    vh.wrap.setPadding(marginFull, marginItems, marginFull, marginFull);
                } else {
                    layoutManager.scrollToPosition(i+1);
                    vh.input.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    vh.input.setOnEditorActionListener(null);
                    vh.wrap.setPadding(marginFull, marginItems, marginFull, marginItems);
                }
                vh.content.setVisibility(View.GONE);
            }
            vh.view.setLayoutParams(params);
        }

        @Override
        public int getItemCount() {
            return exercise.getFields().size() + 1;
        }
        
    }
    
}
