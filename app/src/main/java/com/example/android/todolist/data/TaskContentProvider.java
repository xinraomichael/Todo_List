/*
* Copyright (C) 2016 The Android Open Source Project
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

package com.example.android.todolist.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.android.todolist.R;

// TODO (1) Verify that TaskContentProvider extends from ContentProvider and implements required methods
public class TaskContentProvider extends ContentProvider {


    TaskDbHelper mTaskDbHelper;

    public static final int TASKS = 100;
    public static final int TASK_ID = 101;

    public static final UriMatcher sUriMatcher = buildUriMatch();

    /* onCreate() is where you should initialize anything you’ll need to setup
    your underlying data source.
    In this case, you’re working with a SQLite database, so you’ll need to
    initialize a DbHelper to gain access to it.
     */

    public static UriMatcher buildUriMatch() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS, TASKS);
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS + "/#", TASK_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        // TODO (2) Complete onCreate() and initialize a TaskDbhelper on startup
        // [Hint] Declare the DbHelper as a global variable
        Context context = getContext();
        mTaskDbHelper = new TaskDbHelper(context);

        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        int matcher = sUriMatcher.match(uri);

        Uri returnUri = null;

        switch (matcher) {
            case TASKS:
                // if insert is successful, id should be the unique id for new row, otherwise it would be -1
                long id = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
                if(id > 0) {
                    returnUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, id);
                }
                else{
                    throw new UnsupportedOperationException("failed to insert");
                }
                break;
            case TASK_ID:
                break;
            default:
                throw new UnsupportedOperationException("UNKNOWN URI" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mTaskDbHelper.getReadableDatabase();

        int matcher = sUriMatcher.match(uri);

        Cursor cursor = null;

        switch (matcher) {
            case TASKS:
                cursor = db.query(TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_ID:
                // 1 here because the first element in a row is id
                String id = uri.getPathSegments().get(1);

                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                cursor = db.query(TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("cant query");
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        int matcher = sUriMatcher.match(uri);

        int returnInt;

        switch (matcher) {
            case TASKS:
                returnInt = db.delete(TaskContract.TaskEntry.TABLE_NAME, null, null);
                Toast.makeText(getContext(), "All tasks have been deleted!", Toast.LENGTH_LONG).show();
                break;
            case TASK_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                returnInt = db.delete(TaskContract.TaskEntry.TABLE_NAME, mSelection, mSelectionArgs);
                Toast.makeText(getContext(), "A task has been deleted!", Toast.LENGTH_LONG).show();
                break;
            default:
                throw new UnsupportedOperationException("cant query");
        }

        if (returnInt != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnInt;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

}
