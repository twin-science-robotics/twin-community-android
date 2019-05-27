package com.twinscience.twin.lite.android.presentation.view;

import android.view.ViewGroup;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Copyright (C) 2017 mertselcukdemir
 */

public abstract class BaseRecyclerAdapter<T extends RecyclerView.ViewHolder, D> extends RecyclerView.Adapter<T> {

    protected List<D> mDataSet;

    public BaseRecyclerAdapter(List<D> dataSet) {
        this.mDataSet = dataSet;
        setHasStableIds(true);
    }

    public abstract T createView(ViewGroup view, int viewType);

    public abstract void bindView(T view, int position);


    @Override
    public long getItemId(int position) {
        return mDataSet.get(position).hashCode();
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return createView(parent, viewType);
    }

    @Override
    public void onBindViewHolder(T holder, int position) {
        bindView(holder, position);
    }

    public int getItemCount() {
        return mDataSet.size();
    }

    public void swapDataSet(List<D> newData) {
        mDataSet = newData;
        notifyDataSetChanged();
    }

    public D getItem(int position) {
        return mDataSet.get(position);
    }

    public int getPosition(D item) {
        return mDataSet.indexOf(item);
    }

    public void removeItem(int position, D item) {
        mDataSet.remove(position);
        mDataSet.add(0, item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mDataSet.remove(position);
        notifyDataSetChanged();
    }

    public void replaceItem(D item, int position) {
        mDataSet.set(position, item);
        notifyItemChanged(position);
    }

    public void addItem(D item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    public void addItem(int position, D item) {
        mDataSet.add(position, item);
        notifyDataSetChanged();
    }

    public List<D> getDataSet() {
        return mDataSet;
    }
}
