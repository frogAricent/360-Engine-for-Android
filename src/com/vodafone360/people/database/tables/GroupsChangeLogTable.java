package com.vodafone360.people.database.tables;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.vodafone360.people.Settings;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.database.tables.ContactChangeLogTable.ContactChangeType;
import com.vodafone360.people.database.tables.GroupsTable.Field;
import com.vodafone360.people.datatypes.ContactDetail;
import com.vodafone360.people.datatypes.GroupItem;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.utils.CloseUtils;
import com.vodafone360.people.utils.LogUtils;

public class GroupsChangeLogTable {
	
	/**
     * Name of the table as it appears in the database
     */
    private static final String TABLE_NAME = "GroupsChangeLog";
    
    /**
     * An enumeration of all the field names in the database.
     */
    public static enum Field {
    	NAME("Name"),
        GROUPID("GroupId"),
        ISADDED("IsAdded"),
        CONTACTLIST("ContactList");

        /**
         * The name of the field as it appears in the database
         */
        private String mField;

        /**
         * Constructor
         * 
         * @param field - The name of the field (see list above)
         */
        private Field(String field) {
            mField = field;
        }

        /**
         * @return the name of the field as it appears in the database.
         */
        public String toString() {
            return mField;
        }

    }
    
    /**
     * Enumerator for type of changes supported by the table
     * @author bgh21577
     *
     */
    public static enum GroupChangeType {
        ADD_GROUP,
        DELETE_GROUP
    }
    
    /**
     * Wraps up the data present in the change log table
     */
    public static class GroupChangeInfo {
        public Long mGroupId = null;

        public GroupChangeType mType = null;

        public String mGroupName = null;
        
        public String mContactList = null;

        /**
         * Converts the encapsulated data into a string that can be displayed
         * for debug purposes.
         */
        @Override
        public String toString() {
            return "Group Change ID: " + mGroupId + "\n" + "Is added: "
                    + mType + "\n" + "Group Name: " + mGroupName + "\n" + "Group Contacts: " + mContactList ;
        }
    }
    
    
    
    
    /**
     * Creates Groups Table and populate it with system groups.
     * 
     * @param context A context for reading strings from the resources
     * @param writeableDb A writable SQLite database
     * @throws SQLException If an SQL compilation error occurs
     */
    //Verify which must be primary key
    public static void create(SQLiteDatabase writableDb) throws SQLException {
        DatabaseHelper.trace(true, "GroupChangeLogTableTable.create()");
        writableDb.execSQL("CREATE TABLE " + TABLE_NAME + " (" + Field.GROUPID
                + " INTEGER PRIMARY KEY, " + Field.NAME+" TEXT, "+ Field.ISADDED +" BOOLEAN, "+ Field.CONTACTLIST +" TEXT);");
    }
    
    /**
     * Fetches the list of table fields that can be injected into an SQL query
     * statement. The {@link #getQueryData(Cursor)} method can be used to obtain
     * the data from the query.
     * 
     * @return The query string
     * @see #getQueryData(Cursor).
     */
    private static String getFullQueryList() {
        return Field.GROUPID + ", " + Field.NAME + ", " + Field.ISADDED + ", " + Field.CONTACTLIST;
    }
    
    /**
     * Returns a full SQL query statement to fetch a set of groups from the
     * table. The {@link #getQueryData(Cursor)} method can be used to obtain the
     * data from the query.
     * 
     * @param whereClause An SQL where clause (without the "WHERE"). Cannot be
     *            null.
     * @return The query string
     * @see #getQueryData(Cursor).
     */
    private static String getQueryStringSql(String whereClause) {
        String whereString = "";
        if (whereClause != null) {
            whereString = " WHERE " + whereClause;
        }
        return "SELECT " + getFullQueryList() + " FROM " + TABLE_NAME + whereString;
    }
    
    /**
     * Column indices which match the query string returned by
     * {@link #getFullQueryList()}.
     */
    
    private static final int GROUPID = 0;
    
    private static final int NAME = 1;
    
    private static final int CHANGETYPE = 2;
    
    private static final int CONTACTLIST = 3;

    /**
     * Fetches the group data from the current record of the given cursor.
     * 
     * @param c Cursor returned by one of the {@link #getFullQueryList()} based
     *            query methods.
     * @return Filled in GroupItem object
     */
    public static GroupItem getQueryData(Cursor c) {
        GroupItem group = new GroupItem();

		if (!c.isNull(GROUPID)) {
			if(c.getLong(GROUPID) > 50)
				group.mId = c.getLong(GROUPID);
			else
				group.mId = null;
		}
		if (!c.isNull(NAME)) {
            group.mName = c.getString(NAME);
        }
        
        return group;
    }
    
