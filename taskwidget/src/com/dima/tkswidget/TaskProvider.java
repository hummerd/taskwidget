package com.dima.tkswidget;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

public class TaskProvider {
	protected final Context m_context;
	protected final ContentResolver m_content;
	
	public TaskProvider(Context context) {
		m_context = context;
		m_content = m_context.getContentResolver();
	}
	

	public List<TaskList> getLists() {
		Cursor cursor = m_content.query(
				TaskMetadata.TASK_LIST_INFO.CONTENT_DIR,
				new String[] { TaskMetadata.COL_TL_ID, TaskMetadata.COL_TL_TITLE },  
				"",  
				null, 
				null);
		
		List<TaskList> result = new ArrayList<TaskList>(cursor.getCount());
		while (cursor.moveToNext()) {
			TaskList taskList = new TaskList();
			taskList.setId(cursor.getString(0));
			taskList.setTitle(cursor.getString(1));
			result.add(taskList);
		}
		return result;
	}
	
	public TaskList getList(String id) {
		Cursor cursor = m_content.query(
				TaskMetadata.TASK_LIST_INFO.CONTENT_ITEM,
				new String[] { TaskMetadata.COL_TL_ID, TaskMetadata.COL_TL_TITLE },  
				TaskMetadata.TASK_LIST_INFO.COL_ID + " = ?",  
				new String[] { id }, 
				null);
		
		if(!cursor.moveToNext()) {
			return null;
		}
		
		TaskList result = new TaskList();
		result.setId(cursor.getString(0));
		result.setTitle(cursor.getString(1));
		cursor.close();
		return result;
	}
	
	public List<Task> getListTasks(String id) {
		Cursor cursor = m_content.query(
				TaskMetadata.TASK_INFO.CONTENT_ITEM,
				new String[] { TaskMetadata.COL_ID, TaskMetadata.COL_TITLE },  
				TaskMetadata.COL_PARENT_LIST_ID + " = ?",  
				new String[] { id }, 
				null);
		
		List<Task> result = new ArrayList<Task>(cursor.getCount());
		while (cursor.moveToNext()) {
			Task task = new Task();
			task.setId(cursor.getString(0));
			task.setTitle(cursor.getString(1));
			result.add(task);
		}
		
		cursor.close();
		return result;
	}
}