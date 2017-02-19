package com.dima.tkswidget;

import java.io.IOException;
import java.util.List;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;


public class TaskSyncAdapter extends AbstractThreadedSyncAdapter {
  private final Context m_context;

  public TaskSyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    m_context = context;
  }

  @Override
  public void onPerformSync(
      Account account,
      Bundle extras,
      String authority,
      ContentProviderClient provider,
      SyncResult syncResult) {

    LogHelper.d("TaskSyncAdapter started");
    String token = null;
    WidgetController ctrl = new WidgetController(m_context, null);

    try {
      try {
        ctrl.notifySyncState(WidgetController.SYNC_STATE_STARTED);

        GoogleServiceAuthenticator servAuth = new GoogleServiceAuthenticator(account, m_context);
        token = servAuth.authentificateSyncAdapter(authority, extras);

        GoogleTasksLoader taskLoader = new GoogleTasksLoader(servAuth.getAccessProtectedResource());
        List<TaskList> taskLists = taskLoader.getTasksLists();

        ContentValues[] listsValues = new ContentValues[taskLists.size()];
        for (int i = 0; i < taskLists.size(); i++) {
          TaskList taskList = taskLists.get(i);
          listsValues[i] = getTaskListValues(taskList);
        }

        provider.delete(TaskMetadata.TASK_LIST_INFO.CONTENT_DIR, "", new String[0]);
        provider.bulkInsert(TaskMetadata.TASK_LIST_INFO.CONTENT_DIR, listsValues);
        ctrl.notifySyncState(WidgetController.SYNC_STATE_LISTS_UPDATED);

        for (TaskList taskList : taskLists) {
          String taskListId = taskList.getId();
          List<Task> tasks = taskLoader.getTasks(taskListId);

          LogHelper.d("Got " + Integer.toString(tasks.size()) + " tasks for list " + taskList.getTitle());
          ContentValues[] tasksValues = new ContentValues[tasks.size()];
          for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            tasksValues[i] = getTaskValues(task, taskListId);
          }
          provider.delete(
              TaskMetadata.TASK_INFO.CONTENT_DIR,
              TaskMetadata.COL_PARENT_LIST_ID + " = ?",
              new String[]{taskListId});
          provider.bulkInsert(TaskMetadata.TASK_INFO.CONTENT_DIR, tasksValues);
        }

        ctrl.notifySyncState(WidgetController.SYNC_STATE_TASKS_UPDATED);
        ctrl.notifySyncState(WidgetController.SYNC_STATE_FINISHED);
        return;

      } catch (final RemoteException e) {
        e.printStackTrace();
        syncResult.stats.numParseExceptions++;
      } catch (UserRecoverableAuthIOException e) {
        e.printStackTrace();
        syncResult.stats.numAuthExceptions++;
        if (token != null) {
          GoogleAuthUtil.clearToken(m_context, token);
        }
      } catch (GoogleAuthException e) {
        e.printStackTrace();
        syncResult.stats.numAuthExceptions++;
      } catch (IOException e) {
        syncResult.stats.numIoExceptions++;
        e.printStackTrace();
        if (token != null) {
          GoogleAuthUtil.clearToken(m_context, token);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    LogHelper.d("TaskSyncAdapter failed to sync");
    ctrl.notifySyncState(WidgetController.SYNC_STATE_FINISHED);
  }

  private ContentValues getTaskListValues(TaskList list) {
    ContentValues result = new ContentValues(3);

    result.put(TaskMetadata.COL_TL_ID, list.getId());
    result.put(TaskMetadata.COL_TL_TITLE, list.getTitle());
    result.put(TaskMetadata.COL_TL_CREATE_DATE, list.getUpdated().getValue());

    return result;
  }

  private ContentValues getTaskValues(Task task, String listId) {
    ContentValues result = new ContentValues(7);

    result.put(TaskMetadata.COL_ID, task.getId());
    result.put(TaskMetadata.COL_TITLE, task.getTitle());
    result.put(TaskMetadata.COL_CREATE_DATE, task.getUpdated().getValue());
    result.put(TaskMetadata.COL_STATUS, task.getStatus());
    result.put(TaskMetadata.COL_PARENT_LIST_ID, listId);
    result.put(TaskMetadata.COL_PARENT_TASK_ID, task.getParent());
    result.put(TaskMetadata.COL_POSITION, task.getPosition());

    return result;
  }
}
