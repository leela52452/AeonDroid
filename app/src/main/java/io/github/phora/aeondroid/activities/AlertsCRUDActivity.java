package io.github.phora.aeondroid.activities;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CursorTreeAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;

import net.i2p.android.ext.floatingactionbutton.AddFloatingActionButton;

import io.github.phora.aeondroid.DBHelper;
import io.github.phora.aeondroid.R;
import io.github.phora.aeondroid.model.adapters.AlertCursorAdapter;
import io.github.phora.aeondroid.widgets.FABAnimator;

public class AlertsCRUDActivity extends ExpandableListActivity {

    private static final int EDITED_ALERT_AND_STEPS = 1;
    private static final int EDITED_STEP = 2;

    private Context context;
    private View.OnClickListener mLinkButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //take us to the triggers activity with a triggers for alert cursor
            Intent intent = new Intent(AlertsCRUDActivity.this, EditTriggerActivity.class);
            startActivity(intent);
        }
    };
    private View.OnClickListener mEditButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //take us to the edit steps activity, which will have name+steps on there
            //for re-ordering
            Intent intent = new Intent(AlertsCRUDActivity.this, AlertEditActivity.class);
            Cursor c = ((AlertCursorAdapter)getExpandableListAdapter()).getCursor();
            int pos = (Integer)view.getTag();
            c.move(pos);

            startActivityForResult(intent, EDITED_ALERT_AND_STEPS);
        }
    };
    private View.OnClickListener mStepEditButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //take us to the step edit activity, where we actually edit the parameters
            Intent intent = new Intent(AlertsCRUDActivity.this, StepEditActivity.class);
            AlertCursorAdapter aca = (AlertCursorAdapter)getExpandableListAdapter();
            Cursor c = (Cursor) view.getTag(0);
            int pos = (Integer) view.getTag(1);
            c.move(pos);

            long stepId = c.getLong(c.getColumnIndex(DBHelper.COLUMN_ID));
            int repetitions = c.getInt(c.getColumnIndex(DBHelper.STEP_REPITITIONS));
            String url = c.getString(c.getColumnIndex(DBHelper.STEP_LINK));
            String desc = c.getString(c.getColumnIndex(DBHelper.STEP_DESCRIPTION));
            String imageUri = c.getString(c.getColumnIndex(DBHelper.STEP_IMAGE));
            int color = c.getInt(c.getColumnIndex(DBHelper.STEP_COLOR));

            intent.putExtra(StepEditActivity.EXTRA_STEP_ID, stepId);
            intent.putExtra(StepEditActivity.EXTRA_REPS, repetitions);
            intent.putExtra(StepEditActivity.EXTRA_URL, url);
            intent.putExtra(StepEditActivity.EXTRA_DESC, desc);
            intent.putExtra(StepEditActivity.EXTRA_IMAGE, imageUri);
            intent.putExtra(StepEditActivity.EXTRA_COLOR, color);

            startActivityForResult(intent, EDITED_STEP);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts_edit);

        context = this;

        final ExpandableListView listView = getExpandableListView();

        AddFloatingActionButton fab = (AddFloatingActionButton) findViewById(R.id.fab);

        listView.setOnScrollListener(new FABAnimator(context, fab));
    }

    public void addAlert(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setMessage(R.string.AlertsEditActivity_NameAlert);
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String alertName = editText.getText().toString();
                if (!TextUtils.isEmpty(alertName)) {
                    new AddAlertTask(alertName).execute();
                }
            }
        });
        builder.setNegativeButton(R.string.Cancel, null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITED_ALERT_AND_STEPS) {
            if (resultCode == RESULT_OK) {
                new LoadAlertsTask().execute();
            }
        }
        else if (requestCode == EDITED_STEP) {
            if (resultCode == RESULT_OK) {

                new LoadAlertsTask().execute();
            }
        }
    }

    private class LoadAlertsTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            CursorTreeAdapter adapter = (CursorTreeAdapter) AlertsCRUDActivity.this.getExpandableListAdapter();
            if (adapter == null) {
                adapter = new AlertCursorAdapter(cursor, context, mLinkButtonListener, mEditButtonListener, mStepEditButtonListener);
                AlertsCRUDActivity.this.setListAdapter(adapter);
            }
            else {
                adapter.changeCursor(cursor);
            }
        }
    }

    private class AddAlertTask extends AsyncTask<Void, Void, Void> {
        private String alertName;

        public AddAlertTask(String alertName) {
            this.alertName = alertName;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DBHelper.getInstance(context).createAlert(alertName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LoadAlertsTask().execute();
        }
    }
}