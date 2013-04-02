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

package com.google.android.demos.jamendo.net;

import com.google.android.filecache.FileResponseCache;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.ResponseCache;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public class JamendoCache extends FileResponseCache {

    private static final String TAG = "JamendoCache";

    public static void install(Context context) {
        ResponseCache responseCache = ResponseCache.getDefault();
        if (responseCache instanceof JamendoCache) {
            Log.d(TAG, "Cache has already been installed.");
        } else if (responseCache == null) {
            JamendoCache dropCache = new JamendoCache(context);
            ResponseCache.setDefault(dropCache);
        } else {
            Class<? extends ResponseCache> type = responseCache.getClass();
            Log.e(TAG, "Another ResponseCache has already been installed: " + type);
        }
    }

    private static File getCacheDir(Context context) {
        File dir = context.getCacheDir();
        dir = new File(dir, "filecache");
        return dir;
    }

    private final Context mContext;

    public JamendoCache(Context context) {
        mContext = context;
    }
    
    @Override
    protected boolean isStale(File file, URI uri, String requestMethod,
            Map<String, List<String>> requestHeaders, Object cookie) {
        if (cookie instanceof Long) {
            Long maxAge = (Long) cookie;
            long age = System.currentTimeMillis() - file.lastModified();
            if (age > maxAge.longValue()) {
                return true;
            }
        }
        return super.isStale(file, uri, requestMethod, requestHeaders, cookie);
    }

    @Override
    protected File getFile(URI uri, String requestMethod, Map<String, List<String>> requestHeaders,
            Object cookie) {
        try {
            File parent = getCacheDir(mContext);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(String.valueOf(uri).getBytes("UTF-8"));
            byte[] output = digest.digest();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < output.length; i++) {
                builder.append(Integer.toHexString(0xFF & output[i]));
            }
            String filename = builder.toString();
            return new File(parent, filename);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
