package com.cybexmobile.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cybexmobile.HelperClass.StoreThemeHelper;
import com.cybexmobile.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseThemeFragment extends Fragment {

    private ListView mListView;
    private List<String> mThemeList;
    private int selectedPosition = 0;
    private String[] theme = new String[2];
    private SharedPreferences mSharedPreference;


    public ChooseThemeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_choose_theme, container, false);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {
            final ImageView backArrowButton = (ImageView) actionBar.getCustomView().findViewById(R.id.action_bar_arrow_back_button);
            backArrowButton.setVisibility(View.VISIBLE);
            backArrowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                    backArrowButton.setVisibility(View.GONE);
                }
            });
        }
        initView(view);
        initData();
        return view;
    }


    private void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.select_theme_list);
    }

    private void initData() {
        mThemeList = new ArrayList<String>();
        mThemeList.add(getContext().getResources().getString(R.string.setting_theme_dark));
        mThemeList.add(getContext().getResources().getString(R.string.setting_theme_light));


        selectedPosition = StoreThemeHelper.getLocalThemePosition(getContext());
        final ThemeAdapter themeAdapter = new ThemeAdapter(getActivity(), mThemeList, selectedPosition);
        mListView.setAdapter(themeAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                StoreThemeHelper.setLocalThemePosition(getContext(), selectedPosition);
                themeAdapter.notifyDataSetChanged();
                if (mThemeList.get(position).equals(getResources().getString(R.string.setting_theme_light))) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("night_mode", true).apply();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("night_mode", false).apply();

                }
                getFragmentManager().beginTransaction().remove(ChooseThemeFragment.this).commit();
                getActivity().recreate();
            }
        });
    }

    public class ThemeAdapter extends BaseAdapter {
        Context mContext;
        List<String> mList;
        int mPosition;
        LayoutInflater inflater;

        public ThemeAdapter(Context context, List<String> list, int position) {
            mContext = context;
            mList = list;
            mPosition = position;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ThemeAdapter.ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.theme_item_adapter, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.theme_name);
                viewHolder.select = (RadioButton) convertView.findViewById(R.id.theme_radio_button);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ThemeAdapter.ViewHolder) convertView.getTag();
            }

            viewHolder.name.setText(mList.get(position));
            if (selectedPosition == position) {
                viewHolder.select.setBackgroundResource(R.drawable.ic_check);
                viewHolder.name.setTextColor(getResources().getColor(R.color.primary_orange));
            } else {
                viewHolder.select.setBackgroundResource(0);
                viewHolder.name.setTextColor(getResources().getColor(R.color.primary_color_white));
            }

            return convertView;
        }

        public class ViewHolder {
            TextView name;
            RadioButton select;
        }
    }

}
