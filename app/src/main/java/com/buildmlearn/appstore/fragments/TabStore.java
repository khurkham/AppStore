package com.buildmlearn.appstore.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.buildmlearn.appstore.adapters.CardCategoriesAdapter;
import com.buildmlearn.appstore.models.Apps;
import com.buildmlearn.appstore.utils.SpacesItemDecoration;

import org.buildmlearn.appstore.R;
import com.buildmlearn.appstore.activities.AppsActivity;
import com.buildmlearn.appstore.activities.CategoriesActivity;
import com.buildmlearn.appstore.activities.SplashActivity;
import com.buildmlearn.appstore.adapters.CardViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment class of the Store section on the Home Page.
 */
public class TabStore extends Fragment {

    private static RecyclerView mRecyclerView2;
    private static TextView txtAppsStore;
    private static Context mContext;
    private static TextView txtStore;
    private static int y=9;

    /**
     * It is called when the fragment is created.
     * @param inflater Inflater object which inflates the layout to the container
     * @param container Viewgroup object which is inflated
     * @param savedInstanceState Any instance which need to be loaded, once the app is resumed.
     * @return View object of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_store, container, false);
        mContext = v.getContext();
        txtStore=(TextView)v.findViewById(R.id.txtStore);
        txtAppsStore=(TextView)v.findViewById(R.id.txtAppsStore);
        TextView txtMoreApps = (TextView) v.findViewById(R.id.txtMoreApps);
        TextView txtMoreCategories = (TextView) v.findViewById(R.id.txtMoreCategories);
        txtMoreApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                Intent i = new Intent(mContext, AppsActivity.class);
                TabStore.this.startActivity(i);
            }
        });
        txtMoreCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                Intent i = new Intent(mContext, CategoriesActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                TabStore.this.startActivity(i);
                ((Activity) mContext).finish();
            }
        });
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mContext);
        int x = Integer.parseInt(SP.getString("number_featured_categories", getResources().getInteger(R.integer.default_number_of_featured_apps)+""));
        y= Integer.parseInt(SP.getString("number_featured_apps",getResources().getInteger(R.integer.default_number_of_featured_categories)+""));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.card_spacing);
        RecyclerView mRecyclerView1 = (RecyclerView) v.findViewById(R.id.rvCategoriesCard);
        mRecyclerView1.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(v.getContext());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView1.setLayoutManager(llm);
        CardCategoriesAdapter adapter1 = new CardCategoriesAdapter(mContext, x);
        mRecyclerView1.setAdapter(adapter1);
        mRecyclerView1.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRecyclerView2 = (RecyclerView) v.findViewById(R.id.rvAppCard);
        mRecyclerView2.setHasFixedSize(true);
        GridLayoutManager glm = new GridLayoutManager(v.getContext(), 3);
        mRecyclerView2.setLayoutManager(glm);
        CardViewAdapter adapter2 = new CardViewAdapter(SplashActivity.appList, v.getContext(),y);
        mRecyclerView2.setAdapter(adapter2);
        mRecyclerView2.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        return v;
    }

    /**
     * This method is called from the Navigation Activity, which controls all the search view.
     * @param query A string with search query.
     */
    public static void refineSearch(String query)    {
        txtAppsStore.setText("Search Results");
        List<Apps> tempList=new ArrayList<>();
        if(query.equals("")){closeSearch();return;}
        else for(int i=0;i<SplashActivity.appList.size();i++)
        {
            if(SplashActivity.appList.get(i).Name.toLowerCase().contains(query.toLowerCase()))tempList.add(SplashActivity.appList.get(i));
        }
        if(tempList.size()!=0){
            CardViewAdapter adapter = new CardViewAdapter(tempList, mContext);
            mRecyclerView2.setAdapter(adapter);
            txtStore.setVisibility(View.GONE);
            mRecyclerView2.setVisibility(View.VISIBLE);
        }
        else{
            txtStore.setText("Sorry, No app matches your search!");
            mRecyclerView2.setVisibility(View.GONE);
            txtStore.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This method is called from the Navigation Activity. It closes the search view for this activity.
     */
    public static void closeSearch()    {
        txtAppsStore.setText("Featured Apps");
        mRecyclerView2.setVisibility(View.VISIBLE);
        txtStore.setVisibility(View.GONE);
        CardViewAdapter adapter = new CardViewAdapter(SplashActivity.appList, mContext,y);
        mRecyclerView2.setAdapter(adapter);
    }
}