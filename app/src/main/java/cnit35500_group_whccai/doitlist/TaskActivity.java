package cnit35500_group_whccai.doitlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import Functional.CoursesControl;
import Functional.Globals;
import Functional.Task;
import Functional.TasksControl;
import Functional.RecyclerViewAdapterCourseScope;

public class TaskActivity extends AppCompatActivity
{
    private TasksControl mTasks;
    private CoursesControl mCourses;
    private Task currentTask;
    private Menu menu;

    private TextView mTxtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Receive data through Intent
        // Reference: https://stackoverflow.com/questions/14333449/passing-data-through-intent-using-serializable
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            // Obtain data
            mTasks = (TasksControl) getIntent().getSerializableExtra(Globals.ExtraKey_Tasks);
            mCourses = (CoursesControl) getIntent().getSerializableExtra(Globals.ExtraKey_Courses);
            Integer cIndex = getIntent().getIntExtra(Globals.ExtraKey_Index, 0);

            // Set global values
            currentTask = mTasks.getTaskAt(cIndex);

            // Set up toolbar
            android.support.v7.widget.Toolbar toolbar = findViewById(R.id.ios_toolbar);
            setSupportActionBar(toolbar);
            //
        } else
        {
            Toast.makeText(this, "Failed to open a task", Toast.LENGTH_LONG).show();

            finish();
        }
        //

        populateViews();
    }

    public void openSessionsHistory(View view)
    {
        Intent i = new Intent(this, SessionsHistoryActivity.class);

        // Pass an object to another activity
        // Reference: https://stackoverflow.com/questions/2736389/how-to-pass-an-object-from-one-activity-to-another-on-android
        i.putExtra(Globals.ExtraKey_Sessions, currentTask.getSessions());

        startActivityForResult(i, Globals.SessionsHistoryRequestCode);
    }

    public void recordTaskTime(View v)
    {
        currentTask.updateRecordingTime();

        mTxtStatus.setText(currentTask.getStatusStr());
    }

    private void populateViews()
    {
        CollapsingToolbarLayout col_toolbar = findViewById(R.id.ios_col_toolbar);
        col_toolbar.setTitle(currentTask.getName());

        // Populate views with data
        // Set up scope view
        // RelativeLayout item = (RelativeLayout) view.findViewById(R.id.item);
        RecyclerView recyclerView = findViewById(R.id.rvCourses);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);
        RecyclerViewAdapterCourseScope adapter = new RecyclerViewAdapterCourseScope(this, currentTask.getCourse());
        //viewAdapterCourseScope.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // Show status
        TextView txt = findViewById(R.id.txtTaskStatus);
        txt.setText(currentTask.getStatusStr().toUpperCase());

        mTxtStatus = txt;

        // Show deadline
        String formattedString = Globals.formatDateAndTime(currentTask.getDeadline());

        txt = findViewById(R.id.lblTaskDeadline);
        txt.setText(formattedString);

        // Show time before deadline label
        Long timeBeforeDeadline = currentTask.getTimeBeforeDeadline();

        txt = findViewById(R.id.txtTaskDeadlineTimeRemaining);
        txt.setText(Globals.formatTimeTotal(timeBeforeDeadline));
        //

        // Set time remaining
        Long taskGoalTime = currentTask.getTimeGoal(),
                taskSpentTime = currentTask.getTimeSpent(),
                taskRemainTime = currentTask.getTimeRemainEst();

        txt = findViewById(R.id.lblTaskProgressRemaining);
        txt.setText(Globals.formatTimeTotal(taskRemainTime));

        // Set time spent
        txt = findViewById(R.id.txtTaskProgressMade);
        txt.setText(Globals.formatTimeTotal(taskSpentTime));

        // Set time goal
        txt = findViewById(R.id.txtTaskProgressTotal);
        txt.setText(Globals.formatTimeTotal(taskGoalTime));

        // Set progress bar
        ProgressBar timeSpent = findViewById(R.id.prgTaskSpent);
        timeSpent.setIndeterminate(false);
        if (!currentTask.getStatusFinished())
        {
            timeSpent.setMax(Math.toIntExact(currentTask.getTimeGoal()));
            timeSpent.setProgress(Math.toIntExact(currentTask.getTimeSpent()));
        }
        else
        {
            timeSpent.setMax(1);
            timeSpent.setProgress(1);
        }
        //

        // Set Sessions info
        long sessionsCount = currentTask.getSessions().getSessions().length;

        txt = findViewById(R.id.lblTaskSessionsCount);
        txt.setText(String.valueOf(sessionsCount));

        // Set session avg length
        Long avgLength = currentTask.getAvgSessionLength();

        txt = findViewById(R.id.lblTaskSessionsAvgTime);
        txt.setText(Globals.formatTimeTotal(avgLength));
        //

        // Set notes
        txt = findViewById(R.id.lblTaskNotes);
        txt.setText(currentTask.getDetail());

        // Set record floating button
        FloatingActionButton fab = findViewById(R.id.btnFloatTaskRecord);
        if (currentTask.getStatusFinished())
            fab.hide();
        else
            fab.show();
    }

    // Add ToolBar button
    // Reference: https://stackoverflow.com/questions/38158953/how-to-create-button-in-action-bar-in-android
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_buttons, menu);

        this.menu = menu;

        // Show and hide menu buttons
        updateMenuVisibles("fed");

        return super.onCreateOptionsMenu(menu);
    }

    private void updateMenuVisibles(String mode)
    {
        MenuItem itemFinish = menu.findItem(R.id.btnToolBarFinish);
        MenuItem itemDone = menu.findItem(R.id.btnToolBarDone);
        MenuItem itemEdit = menu.findItem(R.id.btnToolBarEdit);
        MenuItem itemRemove = menu.findItem(R.id.btnToolBarRemove);

        itemFinish.setVisible(false);
        itemEdit.setVisible(false);
        itemRemove.setVisible(false);
        itemDone.setVisible(false);

        if (mode.contains("f"))
            itemFinish.setVisible(true);

        if (mode.contains("e"))
            itemEdit.setVisible(true);

        if (mode.contains("d"))
            itemDone.setVisible(true);
    }

    // Handle toolbar button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case (R.id.btnToolBarFinish):
                currentTask.setStatusFinished(!currentTask.getStatusFinished());

                FloatingActionButton fab = findViewById(R.id.btnFloatTaskRecord);

                mTxtStatus.setText(currentTask.getStatusStr());

                if (currentTask.getStatusFinished())
                    fab.hide();
                else
                    fab.show();

                break;

            case (R.id.btnToolBarEdit):
                Intent i = new Intent(this, ManageTaskActivity.class);

                // Pass an object to another activity
                // Reference: https://stackoverflow.com/questions/2736389/how-to-pass-an-object-from-one-activity-to-another-on-android
                i.putExtra(Globals.ExtraKey_ViewMode, Globals.ViewMode_Edit);
                i.putExtra(Globals.ExtraKey_Tasks, mTasks);
                i.putExtra(Globals.ExtraKey_Courses, mCourses);
                i.putExtra(Globals.ExtraKey_Index, mTasks.getTaskIndexFor(currentTask));

                // Set result code from Third activity to first activity
                // Reference: https://stackoverflow.com/questions/28944137/android-get-result-from-third-activity
                i.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(i);

                finish();
                break;

            case (R.id.btnToolBarDone):
                // Send data back
                Intent i2 = new Intent();
                i2.putExtra(Globals.ExtraKey_Tasks, mTasks);
                i2.putExtra(Globals.ExtraKey_Courses, mCourses);
                setResult(Globals.RESULT_SAVE, i2);

                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}