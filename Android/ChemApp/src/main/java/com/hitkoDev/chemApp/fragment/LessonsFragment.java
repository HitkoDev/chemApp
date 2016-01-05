/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.MainActivity;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.data.Lesson;
import com.hitkoDev.chemApp.rest.LoadDataTask;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.hitkoDev.chemApp.data.LoadedDrawable.OnDrawableUpdatedListener;

/**
 *
 * @author hitno
 */
public class LessonsFragment extends Fragment {
    
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private RecyclerView recyclerView;
    private LayoutManager layoutManager;
    private int loadedSection;
    private ArrayList<Lesson> lessons = new ArrayList();
    private LessonAdapter adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.lessons_content, container, false);
        
       // TextView t = (TextView) v.findViewById(R.id.section_name);
        
        settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
        prefEditor = settings.edit();
        settings.getInt("section", 0);
        
        recyclerView = (RecyclerView) v.findViewById(R.id.lesson_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new LessonAdapter();
        
        loadContent(settings.getInt("section", 0));
        
       // t.setText(settings.getInt("section", 0) + "");
        
        return v;
    }
    
    public void loadContent(final int section){
        if(section > 0){
            if(section != loadedSection) {
                new LoadDataTask(getContext(), new OnJSONResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            JSONArray l = response.getJSONObject("object").getJSONArray("content");
                            lessons.clear();
                            for(int i = 0; i < l.length(); i++){
                                try {
                                    final int index = lessons.size();
                                    Lesson ls = new Lesson(l.getJSONObject(i), getContext(), new OnDrawableUpdatedListener(){
                                        @Override
                                        public void onDrawableUpdated() {
                                            adapter.notifyItemChanged(index);
                                        }
                                    });
                                    lessons.add(ls);
                                } catch (JSONException ex) {
                                    Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                                }
                            }
                        } catch (JSONException ex) {
                            Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                        loadedSection = section;
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onFail(String response) {
                        System.out.println(response);
                    }
                }).execute("section", section+"");
            } else {
                if(recyclerView.getAdapter() != adapter) recyclerView.setAdapter(adapter);
            }
        }
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        
        LinearLayout text;
        TextView tTitle;
        TextView tDesc;
        
        LinearLayout image;
        ImageView iImage;
        TextView iTitle;
        
        public ViewHolder(View v) {
            super(v);
            text = (LinearLayout) v.findViewById(R.id.lesson_text);
            tTitle = (TextView) text.findViewById(R.id.text_title);
            tDesc = (TextView) text.findViewById(R.id.text_cont);
            
            image = (LinearLayout) v.findViewById(R.id.lesson_image);
            iImage = (ImageView) image.findViewById(R.id.image_cont);
            iTitle = (TextView) image.findViewById(R.id.image_title);
        }
        
    }
    
    class LessonAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup vg, int i) {
            View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.lesson_card, vg, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder vh, final int i) {
            Lesson l = lessons.get(i);
            switch(l.getClassKey()){
                case "caText":
                    vh.text.setVisibility(View.VISIBLE);
                    vh.image.setVisibility(View.GONE);
                    
                    vh.tTitle.setText(l.getNameParsed());
                    vh.tDesc.setText(l.getContentParsed());
                    
                    break;
                
                case "caImage":
                    vh.text.setVisibility(View.GONE);
                    vh.image.setVisibility(View.VISIBLE);
                    
                    vh.iTitle.setText(l.getNameParsed());
                    vh.iImage.setImageDrawable(l.getImageContent());
                    
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return lessons.size();
        }
        
    }
    
}