    /**
     * Fetches a list of all the available groups.
     * 
     * @param groupList A list that will be populated with the result.
     * @param readableDb Readable SQLite database
     * @return SUCCESS or a suitable error
     */
    public static Cursor fetchGroupListCursor( SQLiteDatabase readableDb) {
        DatabaseHelper.trace(false, "GroupsChangeLogTable.fetchGroupList()");
        Cursor c = null;
        try {
            String query = "SELECT " + getFullQueryList() + " FROM " + TABLE_NAME;
            c = readableDb.rawQuery(query, null);
            /*while (c.moveToNext()) {
                groupList.add(getQueryData(c));
            }*/
            if(c.getCount() == 0){
            	return null;
            }
        } catch (SQLiteException e) {
            LogUtils.logE("GroupsChangeLogTable.fetchGroupList() Exception - Unable to fetch group list", e);
            //return ServiceStatus.ERROR_DATABASE_CORRUPT;
            return null;
        } finally {
            //CloseUtils.close(c);
            //c = null;
        }
        //return ServiceStatus.SUCCESS;
        return c;
    }
    
    /**
     * Returns a ContentValues object that can be used to insert or modify a
     * group in the table.
     * 
     * @param group The source GroupItem object
     * @param contactList The list of contacts related to this group
     * @param operation The operation to be done. Added/edited or deleted
     
     * @return The ContentValues object containing the data.
     * @note NULL fields in the given group will not be included in the
     *       ContentValues
     */
    private static ContentValues fillUpdateData(GroupItem group, List<Long> contactList, boolean operation) {
        ContentValues contactDetailValues = new ContentValues();
        /*********Verify below************/
        
        if (group.mName != null) {
            contactDetailValues.put(Field.NAME.toString(), group.mName);
        }
        if (group.mId != null) {
            contactDetailValues.put(Field.GROUPID.toString(), group.mId);
        }
        
        contactDetailValues.put(Field.ISADDED.toString(), operation);
        if((contactList != null)&&(contactList.size()!= 0)){
        	String contactListString = "";
        	for(Long id: contactList){
        		contactListString += String.valueOf(id) + ";";
        	}
        	contactDetailValues.put(Field.CONTACTLIST.toString(), contactListString);
        }
        
        /*********************************/
        return contactDetailValues;
    }
    
