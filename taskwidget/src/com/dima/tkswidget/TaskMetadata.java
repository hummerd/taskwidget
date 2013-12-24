package com.dima.tkswidget;

import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;
import android.util.SparseArray;

public class TaskMetadata {
	public static class EntityMetaInfo {
		public final String TABLE_NAME;
		public final String COL_ID;
		public final String COL_NULL;
		public final String DEFAULT_SORT_ORDER;
		public final Uri CONTENT_DIR;
		public final Uri CONTENT_ITEM;
		public final Uri CONTENT_ITEM_PATTERN;
		public final int PATH_ID_POS;
		public final String ITEM_CONTENT_TYPE;
		public final String DIR_CONTENT_TYPE;
		
		public EntityMetaInfo(
				String tableName, 
				String idColumn,
				String nullColumn,
				String defaultSortOrder,
				Uri contentDir,
				Uri contentItem,
				Uri contentItemPattern,
				int idPathPos,
				String itemContentType,
				String dirContentType) {
			TABLE_NAME = tableName;
			COL_ID = idColumn;
			COL_NULL = nullColumn;
			DEFAULT_SORT_ORDER = defaultSortOrder;
			CONTENT_DIR = contentDir;
			CONTENT_ITEM = contentItem;
			CONTENT_ITEM_PATTERN = contentItemPattern;
			PATH_ID_POS = idPathPos;
			ITEM_CONTENT_TYPE = itemContentType;
			DIR_CONTENT_TYPE = dirContentType;
		}
	}
	
	public static final String AUTHORITY = "com.dima.tkswidget.provider";
	
	
	public static final String COL_ID = "id";
	public static final String COL_TITLE = "title";
	public static final String COL_STATUS = "status";
	public static final String COL_CREATE_DATE = "date";
    public static final String COL_POSITION = "position";
	public static final String COL_PARENT_TASK_ID = "parent_id";
	public static final String COL_PARENT_LIST_ID = "list_id";
	
	public static final EntityMetaInfo TASK_INFO = new EntityMetaInfo(
		"task",
		COL_ID,
		COL_TITLE,
		COL_CREATE_DATE + " DESC",
		Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/tasks"),
		Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/task"),
		Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/task/*"),
		1,
		"vnd.android.cursor.item/vnd." + AUTHORITY + ".task",
		"vnd.android.cursor.dir/vnd." + AUTHORITY + ".task"
		);
	
	
	
	public static final String COL_TL_ID = "id";
	public static final String COL_TL_TITLE = "title";
	public static final String COL_TL_CREATE_DATE = "date";

	public static final EntityMetaInfo TASK_LIST_INFO = new EntityMetaInfo(
		"task_list",
		COL_TL_ID,
		COL_TL_TITLE,
		COL_TL_CREATE_DATE + " DESC",
		Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/tasklists"),
		Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/tasklist"),
		Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/tasklist/*"),
		1,
		"vnd.android.cursor.item/vnd." + AUTHORITY + ".tasklist",
		"vnd.android.cursor.dir/vnd." + AUTHORITY + ".tasklist"
		);

	
	
	public static final int URI_KIND_TASKS = 1;
	public static final int URI_KIND_TASK_ID = 2;
	public static final int URI_KIND_LISTS = 3;
	public static final int URI_KIND_LIST_ID = 4;
	
    public static int CheckUriKind(Uri uri)
    {
    	return s_uriMatcher.match(uri);
    }
    
    public static EntityMetaInfo GetUriMetdata(Uri uri)
    {
    	int match = s_uriMatcher.match(uri);
    	return s_uriMeta.get(match);
    }
    
    
	private static final UriMatcher s_uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final SparseArray<EntityMetaInfo> s_uriMeta = new SparseArray<EntityMetaInfo>(4);
	
    static {
		s_uriMatcher.addURI(AUTHORITY, "tasks", URI_KIND_TASKS);
		s_uriMatcher.addURI(AUTHORITY, "task/*", URI_KIND_TASK_ID);
		
		s_uriMatcher.addURI(AUTHORITY, "tasklists", URI_KIND_LISTS);
		s_uriMatcher.addURI(AUTHORITY, "tasklist/*", URI_KIND_LIST_ID);
	
		s_uriMeta.put(URI_KIND_TASKS, TASK_INFO);
		s_uriMeta.put(URI_KIND_TASK_ID, TASK_INFO);
		s_uriMeta.put(URI_KIND_LISTS, TASK_LIST_INFO);
		s_uriMeta.put(URI_KIND_LIST_ID, TASK_LIST_INFO);
    }
}
