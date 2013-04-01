package dima.soft;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;


public class GoogleTasksLoader {
	protected static final String TASKS_APP_KEY = "AIzaSyC0HLKFQZbYA0iolnXzNo5WNItMNOomLtg";
	protected static final String APP_NAME = "com.dima.googletaskswidget";
	
	protected GoogleAccountCredential m_googleAccess;
	protected Tasks m_tasksService;
	
	
	public GoogleTasksLoader(GoogleAccountCredential googleAccess) {
		m_googleAccess = googleAccess;
		
		HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
		m_tasksService = new Tasks.Builder(httpTransport, new GsonFactory(), m_googleAccess)
			.setApplicationName(APP_NAME)
			.build();
		
//		.build().(httpTransport, new JacksonFactory())
//			.setApplicationName(APP_NAME)
//	    	.setHttpRequestInitializer(m_googleAccess)
//	    	.setJsonHttpRequestInitializer(new JsonHttpRequestInitializer() {
//	              public void initialize(JsonHttpRequest request) throws IOException {
//	                TasksRequest tasksRequest = (TasksRequest) request;
//	                tasksRequest.setKey(TASKS_APP_KEY);
//	              }
//	            })
//	        .build();
		
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
