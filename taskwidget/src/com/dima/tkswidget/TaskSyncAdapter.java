package com.dima.tkswidget;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;


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

	        try {
	        	GoogleServiceAuthentificator servAuth = new GoogleServiceAuthentificator(account.name, m_context);
	        	servAuth.authentificate();
	        	GoogleTasksLoader taskLoader = new GoogleTasksLoader(servAuth.getAccessProtectedResource());
	        
	        	List<TaskList> taskLists = taskLoader.getTasksLists();
	        	
	        	provider.delete(TaskMetadata.TASK_INFO.CONTENT_DIR, "", new String[0]);
	        	provider.delete(TaskMetadata.TASK_LIST_INFO.CONTENT_DIR, "", new String[0]);
	        	
	        	ContentValues[] listsValues = new ContentValues[taskLists.size()]; 
	        	for (int i = 0; i < taskLists.size(); i++) {
					TaskList taskList = taskLists.get(i);
					listsValues[i] = getTaskListValues(taskList);
				}
	        	provider.bulkInsert(TaskMetadata.TASK_LIST_INFO.CONTENT_DIR, listsValues);
	        	
	        	for (TaskList taskList : taskLists) {
	        		String taskListId = taskList.getId();
	        		List<Task> tasks = taskLoader.getTasks(taskListId);
	        		
	        		ContentValues[] tasksValues = new ContentValues[tasks.size()];
	        		for (int i = 0; i < tasks.size(); i++) {
						Task task = tasks.get(i);
						tasksValues[i] = getTaskValues(task, taskListId);
					}
	        		provider.bulkInsert(TaskMetadata.TASK_INFO.CONTENT_DIR, tasksValues);
	        	}
	        	
	        	WidgetController ctrl = new WidgetController(m_context);
	        	ctrl.notifyUpdateCompleted();
	        	
	        } catch (final RemoteException e) {
	            LogHelper.e( "RemoteException", e);
	            syncResult.stats.numParseExceptions++;
//	        } catch (final AuthenticatorException e) {
//	            LogHelper.e( "AuthenticatorException", e);
//	            syncResult.stats.numParseExceptions++;
//	        } catch (final OperationCanceledException e) {
//	        	LogHelper.e("OperationCanceledExcetpion", e);
	        } catch (final IOException e) {
	        	LogHelper.e("IOException", e);
	            syncResult.stats.numIoExceptions++;
//	        } catch (final AuthenticationException e) {
//	        	LogHelper.e("AuthenticationException", e);
//	            syncResult.stats.numAuthExceptions++;
//	        } catch (final ParseException e) {
//	        	LogHelper.e("ParseException", e);
//	            syncResult.stats.numParseExceptions++;
//	        } catch (final JSONException e) {
//	        	LogHelper.e("JSONException", e);
//	            syncResult.stats.numParseExceptions++;
	        }
	    }
	    
	    private ContentValues getTaskListValues(TaskList list) {
	    	ContentValues result = new ContentValues(3);
	    	
	    	result.put(TaskMetadata.COL_TL_ID, list.getId());
	    	result.put(TaskMetadata.COL_TL_TITLE, list.getTitle());
	    	Map<String, Object> uk = list.getUnknownKeys();
	    	result.put(TaskMetadata.COL_TL_CREATE_DATE, DateTime.parseRfc3339((String)uk.get("updated")).getValue());
	    	
	    	return result;
	    }
	    
	    private ContentValues getTaskValues(Task task, String listId) {
	    	ContentValues result = new ContentValues(5);
	    	
	    	result.put(TaskMetadata.COL_ID, task.getId());
	    	result.put(TaskMetadata.COL_TITLE, task.getTitle());
	    	result.put(TaskMetadata.COL_PARENT_LIST_ID, listId);
	    	result.put(TaskMetadata.COL_PARENT_TASK_ID, task.getParent());
	    	Map<String, Object> uk = task.getUnknownKeys();
	    	result.put(TaskMetadata.COL_TL_CREATE_DATE, DateTime.parseRfc3339((String)uk.get("updated")).getValue());
	    	
	    	return result;
	    }
}
