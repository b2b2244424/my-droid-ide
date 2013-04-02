/*-
 * Copyright (C) 2010 Google Inc.
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

package com.google.android.demos.jamendo.widget;

import com.google.android.demos.jamendo.R;
import com.google.android.feeds.ContentDecorator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

/**
 * Gallery decorator.
 * <ul>
 * <li>Displays a progress spinner when more rows are loading</li>
 * <li>Displays an error message when additional rows fail to load</li>
 * <li>Sends retry button clicks to a listener</li>
 * </ul>
 */
public final class GalleryDecorator extends ContentDecorator implements SpinnerAdapter {

    private final View.OnClickListener mListener;

    public GalleryDecorator(ListAdapter adapter, View.OnClickListener listener) {
        super(adapter);
        mListener = listener;
    }

    @Override
    protected View newLoadingView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.jamendo_gallery_loading, parent, false);
    }

    @Override
    protected View newErrorView(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.jamendo_gallery_error, parent, false);
        view.findViewById(R.id.retry).setOnClickListener(mListener);
        return view;
    }

    /** {@inheritDoc} */
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