    /**
     * Adds list of groups to the table
     * 
     * @param groupList The list to add
     * @param writableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static Long addGroup(GroupItem groupItem,List<Long> contactIdList, SQLiteDatabase writableDb) {
        try {
        	groupItem.mId = null;
            writableDb.beginTransaction();
            	LogUtils.logD("Adding group:"+groupItem.toString());
                if (Settings.ENABLED_DATABASE_TRACE) {
                    DatabaseHelper.trace(true, "GroupsChangeLogTable.addGroupList() mName["
                            + groupItem.mName + "]");
                }
                groupItem.mLocalGroupId = writableDb.insertOrThrow(TABLE_NAME, null,
                        fillUpdateData(groupItem,contactIdList, true));
                if (groupItem.mLocalGroupId < 0) {
                    LogUtils.logE("GroupsTable.addGroupList() Unable to add group - mName["
                            + groupItem.mName + "");
                    writableDb.endTransaction();
                    //return ServiceStatus.ERROR_DATABASE_CORRUPT;
                    return -1L;
                }else{
    				LogUtils.logD("GroupsChangeLogTable - Group added to local database");
    			}
                LogUtils.logD("Added group:"+groupItem.toString());
            writableDb.setTransactionSuccessful();

        } catch (SQLException e) {
            LogUtils.logE("GroupsTable.addGroupList() SQLException - Unable to add group", e);
            //return ServiceStatus.ERROR_DATABASE_CORRUPT;
            return -1L;

        } finally {
            if (writableDb != null) {
                writableDb.endTransaction();
            }
        }

        //return ServiceStatus.SUCCESS;
        return groupItem.mLocalGroupId;
    }
    
    /**
     * Edits list of groups 
     * 
     * @param groupList The list to add
     * @param writableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus editGroup(GroupItem groupItem,List<Long> contactIdList, SQLiteDatabase writableDb) {
        try {
            writableDb.beginTransaction();
            
                if (Settings.ENABLED_DATABASE_TRACE) {
                    DatabaseHelper.trace(true, "GroupsChangeLogTable.addGroupList() mName["
                            + groupItem.mName + "]");
                }
                groupItem.mLocalGroupId = writableDb.insertOrThrow(TABLE_NAME, 
                		null,
                        fillUpdateData(groupItem,contactIdList, true));
                if (groupItem.mLocalGroupId < 0) {
                    LogUtils.logE("GroupsTable.addGroupList() Unable to add group - mName["
                            + groupItem.mName + "");
                    writableDb.endTransaction();
                    return ServiceStatus.ERROR_DATABASE_CORRUPT;
                }else{
    				LogUtils.logD("GroupsChangeLogTable - Group added to local database");
    			}
            
            writableDb.setTransactionSuccessful();

        } catch (SQLException e) {
            LogUtils.logE("GroupsTable.addGroupList() SQLException - Unable to add group", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;

        } finally {
            if (writableDb != null) {
                writableDb.endTransaction();
            }
        }

        return ServiceStatus.SUCCESS;
    }
    
    /**
     * Adds list of groups to delete to the change log table
     * 
     * @param groupList The list to add
     * @param writableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus deleteGroup(GroupItem groupItem, SQLiteDatabase writableDb) {
    	LogUtils.logD("GroupsChangeLogTable.deleteGroup");
        try {
            writableDb.beginTransaction();
            	LogUtils.logD("Deleting group:"+groupItem.toString());
                groupItem.mLocalGroupId = writableDb.insertOrThrow(TABLE_NAME, null,
                        fillUpdateData(groupItem,null, false));
                if (groupItem.mLocalGroupId < 0) {
                    LogUtils.logE("GroupsTable.addGroupList() Unable to add group - mName["
                            + groupItem.mName + "");
                    writableDb.endTransaction();
                    return ServiceStatus.ERROR_DATABASE_CORRUPT;
                }else{
    				LogUtils.logD("GroupsChangeLogTable - Group delete request added to local database");
    			}
                //LogUtils.logD("deleting group:"+groupItem.toString());
            writableDb.setTransactionSuccessful();

        
        } catch (SQLException e) {
            LogUtils.logE("GroupsTable.DeleteGroupList() SQLException - Unable to delete group", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;

        } finally {
            if (writableDb != null) {
                writableDb.endTransaction();
            }
        }
        return ServiceStatus.SUCCESS;
    }
    
    
    
    /**
     * Removes all groups from the table. The
     * {@link #populateSystemGroups(Context, SQLiteDatabase)} function should be
     * called afterwards to ensure the system groups are restored.
     * 
     * @param writableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus deleteAllGroups(SQLiteDatabase writableDb, SQLiteDatabase readableDb) {
        DatabaseHelper.trace(true, "GroupsChangeLogTable.deleteAllGroups()");
        try {
            if (writableDb.delete(TABLE_NAME, null, null) < 0) {
                LogUtils.logE("GroupsChangeLogTable.deleteAllGroups() Unable to delete all groups");
                return ServiceStatus.ERROR_DATABASE_CORRUPT;
            }else{
            	LogUtils.logD("GroupsChangeLogTable.deleteAllgroups - All groups have been deleted.");
            	ArrayList<GroupItem> grpList = new ArrayList<GroupItem>();
            	fetchUploadGroupList(grpList, readableDb);
            	LogUtils.logD("Groups to be uploaded:"+grpList.size());
            	ArrayList<Long> grpIdList = new ArrayList<Long>();
            	fetchDeleteGroupList(grpIdList, readableDb);
            	LogUtils.logD("Groups to be uploaded:"+grpList.size());
            }

        } catch (SQLException e) {
            LogUtils.logE(
                    "GroupsChangeLogTable.deleteAllGroups() SQLException - Unable to delete all groups", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        }
        return ServiceStatus.SUCCESS;
    }
    
    /**
	 * Fetches a list of all the available groups for upload.
	 * 
	 * @param groupList A list that will be populated with the result.
	 * @param readableDb Readable SQLite database
	 * @return SUCCESS or a suitable error
	 */
	public static ServiceStatus fetchUploadGroupList(ArrayList<GroupItem> groupList,
			SQLiteDatabase readableDb) {
		DatabaseHelper.trace(false, "GroupsTableNew.fetchUploadGroupList()");
		Cursor c = null;
		try {
			String query = "SELECT " + /*getUploadQueryList()*/"*" + " FROM "
			+ TABLE_NAME + " WHERE " + Field.ISADDED
			+ "='1'";
			c = readableDb.rawQuery(query, null);
			while (c.moveToNext()) {
				groupList.add(getQueryData(c));
			}
			LogUtils.logD("List size:"+groupList.size());
			for(int i = 0; i < groupList.size(); i++){
				LogUtils.logD(""+groupList.get(i).mName);
			}

		} catch (SQLiteException e) {
			LogUtils.logE("GroupsTableNew.fetchUploadGroupList() Exception - Unable to get Upload group list", e);
			return ServiceStatus.ERROR_DATABASE_CORRUPT;
		} finally {
			CloseUtils.close(c);
			c = null;
		}
		return ServiceStatus.SUCCESS;
	}
	
