package dima.soft;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksRequest;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;


public class GoogleTasksLoader {
	protected static final String TASKS_APP_KEY = "AIzaSyC0HLKFQZbYA0iolnXzNo5WNItMNOomLtg";
	protected static final String APP_NAME = "simpletaskswidget";
	
	protected GoogleAccessProtectedResource m_googleAccess;
	protected Tasks m_tasksService;
	
	
	public GoogleTasksLoader(GoogleAccessProtectedResource googleAccess) {
		m_googleAccess = googleAccess;
		
		HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
		m_tasksService = Tasks.builder(httpTransport, new JacksonFactory())
			.setApplicationName(APP_NAME)
	    	.setHttpRequestInitializer(m_googleAccess)
	    	.setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
	              public void initialize(JsonHttpRequest request) throws IOException {
	                TasksRequest tasksRequest = (TasksRequest) request;
	                tasksRequest.setKey(TASKS_APP_KEY);
	              }
	            })
	        .build();
		
		Logger.getLogger("com.google.api.client").setLevel(Level.ALL);
	}
	
	
	public List<Task> getTasks(String listId) 
			throws IOException {
		com.google.api.services.tasks.model.Tasks taskList = m_tasksService
				.tasks()
				.list(listId)
				.execute();
			
		if (taskList == null)
			return null;
		
		return taskList.getItems();
	}

	public List<TaskList> getTasksLists() 
			throws IOException {
		TaskLists taskList = m_tasksService
			.tasklists()
			.list()
			.execute();
		
		if (taskList == null)
			return null;
		
		return taskList.getItems();
	}
	
	public TaskList getTasksList(String listId) 
		throws IOException {
			
		return m_tasksService
			.tasklists()
			.get(listId)
			.execute();
		}
}
