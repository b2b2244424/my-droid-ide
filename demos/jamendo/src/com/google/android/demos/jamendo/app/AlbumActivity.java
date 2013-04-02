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
import com.google.android.demos.jamendo.provider.JamendoContract.Tracks;
import com.google.android.demos.jamendo.widget.AlbumHeaderAdapter;
import com.google.android.demos.jamendo.widget.ListSeparatorAdapter;
import com.google.android.demos.jamendo.widget.TrackListAdapter;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import java.text.MessageFormat;

public class AlbumActivity extends JamendoActivity {

    private static final Uri BASE_URI = Uri.parse("http://www.jamendo.com/album");

    private static final int MENU_GROUP_INTENT_OPTIONS = Menu.FIRST;

    @Override
    protected CursorAdapter createHeaderAdapter() {
        return new AlbumHeaderAdapter(this);
    }

    @Override
    protected ListAdapter createSeparatorAdapter() {
        return new ListSeparatorAdapter(R.string.list_title_tracks);
    }

    @Override
    protected CursorAdapter createListAdapter() {
        return new TrackListAdapter(this);
    }

    /** {@inheritDoc} */
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case LOADER_HEADER: {
                Uri uri = getIntent().getData();
                String[] projection = {
                        Albums._ID, Albums.ID, Albums.IMAGE, Albums.NAME, Artists.NAME, Artists.ID,
                        Albums.GENRE
                };
                String selection = String.format("%s=?", JamendoContract.PARAM_IMAGE_SIZE);
                String[] selectionArgs = {
                    getDimensionPixelSizeAsString(R.dimen.image_size)
                };
                String sortOrder = null;
                return new CursorLoader(this, uri, projection, selection, selectionArgs, sortOrder);
            }
            case LOADER_LIST: {
                Uri uri = Tracks.CONTENT_URI;
                String[] projection = TrackListAdapter.PROJECTION;

                // Join the track query to the artist table so that the artist is known
                // even if the album cursor fails.
                String selection = String.format("%s=?&%s=?&%s=?", JamendoContract.PARAM_JOIN,
                        JamendoContract.PARAM_JOIN, Albums.ID);

                Uri albumUri = getIntent().getData();
                long albumId = ContentUris.parseId(albumUri);
                String[] selectionArgs = {
                        JamendoContract.JOIN_TRACK_ALBUM, JamendoContract.JOIN_ALBUM_ARTIST,
                        String.valueOf(albumId)
                };
                String sortOrder = Tracks.Order.NUMALBUM.ascending();
                return new CursorLoader(this, uri, projection, selection, selectionArgs, sortOrder);
            }
            default:
                return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.album_options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        int groupId = MENU_GROUP_INTENT_OPTIONS;
        int itemId = Menu.NONE;
        int order = Menu.NONE;
        ComponentName caller = getComponentName();
        Intent[] specifics = null;
        Intent intent = new Intent();
        long id = ContentUris.parseId(getIntent().getData());
        intent.setDataAndType(
                JamendoContract.createPlaylistUri(JamendoContract.FORMAT_M3U, Albums.ID, id),
                JamendoContract.CONTENT_TYPE_M3U);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        int flags = 0;
        MenuItem[] outSpecificItems = null;
        menu.addIntentOptions(groupId, itemId, order, caller, specifics, intent, flags,
                outSpecificItems);
        return menu.hasVisibleItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                share();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void share() {
        long id = ContentUris.parseId(getIntent().getData());
        Uri uri = ContentUris.withAppendedId(BASE_URI, id);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        Cursor cursor = mHeaderAdapter.getCursor();
        if (cursor != null && cursor.moveToFirst()) {
            String albumName = cursor.getString(cursor.getColumnIndexOrThrow(Albums.NAME));
            String artistName = cursor.getString(cursor.getColumnIndexOrThrow(Artists.NAME));
            String template = getString(R.string.jamendo_template_album_artist);
            String subject = MessageFormat.format(template, albumName, artistName);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(uri));
        intent = Intent.createChooser(intent, null);
        startActivity(intent);
    }

    /** {@inheritDoc} */
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        TrackListAdapter.playTrack(this, id);
    }
}
