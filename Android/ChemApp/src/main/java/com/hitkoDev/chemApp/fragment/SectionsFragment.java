/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.MainActivity;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.data.Section;
import com.hitkoDev.chemApp.rest.LoadDataTask;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import com.hitkoDev.chemApp.tiles.ImageCanvas.Dimensions;
import com.hitkoDev.chemApp.tiles.LetterTileProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author hitno
 */
public class SectionsFragment extends Fragment {

    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;
    private static final String EXPANDED_KEYS = "chemApp.SectionsFragment.expand_keys";
    private static final String EXPANDED_VALUES = "chemApp.SectionsFragment.expand_values";
    private int loadedLevel = 0;
    
    public interface onSelectedListener {
        public void onSectionSelected(int section);
    }
    
    private onSelectedListener listener;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private final ArrayList<Section> sections = new ArrayList();

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            listener = (onSelectedListener) activity;
        } catch (ClassCastException e) {
            Logger.getLogger(SectionsFragment.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sections_list, container, false);
        
        settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
        prefEditor = settings.edit();
        
        recyclerView = (RecyclerView) v.findViewById(R.id.sections_recycler_view);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SectionAdapter();
        
        int d = getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
        tileDimensions = new Dimensions(d, d, 1, getResources().getDimensionPixelSize(R.dimen.tile_letter_font_size_medium));
        d = getResources().getDimensionPixelSize(R.dimen.letter_sub_tile_size);
        subTileDimensions = new Dimensions(d, d, 1, getResources().getDimensionPixelSize(R.dimen.tile_letter_font_size_small));
        paddingH = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        paddingV = getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
        offsetPadding = getResources().getDimensionPixelSize(R.dimen.list_item_indent);
        tileProvider = new LetterTileProvider(getResources());
        tileProvider.noCache = true;
        
        loadLevel(settings.getInt("level", 0));
        
        return v;
    }
    
    public void loadLevel(final int level){
        if(level > 0){
            if(level != loadedLevel) {
                new LoadDataTask(getContext(), new OnJSONResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            JSONArray l = response.getJSONObject("object").getJSONArray("sections");
                            sections.clear();
                            for(int i = 0; i < l.length(); i++){
                                try {
                                    Section s = new Section(l.getJSONObject(i), getContext(), new Section.OnIconLoaded() {
                                        @Override
                                        public void notifyLoaded(Section s) {
                                            s.setTile(new BitmapDrawable(getContext().getResources(), tileProvider.makeCircle(s.isChild() ? subTileDimensions : tileDimensions, s.getIcon())));
                                        }
                                    });
                                    s.setTile(new BitmapDrawable(getContext().getResources(), s.loadedIcon() ? tileProvider.makeCircle(tileDimensions, s.getIcon()) : tileProvider.getLetterTile(tileDimensions, s.getName(), s.getId() + "")));
                                    sections.add(s);
                                    if(s.hasChildren()) {
                                        for(Section sub : s.getChildren()) sub.setTile(new BitmapDrawable(getContext().getResources(), s.loadedIcon() ? tileProvider.makeCircle(subTileDimensions, s.getIcon()) : tileProvider.getLetterTile(subTileDimensions, sub.getName(), sub.getId() + "")));
                                        sections.addAll(s.getChildren());
                                    }
                                } catch (JSONException ex) {
                                    Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                                }
                            }
                        } catch (JSONException ex) {
                            Logger.getLogger(MainActivity.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
                        loadedLevel = level;
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onFail(String response) {
                        System.out.println(response);
                    }
                }).execute("level", level+"");
            } else {
                if(recyclerView.getAdapter() != adapter) recyclerView.setAdapter(adapter);
            }
        }
    }
    
    private final HashMap<Integer, Boolean> expanded = new HashMap();
    private Dimensions tileDimensions;
    private Dimensions subTileDimensions;
    private int paddingV;
    private int paddingH;
    private int offsetPadding;
    private LetterTileProvider tileProvider;
        
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;
        public TextView desc;
        public ImageView icon;
        public CheckBox bookmark;
        public CheckBox dropdown;
        public LinearLayout container;
        public int id = 0;
        public int index = 0;
        public RecyclerView.Adapter adapter;
        public boolean binding = false;
        
        public ViewHolder(View v, RecyclerView.Adapter a) {
            super(v);
            adapter = a;
            container = (LinearLayout) v.findViewById(R.id.section_container);
            container.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onSectionSelected(id);
                }
            });
            name = (TextView) v.findViewById(R.id.section_name);
            desc = (TextView) v.findViewById(R.id.section_desc);
            icon = (ImageView) v.findViewById(R.id.tile_icon);
            
            bookmark = (CheckBox) v.findViewById(R.id.section_bookmark);
            bookmark.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(!binding){
                        prefEditor.putBoolean(ChemApp.PREF_SECTION_PR + id, isChecked);
                        prefEditor.commit();
                    }
                }
                
            });
            
            dropdown = (CheckBox) v.findViewById(R.id.section_expand);
            dropdown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    expanded.put(id, isChecked);
                    if(!binding) adapter.notifyItemRangeChanged(index + 1, sections.get(index).getChildrenCount());
                }
                
            });
        }
    }
    
    class SectionAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public int getItemCount() {
            return sections.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder vh, int i) {
            vh.binding = true;
            Section s = sections.get(i);
            if(s.isChild() && !(expanded.containsKey(s.getParent().getId()) && expanded.get(s.getParent().getId()))){
                vh.container.setVisibility(View.GONE);
            } else {
                vh.index = i;
                vh.name.setText(s.getName());
                vh.desc.setText(s.getDescription());
                vh.desc.setVisibility(s.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
                vh.id = s.getId();
                
                vh.container.setPadding(s.isChild() ? offsetPadding: paddingH, paddingV, paddingH, paddingV);
                vh.icon.setPadding(0, 0, s.isChild() ? offsetPadding: paddingH, 0);
                vh.icon.setImageDrawable(s.getTile());

                if(s.isChild() || !s.hasChildren()){
                    vh.bookmark.setVisibility(View.VISIBLE);
                    vh.dropdown.setVisibility(View.GONE);
                    vh.bookmark.setChecked(settings.getBoolean(ChemApp.PREF_SECTION_PR + vh.id, false));
                } else {
                    vh.bookmark.setVisibility(View.GONE);
                    vh.dropdown.setVisibility(View.VISIBLE);
                    vh.dropdown.setChecked(expanded.containsKey(s.getId()) && expanded.get(s.getId()));
                }
                vh.container.setVisibility(View.VISIBLE);
            }
            vh.binding = false;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup vg, int i) {
            View v = LayoutInflater.from(vg.getContext()).inflate(R.layout.section_card, vg, false);
            ViewHolder vh = new ViewHolder(v, this);
            return vh;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); //To change body of generated methods, choose Tools | Templates.
        int i = expanded.size();
        int[] k = new int[i];
        boolean[] v = new boolean[i];
        i = 0;
        for(Entry<Integer, Boolean> e : expanded.entrySet()){
            k[i] = e.getKey();
            v[i] = e.getValue();
            i++;
        }
        outState.putIntArray(EXPANDED_KEYS, k);
        outState.putBooleanArray(EXPANDED_VALUES, v);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle); //To change body of generated methods, choose Tools | Templates.
        setRetainInstance(true);
        if(bundle != null){
            int[] k = bundle.getIntArray(EXPANDED_KEYS);
            boolean[] v = bundle.getBooleanArray(EXPANDED_VALUES);
            if(k != null && v != null) for(int i = 0; i < k.length && i < v.length; i++) expanded.put(k[i], v[i]);
        }
    }
    
}
