/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kharlamov.cheesetask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CheeseListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Cheese>> {

    public static final String KEY_CHEESES = "cheeses";
    public static final String KEY_FRAGMENT_NUMBER = "fragment_number";
    private RecyclerView mRvCheeses;
    private ArrayList<Cheese> mCheeseList;
    private int loaderId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        loaderId = getArguments().getInt(KEY_FRAGMENT_NUMBER);
        View view = inflater.inflate(R.layout.fragment_cheese_list, container, false);
        mRvCheeses = (RecyclerView) view.findViewById(R.id.recyclerview);
        if (savedInstanceState != null) {
            try {
                mCheeseList = (ArrayList<Cheese>) savedInstanceState.get(KEY_CHEESES);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        setupRecyclerView(mRvCheeses);
        return view;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        if (mCheeseList==null) {
            getActivity().getSupportLoaderManager().initLoader(loaderId, null, this);
        } else {
            mRvCheeses.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), mCheeseList));
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CHEESES, mCheeseList);
    }

    @Override
    public Loader<List<Cheese>> onCreateLoader(int id, Bundle args) {
        return new ListLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<Cheese>> loader, List<Cheese> data) {
        if (loader.getId()==loaderId) {
            mRvCheeses.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), data));
            mCheeseList = (ArrayList<Cheese>) data;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Cheese>> loader) {

    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder>
    {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<Cheese> mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public Cheese mBoundItem;

            public final View mView;
            public final ImageView mImageView;
            public final TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.avatar);
                mTextView = (TextView) view.findViewById(android.R.id.text1);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }

        public Cheese getValueAt(int position) {
            return mValues.get(position);
        }

        public SimpleStringRecyclerViewAdapter(Context context, List<Cheese> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if (mValues != null) {
                holder.mBoundItem = getValueAt(position);
                holder.mTextView.setText(holder.mBoundItem.getName());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, CheeseDetailActivity.class);
                        intent.putExtra(CheeseDetailActivity.EXTRA_CHEESE, holder.mBoundItem);

                        context.startActivity(intent);
                    }
                });

                Glide.with(holder.mImageView.getContext())
                        .load(holder.mBoundItem.getDrawableResId())
                        .fitCenter()
                        .into(holder.mImageView);
            }
            else {
                holder.mTextView.setText(R.string.no_cheeses_loaded);
            }
        }

        @Override
        public int getItemCount() {
            if (mValues != null) {
                return mValues.size();
            }
            else {
                return 1;
            }
        }
    }

    private static class ListLoader extends AsyncTaskLoader<List<Cheese>> {

        public ListLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        public List<Cheese> loadInBackground() {
            try {
                return CheeseApi.listCheeses(30);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
