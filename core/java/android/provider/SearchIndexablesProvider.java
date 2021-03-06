/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

/**
 * Base class for a search indexable provider. Such provider offers data to be indexed either
 * as a reference to an XML file (like a {@link android.preference.PreferenceScreen}) or either
 * as some raw data.
 *
 * @see SearchIndexableResource
 * @see SearchIndexableData
 * @see SearchIndexablesContract
 *
 * To create a search indexables provider, extend this class, then implement the abstract methods,
 * and add it to your manifest like this:
 *
 * <pre class="prettyprint">&lt;manifest&gt;
 *    ...
 *    &lt;application&gt;
 *        ...
 *        &lt;provider
 *            android:name="com.example.MyIndexablesProvider"
 *            android:authorities="com.example.myindexablesprovider"
 *            android:exported="true"
 *            android:grantUriPermissions="true"
 *            android:permission="android.permission.READ_SEARCH_INDEXABLES"
 *            &lt;intent-filter&gt;
 *                &lt;action android:name="android.content.action.SEARCH_INDEXABLES_PROVIDER" /&gt;
 *            &lt;/intent-filter&gt;
 *        &lt;/provider&gt;
 *        ...
 *    &lt;/application&gt;
 *&lt;/manifest&gt;</pre>
 * <p>
 * When defining your provider, you must protect it with
 * {@link android.Manifest.permission#READ_SEARCH_INDEXABLES}, which is a permission only the system
 * can obtain.
 * </p>
 *
 * @hide
 */
public abstract class SearchIndexablesProvider extends ContentProvider {
    private static final String TAG = "IndexablesProvider";

    private String mAuthority;
    private UriMatcher mMatcher;

    private static final int MATCH_RES_CODE = 1;
    private static final int MATCH_RAW_CODE = 2;
    private static final int MATCH_NON_INDEXABLE_KEYS_CODE = 3;

    /**
     * Implementation is provided by the parent class.
     */
    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        mAuthority = info.authority;

        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(mAuthority, SearchIndexablesContract.INDEXABLES_XML_RES_PATH,
                MATCH_RES_CODE);
        mMatcher.addURI(mAuthority, SearchIndexablesContract.INDEXABLES_RAW_PATH,
                MATCH_RAW_CODE);
        mMatcher.addURI(mAuthority, SearchIndexablesContract.NON_INDEXABLES_KEYS_PATH,
                MATCH_NON_INDEXABLE_KEYS_CODE);

        // Sanity check our setup
        if (!info.exported) {
            throw new SecurityException("Provider must be exported");
        }
        if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grantUriPermissions");
        }
        if (!android.Manifest.permission.READ_SEARCH_INDEXABLES.equals(info.readPermission)) {
            throw new SecurityException("Provider must be protected by READ_SEARCH_INDEXABLES");
        }

        super.attachInfo(context, info);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        switch (mMatcher.match(uri)) {
            case MATCH_RES_CODE:
                return queryXmlResources(null);
            case MATCH_RAW_CODE:
                return queryRawData(null);
            case MATCH_NON_INDEXABLE_KEYS_CODE:
                return queryNonIndexableKeys(null);
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
    }

    /**
     * Returns all {@link android.provider.SearchIndexablesContract.XmlResource}.
     *
     * Those are Xml resource IDs to some {@link android.preference.PreferenceScreen}.
     *
     * @param projection list of {@link android.provider.SearchIndexablesContract.XmlResource}
     *                   columns to put into the cursor. If {@code null} all supported columns
     *                   should be included.
     */
    public abstract Cursor queryXmlResources(String[] projection);

    /**
     * Returns all {@link android.provider.SearchIndexablesContract.RawData}.
     *
     * Those are the raw indexable data.
     *
     * @param projection list of {@link android.provider.SearchIndexablesContract.RawData} columns
     *                   to put into the cursor. If {@code null} all supported columns should be
     *                   included.
     */
    public abstract Cursor queryRawData(String[] projection);

    /**
     * Returns all {@link android.provider.SearchIndexablesContract.NonIndexableKey}.
     *
     * Those are the non indexable data keys.
     *
     * @param projection list of {@link android.provider.SearchIndexablesContract.NonIndexableKey}
     *                   columns to put into the cursor. If {@code null} all supported columns
     *                   should be included.
     */
    public abstract Cursor queryNonIndexableKeys(String[] projection);

    @Override
    public String getType(Uri uri) {
        switch (mMatcher.match(uri)) {
            case MATCH_RES_CODE:
                return SearchIndexablesContract.XmlResource.MIME_TYPE;
            case MATCH_RAW_CODE:
                return SearchIndexablesContract.RawData.MIME_TYPE;
            case MATCH_NON_INDEXABLE_KEYS_CODE:
                return SearchIndexablesContract.NonIndexableKey.MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * Implementation is provided by the parent class. Throws by default, and cannot be overriden.
     */
    @Override
    public final Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported");
    }

    /**
     * Implementation is provided by the parent class. Throws by default, and cannot be overriden.
     */
    @Override
    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    /**
     * Implementation is provided by the parent class. Throws by default, and cannot be overriden.
     */
    @Override
    public final int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }
}
