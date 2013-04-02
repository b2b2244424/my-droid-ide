/*-
 * Copyright (C) 2009 Google Inc.
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

package com.google.android.demos.jamendo.app;

import com.google.android.demos.jamendo.R;
import com.google.android.demos.jamendo.provider.JamendoContract;
import com.google.android.demos.jamendo.provider.JamendoContract.Albums;
import com.google.android.demos.jamendo.provider.JamendoContract.Artists;
import com.google.android.demos.jamendo.provider.JamendoContract.Users;
import com.google.android.demos.jamendo.widget.ListSeparatorAdapter;
import com.google.android.demos.jamendo.widget.SimpleFeedAdapter;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

public class UserActivity extends JamendoActivity {

    private static String getColumnNameForId(String id) {
        if (id.length() != 0 && Character.isDigit(id.charAt(0))) {
            return Users.ID;
        } else {
            return Users.IDSTR;
        }
    }

    @Override
    protected CursorAdapter createHeaderAdapter() {
        String[] from = {
                Users.IMAGE, Users.NAME, Users.LANG
        };
        int[] to = {
                R.id.icon, R.id.text1, R.id.text2
        };
        SimpleFeedAdapter adapter = new SimpleFeedAdapter(this, R.layout.jamendo_header, from,
                to);
        adapter.setDefaultImageUrl(JamendoApp.DEFAULT_USER_AVATAR_100);
        return adapter;
    }

    @Override
    protected ListAdapter createSeparatorAdapter() {
        return new ListSeparatorAdapter(R.string.list_title_latest_starred_albums);
    }

    @Override
    protected CursorAdapter createListAdapter() {
        String[] from = {
                Albums.IMAGE, Artists.NAME, Albums.NAME
        };
        int[] to = {
                R.id.icon, R.id.text1, R.id.text2
        };
        return new SimpleFeedAdapter(this, R.layout.jamendo_list_item_2, from, to);
    }
    
    /** {@inheritDoc} */
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case LOADER_HEADER: {
                Uri uri = getIntent().getData();
                String[] projection = {
                        Users._ID, Users.IMAGE, Users.NAME, Users.LANG
                };
                String selection = String.format("%s=?", JamendoContract.PARAM_IMAGE_SIZE);
                String[] selectionArgs = {
                    getDimensionPixelSizeAsString(R.dimen.avatar_size)
                };
                String sortOrder = null;
                return new CursorLoader(this, uri, projection, selection, selectionArgs, sortOrder);
            }
            case LOADER_LIST: {
                Uri data = getIntent().getData();
                String id = data.getLastPathSegment();
                Uri uri = Albums.CONTENT_URI;
                String[] projection = {
                        Albums._ID, Albums.IMAGE, Artists.NAME, Albums.NAME
                };
                String selection = String.format("%s=?&%s=?&%s=?", getColumnNameForId(id),
                        JamendoContract.PARAM_JOIN, JamendoContract.PARAM_IMAGE_SIZE);
                String[] selectionArgs = {
                        id, JamendoContract.JOIN_ALBUM_USER_STARRED,
                        getDimensionPixelSizeAsString(R.dimen.image_size)
                };
                String sortOrder = Albums.Order.STARREDDATE.descending();
                return new CursorLoader(this, uri, projection, selection, selectionArgs, sortOrder);
            }
            default:
                return null;
        }
    }

    /** {@inheritDoc} */
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(Albums.CONTENT_URI, id);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
