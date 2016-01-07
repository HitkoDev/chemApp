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
import com.hitkoDev.chemApp.rest.TextImageGetter;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class Exercise {
    
    private int id;
    private String name;
    private String content;
    private String classKey;
    private Spanned contentParsed;
    
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
        
        public class Answer implements AutoCloseable {
            
            private int id;
            private String name;
            private String answer;
            private Spanned answerParsed;
            private String explanation;
            private Spanned explanationParsed;
            private String classKey;
            public boolean selected;
            
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
            
        }
        
    }
    
    public static class Input extends Exercise {
        
        private ArrayList<Field> fields = new ArrayList();
        private String explanation;
        private Spanned explanationParsed;

        public Input(JSONObject o, Context c, LoadedDrawable.OnDrawableUpdatedListener l, final OnFieldUpdatedListener fl) throws JSONException {
            super(o, c, l);
            TextImageGetter g = new TextImageGetter(c, l);
            if(o.has("explanation")) explanation = o.getString("explanation");
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

        public ArrayList<Field> getFields() {
            return fields;
        }

        public Spanned getExplanationParsed() {
            return explanationParsed;
        }
        
        public class Field implements AutoCloseable {
            
            private int id;
            private String name;
            private String label;
            private Spanned labelParsed;
            private String type;
            public String value = "";
            
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
            
        }
        
    }
        
    public interface OnFieldUpdatedListener {
        
        public void OnFieldUpdated(int n);
        
    }
    
}
