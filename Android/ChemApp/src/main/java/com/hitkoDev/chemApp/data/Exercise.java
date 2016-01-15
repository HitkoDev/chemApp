/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.data;

import android.content.Context;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import com.hitkoDev.chemApp.rest.SendJSONDataTask;
import com.hitkoDev.chemApp.rest.TextImageGetter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public abstract class Exercise {
    
    private int id;
    private String name;
    private String content;
    private String classKey;
    private Spanned contentParsed;
    private boolean validated = false;

    public boolean isValidated() {
        return validated;
    }
    
    public Exercise(JSONObject o) throws JSONException {
        if(o.has("id")) id = o.getInt("id");
        if(o.has("name")) name = o.getString("name");
        if(o.has("content")) content = o.getString("content");
        if(o.has("class_key")) classKey = o.getString("class_key");
        if(content != null) contentParsed = Lesson.centerImages(Html.fromHtml(content));
    }
    
    public Exercise(JSONObject o, Context c, LoadedDrawable.OnDrawableUpdatedListener l) throws JSONException {
        this(o);
        TextImageGetter g = new TextImageGetter(c, l);
        if(content != null) contentParsed = Lesson.centerImages(Html.fromHtml(content, g, null));
    }

    public Spanned getContentParsed() {
        return contentParsed;
    }
    
    public abstract String getPostString();
    
    public abstract void validate(Context c, OnExerciseValidatedListener l);
    
    public static class Select extends Exercise {
        
        private ArrayList<Answer> answers = new ArrayList();
        private boolean multiselect;

        public Select(JSONObject o, Context c, LoadedDrawable.OnDrawableUpdatedListener l, final OnFieldUpdatedListener fl) throws JSONException {
            super(o, c, l);
            if(o.has("multi")) multiselect = o.getBoolean("multi");
            JSONArray answ = o.getJSONArray("answers");
            for(int i = 0; i < answ.length(); i++){
                JSONObject a = answ.getJSONObject(i);
                final int index = answers.size();
                try(Answer ans = new Answer(a, c, new LoadedDrawable.OnDrawableUpdatedListener() {
                    @Override
                    public void onDrawableUpdated() {
                        if(fl != null) fl.OnFieldUpdated(index);
                    }
                })){
                    answers.add(ans);
                } catch(Exception ex) {
                    
                }
            }
        }

        public Select(JSONObject o) throws JSONException {
            super(o);
            if(o.has("multi")) multiselect = o.getBoolean("multi");
            JSONArray answ = o.getJSONArray("answers");
            for(int i = 0; i < answ.length(); i++){
                JSONObject a = answ.getJSONObject(i);
                try(Answer ans = new Answer(a)){
                    answers.add(ans);
                } catch(Exception ex) {
                    
                }
            }
        }

        public boolean isMultiselect() {
            return multiselect;
        }

        public ArrayList<Answer> getAnswers() {
            return answers;
        }

        @Override
        public String getPostString() {
            JSONObject o = new JSONObject();
            JSONObject f = new JSONObject();
            for(Answer a : answers) a.putData(f);
            
            try {
                o.put("id", super.id);
            } catch (JSONException ex) {
                Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                o.put("fields", f);
            } catch (JSONException ex) {
                Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return o.toString();
        }

        @Override
        public void validate(Context c, final OnExerciseValidatedListener l) {
            new SendJSONDataTask(c, new OnJSONResponseListener() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        JSONObject f = response.getJSONObject("object").getJSONObject("fields");
                        for(Answer a : answers){
                            try {
                                if(f.has(a.id + "")){
                                    a.correctAnswer = f.getBoolean(a.id + "");
                                    a.showExplanation = true;
                                } else {
                                    a.showExplanation = false;
                                }
                            } catch (JSONException ex) {
                                Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
                                a.showExplanation = false;
                            }
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Exercise.Select.super.validated = true;
                    try {
                        l.OnExerciseValidated(response.getJSONObject("object").getString("status"));
                    } catch (JSONException ex) {
                        l.OnExerciseValidated("");
                        Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public void onFail(String response) {
                    System.out.println(response);
                    l.OnExerciseValidated("");
                }
            }).execute(getPostString(), "validate");
        }
        
        public class Answer implements AutoCloseable {
            
            private int id;
            private String name;
            private String answer;
            private Spanned answerParsed;
            private String explanation;
            private Spanned explanationParsed;
            private String classKey;
            public boolean selected;
            public boolean showExplanation = false;
            public boolean correctAnswer = false;
            
            private Answer(JSONObject o) throws JSONException {
                if(o.has("id")) id = o.getInt("id");
                if(o.has("name")) name = o.getString("name");
                if(o.has("answer")) answer = o.getString("answer");
                if(o.has("explanation")) explanation = o.getString("explanation");
                if(o.has("class_key")) classKey = o.getString("class_key");
                if(answer != null) answerParsed = Lesson.centerImages(Html.fromHtml(answer));
                if(explanation != null) explanationParsed = Lesson.centerImages(Html.fromHtml(explanation));
            }

            private Answer(JSONObject o, Context c, LoadedDrawable.OnDrawableUpdatedListener l) throws JSONException {
                this(o);
                TextImageGetter g = new TextImageGetter(c, l);
                if(explanation != null) explanationParsed = Lesson.centerImages(Html.fromHtml(explanation, g, null));
                if(classKey != null && classKey.equals("caMultiselectAnswerImage")){
                    SpannableStringBuilder b = new SpannableStringBuilder(" ");
                    b.setSpan(new ImageSpan(g.getDrawable(answer)), 0, 1, 0);
                    answerParsed = Lesson.centerImages(b);
                } else if(answer != null) answerParsed = Lesson.centerImages(Html.fromHtml(answer, g, null));
            }

            public Spanned getExplanationParsed() {
                return explanationParsed;
            }

            public Spanned getAnswerParsed() {
                return answerParsed;
            }

            @Override
            public void close() throws Exception {
                
            }
            
            private void putData(JSONObject o){
                try {
                    o.put(this.id + "", selected);
                } catch (JSONException ex) {
                    Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
    }
    
    public static class Input extends Exercise {
        
        private ArrayList<Field> fields = new ArrayList();
        private String explanation;
        private Spanned explanationParsed;
        private int inputsGroup;

        public Input(JSONObject o, Context c, LoadedDrawable.OnDrawableUpdatedListener l, final OnFieldUpdatedListener fl) throws JSONException {
            super(o, c, l);
            TextImageGetter g = new TextImageGetter(c, l);
            if(o.has("explanation")) explanation = o.getString("explanation");
            if(o.has("inputs_group")) inputsGroup = o.getInt("inputs_group");
            if(explanation != null) explanationParsed = Lesson.centerImages(Html.fromHtml(explanation, g, null));
            JSONArray answ = o.getJSONArray("input");
            for(int i = 0; i < answ.length(); i++){
                JSONObject a = answ.getJSONObject(i);
                final int index = fields.size();
                if(a.has("user_input") && a.getBoolean("user_input")) try(Field fld = new Field(a, c, new LoadedDrawable.OnDrawableUpdatedListener() {
                    @Override
                    public void onDrawableUpdated() {
                        if(fl != null) fl.OnFieldUpdated(index);
                    }
                })){
                    fields.add(fld);
                } catch(Exception ex) {
                    
                }
            }
        }

        public Input(JSONObject o) throws JSONException {
            super(o);
            if(o.has("explanation")) explanation = o.getString("explanation");
            if(explanation != null) explanationParsed = Lesson.centerImages(Html.fromHtml(explanation));
            JSONArray answ = o.getJSONArray("input");
            for(int i = 0; i < answ.length(); i++){
                JSONObject a = answ.getJSONObject(i);
                if(a.has("user_input") && a.getBoolean("user_input")) try(Field fld = new Field(a)){
                    fields.add(fld);
                } catch(Exception ex) {
                    
                }
            }
        }

        public int getInputsGroup() {
            return inputsGroup;
        }

        public ArrayList<Field> getFields() {
            return fields;
        }

        public Spanned getExplanationParsed() {
            return explanationParsed;
        }

        @Override
        public String getPostString() {
            JSONObject o = new JSONObject();
            JSONObject f = new JSONObject();
            for(Field fl : fields) fl.putData(f);
            
            try {
                o.put("id", super.id);
                o.put("inputs_group", this.inputsGroup);
            } catch (JSONException ex) {
                Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                o.put("fields", f);
            } catch (JSONException ex) {
                Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return o.toString();
        }

        @Override
        public void validate(Context c, final OnExerciseValidatedListener l) {
            new SendJSONDataTask(c, new OnJSONResponseListener() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        JSONObject f = response.getJSONObject("object").getJSONObject("fields");
                        System.out.println(response);
                        for(Field fl : fields){
                            try {
                                if(f.has(fl.name)){
                                    fl.correctAnswer = f.getBoolean(fl.name);
                                    fl.validated = true;
                                } else {
                                    fl.validated = false;
                                }
                            } catch (JSONException ex) {
                                Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
                                fl.validated = false;
                            }
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Exercise.Input.super.validated = true;
                    try {
                        l.OnExerciseValidated(response.getJSONObject("object").getString("status"));
                    } catch (JSONException ex) {
                        l.OnExerciseValidated("");
                        Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public void onFail(String response) {
                    System.out.println(response);
                    l.OnExerciseValidated("");
                }
            }).execute(getPostString(), "validate");
            System.out.println(getPostString());
        }
        
        public class Field implements AutoCloseable {
            
            private int id;
            private String name;
            private String label;
            private Spanned labelParsed;
            private String type;
            public String value = "";
            private boolean validated = false;
            private boolean correctAnswer = false;

            public boolean isCorrect() {
                return correctAnswer;
            }

            public boolean isValidated() {
                return validated;
            }
            
            private Field(JSONObject o) throws JSONException {
                if(o.has("id")) id = o.getInt("id");
                if(o.has("name")) name = o.getString("name");
                if(o.has("label")) label = o.getString("label");
                if(o.has("type")) type = o.getString("type");
                if(label != null) labelParsed = Lesson.centerImages(Html.fromHtml(label));
            }

            private Field(JSONObject o, Context c, LoadedDrawable.OnDrawableUpdatedListener l) throws JSONException {
                this(o);
                TextImageGetter g = new TextImageGetter(c, l);
                if(label != null) labelParsed = Lesson.centerImages(Html.fromHtml(label, g, null));
            }

            public String getType() {
                return type;
            }

            public Spanned getLabelParsed() {
                return labelParsed;
            }

            @Override
            public void close() throws Exception {
                
            }
            
            private void putData(JSONObject o){
                try {
                    value = value.trim();
                    if(!value.isEmpty()){
                        switch(this.type){
                            case "number":
                            case "int":
                                o.put(this.name, Integer.parseInt(value));
                                break;
                            case "uint":
                                o.put(this.name, Integer.parseInt(value));
                                break;
                            case "float":
                                o.put(this.name, Float.parseFloat(value));
                                break;
                            case "ufloat":
                                o.put(this.name, Float.parseFloat(value));
                                break;
                            case "text":
                            default:
                                o.put(this.name, value);
                                break;
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Exercise.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
    }

    public interface OnExerciseValidatedListener {
        
        public void OnExerciseValidated(String status);
        
    }
        
    public interface OnFieldUpdatedListener {
        
        public void OnFieldUpdated(int n);
        
    }
    
}