	/**
	 * Fetches a list of all the available groups for deleting.
	 * 
	 * @param groupIdList A list that will be populated with the result.
	 * @param readableDb Readable SQLite database
	 * @return SUCCESS or a suitable error
	 */
	public static ServiceStatus fetchDeleteGroupList(ArrayList<Long> groupIdList,
			SQLiteDatabase readableDb) {
		DatabaseHelper.trace(false, "GroupsTableNew.fetchDeleteGroupList()");
		groupIdList.clear();
		Cursor c = null;
		try {

			String query = "SELECT " + "*" + " FROM "
			+ TABLE_NAME + " WHERE " + Field.ISADDED + "='0'";
			c = readableDb.rawQuery(query, null);
			while (c.moveToNext()) {
				groupIdList.add(c.getLong(c.getColumnIndex(Field.GROUPID.toString())));
			}
		} catch (SQLiteException e) {
			LogUtils.logE("GroupsTableNew.fetchDeleteGroupList()() Exception - Unable to get deleted group list", e);
			return ServiceStatus.ERROR_DATABASE_CORRUPT;
		} finally {
			CloseUtils.close(c);
			c = null;
		}
		return ServiceStatus.SUCCESS;
	}
	
	/**
	 * Gets the group id
	 *//*
	public static Long getgroupId(Cursor c) {
		Long groupId = -1L;
		if (!c.isNull(GROUPLOCALLID)) {
			LogUtils.logD("GroupId:"+c.getLong(SERVERGROUPID));
			return c.getLong(SERVERGROUPID);
		}
		LogUtils.logD("Default GroupId:"+c.getLong(SERVERGROUPID));
		return groupId;

	}*/
	
	/**
	 * Fetches a list of all the contacts to be added to a particular group.
	 * 
	 * @param groupList A list of newly created groups 
	 * @param readableDb Readable SQLite database
	 * @return SUCCESS or a suitable error
	 */
	public static ServiceStatus fetchGroupContacts(GroupItem group, ArrayList<Long> contactIds,
			SQLiteDatabase readableDb) {
		DatabaseHelper.trace(false, "GroupsTableNew.fetchGroupContacts()");
		ServiceStatus status = ServiceStatus.SUCCESS;
		Cursor c = null;
		contactIds.clear();
		try {
			String query = "SELECT " + Field.CONTACTLIST + " FROM "
			+ TABLE_NAME + " WHERE " + Field.NAME + "='"+group.mName+"'";
			c = readableDb.rawQuery(query, null);
			if (c.getCount() != 0) {
				c.moveToFirst();
				String contList = c.getString(c
						.getColumnIndex(Field.CONTACTLIST.toString()));
				if (contList != null) {
					String[] contListString = new String[50];
					contListString = contList.split(";");
					for (int i = 0; i < contListString.length; i++) {
						contactIds.add(Long.valueOf(contListString[i]));
					}
					LogUtils.logD("Contact List size:" + contactIds.size());
				} else {
					LogUtils.logD("No contacts to be added");
				}
			}

		} catch (SQLiteException e) {
			LogUtils.logE("GroupsTableNew.fetchUploadGroupList() Exception - Unable to get contact list for group "+group.mName, e);
			status = ServiceStatus.ERROR_DATABASE_CORRUPT;
		} finally {
			if (c != null) {
				CloseUtils.close(c);
				c = null;
			}
		}
		
		return status;
	}
	
	
	/**
     * Delete a group from the change log table
     * @param groupItem The group to delete
     * @param writableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus removeGroupFromTable(GroupItem groupItem, SQLiteDatabase writableDb) {
    	LogUtils.logD("GroupsChangeLogTable.removeGroupFromTable");
        try {
            writableDb.beginTransaction();
            	LogUtils.logD("Deleting group:"+groupItem.toString());
            	writableDb.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+Field.NAME+"='"+groupItem.mName+"'");
                
            writableDb.setTransactionSuccessful();
        } catch (SQLException e) {
            LogUtils.logE("GroupsTable.removeGroupFromTable() SQLException - Unable to delete group", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        } finally {
            if (writableDb != null) {
                writableDb.endTransaction();
            }
        }
        return ServiceStatus.SUCCESS;
    }
    

}
