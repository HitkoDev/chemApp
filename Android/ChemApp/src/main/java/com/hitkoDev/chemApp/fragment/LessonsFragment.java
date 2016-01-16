/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hitkoDev.chemApp.fragment;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hitkoDev.chemApp.ChemApp;
import com.hitkoDev.chemApp.R;
import com.hitkoDev.chemApp.data.Lesson;
import com.hitkoDev.chemApp.rest.LoadDataTask;
import com.hitkoDev.chemApp.rest.OnJSONResponseListener;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.hitkoDev.chemApp.data.LoadedDrawable.OnDrawableUpdatedListener;
import com.hitkoDev.chemApp.data.Section;
import com.hitkoDev.chemApp.helper.FragmentActionsListener;
import com.hitkoDev.chemApp.tiles.ImageCanvas;
import com.hitkoDev.chemApp.tiles.LetterTileProvider;

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
    private final ArrayList<Lesson> lessons = new ArrayList();
    private LessonAdapter adapter;
    private FragmentActionsListener listener;
    private Section section;
    private ImageCanvas.Dimensions tileDimensions;
    private LetterTileProvider tileProvider;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentActionsListener) {
            listener = (FragmentActionsListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle); //To change body of generated methods, choose Tools | Templates.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.lessons_content, container, false);

        settings = getContext().getSharedPreferences(ChemApp.PREF_NAME, 0);
        prefEditor = settings.edit();
        settings.getInt("section", 0);

        int d = getResources().getDimensionPixelSize(R.dimen.letter_tile_size);
        tileDimensions = new ImageCanvas.Dimensions(d, d, 1, getResources().getDimensionPixelSize(R.dimen.tile_letter_font_size_medium));
        tileProvider = new LetterTileProvider(getContext());
        tileProvider.noCache = true;

        recyclerView = (RecyclerView) v.findViewById(R.id.lesson_recycler_view);
        layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new LessonAdapter();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        final int section = settings.getInt("section", 0);
        if (section > 0) {
            if (section != loadedSection) {
                if (getContext() != null) {
                    new LoadDataTask(getContext(), new OnJSONResponseListener() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            try {
                                JSONArray l = response.getJSONObject("object").getJSONArray("content");
                                LessonsFragment.this.section = new Section(response.getJSONObject("object"), getContext(), new Section.OnIconLoaded() {
                                    @Override
                                    public void notifyLoaded(Section s) {
                                        setHeader();
                                    }
                                });
                                if (!LessonsFragment.this.section.hasIcon()) {
                                    setHeader();
                                }
                                lessons.clear();
                                for (int i = 0; i < l.length(); i++) {
                                    try {
                                        final int index = lessons.size();
                                        Lesson ls = new Lesson(l.getJSONObject(i), getContext(), new OnDrawableUpdatedListener() {
                                            @Override
                                            public void onDrawableUpdated() {
                                                adapter.notifyItemChanged(index);
                                            }
                                        });
                                        lessons.add(ls);
                                    } catch (Exception ex) {
                                    }
                                }
                            } catch (Exception ex) {
                            }
                            loadedSection = section;
                            recyclerView.setAdapter(adapter);
                        }

                        @Override
                        public void onFail(String response) {
                            System.out.println(response);
                        }
                    }).execute("section", section + "");
                }
            } else {
                if (recyclerView.getAdapter() != adapter) {
                    recyclerView.setAdapter(adapter);
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
            switch (l.getClassKey()) {
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
