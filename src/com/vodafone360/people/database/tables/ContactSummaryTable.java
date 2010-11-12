/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at
 * src/com/vodafone360/people/VODAFONE.LICENSE.txt or
 * http://github.com/360/360-Engine-for-Android
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at src/com/vodafone360/people/VODAFONE.LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright 2010 Vodafone Sales & Services Ltd.  All rights reserved.
 * Use is subject to license terms.
 */

package com.vodafone360.people.database.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;

import com.vodafone360.people.Settings;
import com.vodafone360.people.database.DatabaseHelper;
import com.vodafone360.people.database.SQLKeys;
import com.vodafone360.people.database.tables.ContactDetailsTable.Field;
import com.vodafone360.people.database.tables.ContactsTable.ContactIdInfo;
import com.vodafone360.people.datatypes.ActivityItem;
import com.vodafone360.people.datatypes.Contact;
import com.vodafone360.people.datatypes.ContactDetail;
import com.vodafone360.people.datatypes.ContactSummary;
import com.vodafone360.people.datatypes.VCardHelper;
import com.vodafone360.people.datatypes.ContactDetail.DetailKeys;
import com.vodafone360.people.datatypes.ContactSummary.AltFieldType;
import com.vodafone360.people.datatypes.ContactSummary.OnlineStatus;
import com.vodafone360.people.engine.presence.User;
import com.vodafone360.people.service.ServiceStatus;
import com.vodafone360.people.utils.CloseUtils;
import com.vodafone360.people.utils.LogUtils;
import com.vodafone360.people.utils.StringBufferPool;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MergeCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

/**
 * The ContactSummaryTable contains a summary of important contact details for
 * each contact such as name, status and Avatar availability. This data is
 * duplicated here to improve the performance of the main contact list in the UI
 * (otherwise the a costly inner join between the contact and contact details
 * table would be needed). This class is never instantiated hence all methods
 * must be static.
 * 
 * @version %I%, %G%
 */
public abstract class ContactSummaryTable {
    /**
     * The name of the table as it appears in the database.
     */
    public static final String TABLE_NAME = "ContactSummary";
    
    public static final String TABLE_INDEX_NAME = "ContactSummaryIndex";
    
    /**
     * This holds the presence information for each contact in the ContactSummaryTable
     */
    private static HashMap<Long, Integer> sPresenceMap = new HashMap<Long, Integer>();


    /**
     * An enumeration of all the field names in the database.
     */
    public static enum Field {
        SUMMARYID("_id"),
        LOCALCONTACTID("LocalContactId"),
        DISPLAYNAME("DisplayName"),
        STATUSTEXT("StatusText"),
        ALTFIELDTYPE("AltFieldType"),
        ALTDETAILTYPE("AltDetailType"),
        ONLINESTATUS("OnlineStatus"),
        NATIVEID("NativeId"),
        FRIENDOFMINE("FriendOfMine"),
        PICTURELOADED("PictureLoaded"),
        SNS("Sns"),
        SYNCTOPHONE("Synctophone");

        /**
         * The name of the field as it appears in the database
         */
        private final String mField;

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
     * Creates ContactSummary Table.
     * 
     * @param writeableDb A writable SQLite database
     * @throws SQLException If an SQL compilation error occurs
     */
    public static void create(SQLiteDatabase writeableDb) throws SQLException {
        DatabaseHelper.trace(true, "ContactSummaryTable.create()");
        //TODO: As of now kept the onlinestatus field in table. Would remove it later on
        writeableDb.execSQL("CREATE TABLE " + TABLE_NAME + " (" + Field.SUMMARYID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Field.LOCALCONTACTID + " LONG, "
                + Field.DISPLAYNAME + " TEXT, " + Field.STATUSTEXT + " TEXT, " + Field.ALTFIELDTYPE
                + " INTEGER, " + Field.ALTDETAILTYPE + " INTEGER, " + Field.ONLINESTATUS
                + " INTEGER, " + Field.NATIVEID + " INTEGER, " + Field.FRIENDOFMINE + " BOOLEAN, "
                + Field.PICTURELOADED + " BOOLEAN, " + Field.SNS + " STRING, " + Field.SYNCTOPHONE
                + " BOOLEAN);");
        
        writeableDb.execSQL("CREATE INDEX " + TABLE_INDEX_NAME + " ON " + TABLE_NAME + " ( " + Field.LOCALCONTACTID + ", " + Field.DISPLAYNAME + " )");
        clearPresenceMap();
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
        return Field.SUMMARYID + ", " + TABLE_NAME + "." + Field.LOCALCONTACTID + ", "
                + Field.DISPLAYNAME + ", " + Field.STATUSTEXT + ", " + Field.ONLINESTATUS + ", "
                + Field.NATIVEID + ", " + Field.FRIENDOFMINE + ", " + Field.PICTURELOADED + ", "
                + Field.SNS + ", " + Field.SYNCTOPHONE + ", " + Field.ALTFIELDTYPE + ", "
                + Field.ALTDETAILTYPE;
    }

    /**
     * Returns a full SQL query statement to fetch the contact summary
     * information. The {@link #getQueryData(Cursor)} method can be used to
     * obtain the data from the query.
     * 
     * @return The query string
     * @see #getQueryData(Cursor).
     */
    private static String getOrderedQueryStringSql() {
        return "SELECT " + getFullQueryList() + " FROM " + TABLE_NAME + " ORDER BY LOWER("
                + Field.DISPLAYNAME + ")";
    }

    /**
     * Returns a full SQL query statement to fetch the contact summary
     * information. The {@link #getQueryData(Cursor)} method can be used to
     * obtain the data from the query.
     * 
     * @param whereClause An SQL where clause (without the "WHERE"). Cannot be
     *            null.
     * @return The query string
     * @see #getQueryData(Cursor).
     */
    private static String getQueryStringSql(String whereClause) {
        return "SELECT " + getFullQueryList() + " FROM " + TABLE_NAME + " WHERE " + whereClause;
    }

    /**
     * Returns a full SQL query statement to fetch the contact summary
     * information in alphabetical order of contact name. The
     * {@link #getQueryData(Cursor)} method can be used to obtain the data from
     * the query.
     * 
     * @param whereClause An SQL where clause (without the "WHERE"). Cannot be
     *            null.
     * @return The query string
     * @see #getQueryData(Cursor).
     */
    private static String getOrderedQueryStringSql(String whereClause) {
        return "SELECT " + getFullQueryList() + " FROM " + TABLE_NAME + " WHERE " + whereClause
                + " ORDER BY LOWER(" + Field.DISPLAYNAME + ")";
    }
    
    /**
     * UPDATE ContactSummary SET
     * NativeId = ?
     * WHERE LocalContactId = ?
     */
    private static final String UPDATE_NATIVE_ID_BY_LOCAL_CONTACT_ID = "UPDATE " + 
        TABLE_NAME + " SET " + Field.NATIVEID + "=? WHERE " + Field.LOCALCONTACTID + "=?";

    /**
     * Column indices which match the query string returned by
     * {@link #getFullQueryList()}.
     */
    public static final int SUMMARY_ID = 0;

    public static final int LOCALCONTACT_ID = 1;

    public static final int FORMATTED_NAME = 2;

    public static final int STATUS_TEXT = 3;

    @Deprecated
    public static final int ONLINE_STATUS = 4;

    public static final int NATIVE_CONTACTID = 5;

    public static final int FRIEND_MINE = 6;

    public static final int PICTURE_LOADED = 7;

    public static final int SNS = 8;

    public static final int SYNCTOPHONE = 9;

    public static final int ALTFIELD_TYPE = 10;

    public static final int ALTDETAIL_TYPE = 11;

    /**
     * Fetches the contact summary data from the current record of the given
     * cursor.
     * 
     * @param c Cursor returned by one of the {@link #getFullQueryList()} based
     *            query methods.
     * @return Filled in ContactSummary object
     */
    public static ContactSummary getQueryData(Cursor c) {
        ContactSummary contactSummary = new ContactSummary();

        if (!c.isNull(SUMMARY_ID)) {
            contactSummary.summaryID = c.getLong(SUMMARY_ID);
        }
        if (!c.isNull(LOCALCONTACT_ID)) {
            contactSummary.localContactID = c.getLong(LOCALCONTACT_ID);
        }
        contactSummary.formattedName = c.getString(FORMATTED_NAME);
        contactSummary.statusText = c.getString(STATUS_TEXT);
        
        contactSummary.onlineStatus = getPresence(contactSummary.localContactID);
        
        if (!c.isNull(NATIVE_CONTACTID)) {
            contactSummary.nativeContactId = c.getInt(NATIVE_CONTACTID);
        }
        if (!c.isNull(FRIEND_MINE)) {
            contactSummary.friendOfMine = (c.getInt(FRIEND_MINE) == 0 ? false : true);
        }
        if (!c.isNull(PICTURE_LOADED)) {
            contactSummary.pictureLoaded = (c.getInt(PICTURE_LOADED) == 0 ? false : true);
        }
        if (!c.isNull(SNS)) {
            contactSummary.sns = c.getString(SNS);
        }
        if (!c.isNull(SYNCTOPHONE)) {
            contactSummary.synctophone = (c.getInt(SYNCTOPHONE) == 0 ? false : true);
        }
        if (!c.isNull(ALTFIELD_TYPE)) {
            int val = c.getInt(ALTFIELD_TYPE);
            if (val < AltFieldType.values().length) {
                contactSummary.altFieldType = AltFieldType.values()[val];
            }
        }
        if (!c.isNull(ALTDETAIL_TYPE)) {
            int val = c.getInt(ALTDETAIL_TYPE);
            if (val < ContactDetail.DetailKeys.values().length) {
                contactSummary.altDetailType = ContactDetail.DetailKeyTypes.values()[val];
            }
        }
        return contactSummary;
    }

    /**
     * Fetches the contact summary for a particular contact
     * 
     * @param localContactID The primary key ID of the contact to find
     * @param summary A new ContactSummary object to be filled in
     * @param readableDb Readable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus fetchSummaryItem(long localContactId, ContactSummary summary,
            SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchSummaryItem() localContactId["
                    + localContactId + "]");
        }
        Cursor c1 = null;
        try {
            c1 = readableDb.rawQuery(
                    getQueryStringSql(Field.LOCALCONTACTID + "=" + localContactId), null);
            if (!c1.moveToFirst()) {
                LogUtils.logW("ContactSummeryTable.fetchSummaryItem() localContactId["
                        + localContactId + "] not found in ContactSummeryTable.");
                return ServiceStatus.ERROR_NOT_FOUND;
            }
            summary.copy(getQueryData(c1));
            return ServiceStatus.SUCCESS;
        } catch (SQLiteException e) {
            LogUtils
                    .logE(
                            "ContactSummeryTable.fetchSummaryItem() Exception - Unable to fetch contact summary",
                            e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        } finally {
            CloseUtils.close(c1);
            c1 = null;
        }
    }

    /**
     * Processes a ContentValues object to handle a missing name or missing
     * status.
     * <ol>
     * <li>If the name is missing it will be replaced using the alternative
     * detail.</li>
     * <li>If the name is present, but status is missing the status will be
     * replaced using the alternative detail</li>
     * <li>Otherwise, the althernative detail is not used</li>
     * </ol>
     * In any case the {@link Field#ALTFIELDTYPE} value will be updated to
     * reflect how the alternative detail is being used.
     * 
     * @param values The ContentValues object to be updated
     * @param altDetail The must suitable alternative detail (see
     *            {@link #fetchNewAltDetail(long, ContactDetail, SQLiteDatabase)}
     */
    private static void updateAltValues(ContentValues values, ContactDetail altDetail) {
        if (!values.containsKey(Field.DISPLAYNAME.toString())) {
            values.put(Field.DISPLAYNAME.toString(), altDetail.getValue());
            values.put(Field.ALTFIELDTYPE.toString(), ContactSummary.AltFieldType.NAME.ordinal());
        } else if (!values.containsKey(Field.STATUSTEXT.toString())) {
            values.put(Field.STATUSTEXT.toString(), altDetail.getValue());
            values.put(Field.ALTFIELDTYPE.toString(), ContactSummary.AltFieldType.STATUS.ordinal());
        } else {
            values.put(Field.ALTFIELDTYPE.toString(), ContactSummary.AltFieldType.UNUSED.ordinal());
        }
        if (altDetail.keyType != null) {
            values.put(Field.ALTDETAILTYPE.toString(), altDetail.keyType.ordinal());
        }
    }

    /**
     * Processes a ContentValues object to handle a missing name or missing
     * status.
     * <ol>
     * <li>If type is NAME, the name will be set to the alternative detail.</li>
     * <li>If type is STATUS, the status will be set to the alternative detail</li>
     * <li>Otherwise, the alternative detail is not used</li>
     * </ol>
     * In any case the {@link Field#ALTFIELDTYPE} value will be updated to
     * reflect how the alternative detail is being used.
     * 
     * @param values The ContentValues object to be updated
     * @param altDetail The must suitable alternative detail (see
     *            {@link #fetchNewAltDetail(long, ContactDetail, SQLiteDatabase)}
     * @param type Specifies how the alternative detail should be used
     */
    /*
     * private static void updateAltValues(ContentValues values, ContactDetail
     * altDetail, ContactSummary.AltFieldType type) { switch (type) { case NAME:
     * values.put(Field.DISPLAYNAME.toString(), altDetail.getValue());
     * values.put(Field.ALTFIELDTYPE.toString(),
     * ContactSummary.AltFieldType.NAME .ordinal()); break; case STATUS:
     * values.put(Field.STATUSTEXT.toString(), altDetail.getValue());
     * values.put(Field.ALTFIELDTYPE.toString(),
     * ContactSummary.AltFieldType.STATUS .ordinal()); break; default:
     * values.put(Field.ALTFIELDTYPE.toString(),
     * ContactSummary.AltFieldType.UNUSED .ordinal()); } if (altDetail.keyType
     * != null) { values.put(Field.ALTDETAILTYPE.toString(),
     * altDetail.keyType.ordinal()); } }
     */

    /**
     * Adds contact summary information to the table for a new contact. If the
     * contact has no name or no status, an alternative detail will be used such
     * as telephone number or email address.
     * 
     * @param contact The new contact
     * @param writableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus addContact(Contact contact, SQLiteDatabase writableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(true, "ContactSummeryTable.addContact() contactID["
                    + contact.contactID + "]");
        }
        if (contact.localContactID == null) {
            LogUtils.logE("ContactSummeryTable.addContact() Invalid parameters");
            return ServiceStatus.ERROR_NOT_FOUND;
        }
        try {
            final ContentValues values = new ContentValues();
            values.put(Field.LOCALCONTACTID.toString(), contact.localContactID);
            values.put(Field.NATIVEID.toString(), contact.nativeContactId);
            values.put(Field.FRIENDOFMINE.toString(), contact.friendOfMine);
            values.put(Field.SYNCTOPHONE.toString(), contact.synctophone);

            ContactDetail altDetail = findAlternativeNameContactDetail(values, contact.details);
            updateAltValues(values, altDetail);
            //put sns details in it 
            //TODO: need to check on this
            String sns = isIMAddresPresent(contact.details);
            if(sns != null){    
                values.put(Field.SNS.toString(),sns);
            }
            

            addToPresenceMap(contact.localContactID);

            if (writableDb.insertOrThrow(TABLE_NAME, null, values) < 0) {
                LogUtils.logE("ContactSummeryTable.addContact() "
                        + "Unable to insert new contact summary");
                return ServiceStatus.ERROR_NOT_FOUND;
            }
            return ServiceStatus.SUCCESS;

        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.addContact() SQLException - "
                    + "Unable to insert new contact summary", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        }
    }

    private static String isIMAddresPresent(List<ContactDetail> details) {
        int count = details.size() -1;
        while(count >= 0){
            ContactDetail detail = details.get(count);
            if( detail.key == DetailKeys.VCARD_IMADDRESS){
                if( detail.value != null){
                   return detail.alt;
                   }
            }
            count--;
        }
        return null;
              
    }

    /**
     * This method returns the most preferred contact detail to be displayed
     * instead of the contact name when vcard.name is missing.
     * 
     * @param values - ContentValues to be stored in the DB for the added
     *            contact
     * @param details - the list of all contact details for the contact being
     *            added
     * @return the contact detail most suitable to replace the missing
     *         vcard.name. "Value" field may be empty if no suitable contact
     *         detail was found.
     */
    private static ContactDetail findAlternativeNameContactDetail(ContentValues values,
            List<ContactDetail> details) {
        ContactDetail altDetail = new ContactDetail();
        for (ContactDetail detail : details) {
            getContactValuesFromDetail(values, detail);
            if (isPreferredAltDetail(detail, altDetail)) {
                altDetail.copy(detail);
            }
        }
        return altDetail;
    }

    /**
     * Deletes a contact summary record
     * 
     * @param localContactID The primary key ID of the contact to delete
     * @param writableDb Writeable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus deleteContact(Long localContactId, SQLiteDatabase writableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(true, "ContactSummeryTable.deleteContact() localContactId["
                    + localContactId + "]");
        }
        if (localContactId == null) {
            LogUtils.logE("ContactSummeryTable.deleteContact() Invalid parameters");
            return ServiceStatus.ERROR_NOT_FOUND;
        }
        try {
            if (writableDb.delete(TABLE_NAME, Field.LOCALCONTACTID + "=" + localContactId, null) <= 0) {
                LogUtils.logE("ContactSummeryTable.deleteContact() "
                        + "Unable to delete contact summary");
                return ServiceStatus.ERROR_NOT_FOUND;
            }
            deleteFromPresenceMap(localContactId);
            return ServiceStatus.SUCCESS;

        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.deleteContact() SQLException - "
                    + "Unable to delete contact summary", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        }
    }

    /**
     * Modifies contact parameters. Called when fields in the Contacts table
     * have been changed.
     * 
     * @param contact The modified contact
     * @param writableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus modifyContact(Contact contact, SQLiteDatabase writableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(true, "ContactSummeryTable.modifyContact() contactID["
                    + contact.contactID + "]");
        }
        if (contact.localContactID == null) {
            LogUtils.logE("ContactSummeryTable.modifyContact() Invalid parameters");
            return ServiceStatus.ERROR_NOT_FOUND;
        }
        try {
            final ContentValues values = new ContentValues();
            values.put(Field.LOCALCONTACTID.toString(), contact.localContactID);
            values.put(Field.NATIVEID.toString(), contact.nativeContactId);
            values.put(Field.FRIENDOFMINE.toString(), contact.friendOfMine);
            values.put(Field.SYNCTOPHONE.toString(), contact.synctophone);
            String[] args = {
                String.format("%d", contact.localContactID)
            };
            if (writableDb.update(TABLE_NAME, values, Field.LOCALCONTACTID + "=?", args) < 0) {
                LogUtils.logE("ContactSummeryTable.modifyContact() "
                        + "Unable to update contact summary");
                return ServiceStatus.ERROR_NOT_FOUND;
            }
            return ServiceStatus.SUCCESS;
        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.modifyContact() "
                    + "SQLException - Unable to update contact summary", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        }
    }

    /**
     * Adds suitable entries to a ContentValues objects for inserting or
     * updating the contact summary table, from a contact detail.
     * 
     * @param contactValues The content values object to update
     * @param newDetail The new or modified detail
     * @return true if the summary table has been updated, false otherwise
     */
    private static boolean getContactValuesFromDetail(ContentValues contactValues,
            ContactDetail newDetail) {
        switch (newDetail.key) {
            case VCARD_NAME:
                if (newDetail.value != null) {
                    VCardHelper.Name name = newDetail.getName();
                    if (name != null) {
                        String nameStr = name.toString();
                        // this is what we do to display names of contacts
                        // coming from server
                        if (nameStr.length() > 0) {
                            contactValues.put(Field.DISPLAYNAME.toString(), name.toString());
                        }
                    }
                }
                return true;
//            case PRESENCE_TEXT:
//                if (newDetail.value != null && newDetail.value.length() > 0) {
//                    contactValues.put(Field.STATUSTEXT.toString(), newDetail.value);
//                    contactValues.put(Field.SNS.toString(), newDetail.alt);
//                }
//                return true;
            case PHOTO:
                if (newDetail.value == null) {
                    contactValues.put(Field.PICTURELOADED.toString(), (Boolean)null);
                } else {
                    contactValues.put(Field.PICTURELOADED.toString(), false);
                }
                return true;
            default:
                // Do Nothing.
        }
        return false;
    }

    /**
     * Determines if a contact detail should be used in preference to the
     * current alternative detail (the alternative detail is one that is shown
     * when a contact has no name or no status).
     * 
     * @param newDetail The new detail
     * @param currentDetail The current alternative detail
     * @return true if the new detail should be used, false otherwise
     */
    private static boolean isPreferredAltDetail(ContactDetail newDetail, ContactDetail currentDetail) {
        // this means we'll update the detail
        if (currentDetail.key == null || (currentDetail.key == DetailKeys.UNKNOWN)) {
            return true;
        }
        switch (newDetail.key) {
            case VCARD_PHONE:
                // AA:EMAIL,IMADDRESS,ORG will not be updated, PHONE will
                // consider "preferred" detail check
                switch (currentDetail.key) {
                    case VCARD_EMAIL:
                    case VCARD_IMADDRESS:
                    case VCARD_ORG:
                    case VCARD_ADDRESS:
                    case VCARD_BUSINESS:
                    case VCARD_TITLE:
                    case VCARD_ROLE:
                        return false;
                    case VCARD_PHONE:
                        break;
                    default:
                        return true;
                }
                break;
            case VCARD_IMADDRESS:
                // AA:will be updating everything, except for EMAIL and ORG, and
                // IMADDRESS, when preferred details needs to be considered
                // first
                switch (currentDetail.key) {
                    case VCARD_IMADDRESS:
                        break;
                    case VCARD_EMAIL:
                    case VCARD_ORG:
                    case VCARD_ROLE:
                    case VCARD_TITLE:
                        return false;
                    default:
                        return true;
                }
                break;
            case VCARD_ADDRESS:
            case VCARD_BUSINESS:
                // AA:will be updating everything, except for EMAIL and ORG,
                // when preferred details needs to be considered first
                switch (currentDetail.key) {
                    case VCARD_EMAIL:
                    case VCARD_ORG:
                    case VCARD_ROLE:
                    case VCARD_TITLE:
                        return false;
                    case VCARD_ADDRESS:
                    case VCARD_BUSINESS:
                        break;
                    default:
                        return true;
                }
                break;
            case VCARD_ROLE:
            case VCARD_TITLE:
                // AA:will be updating everything, except for EMAIL and ORG,
                // when preferred details needs to be considered first
                switch (currentDetail.key) {
                    case VCARD_EMAIL:
                    case VCARD_ORG:
                        return false;
                    case VCARD_ROLE:
                    case VCARD_TITLE:
                        break;
                    default:
                        return true;
                }
                break;
            case VCARD_ORG:
                // AA:will be updating everything, except for EMAIL and ORG,
                // when preferred details needs to be considered first
                switch (currentDetail.key) {
                    case VCARD_EMAIL:
                        return false;
                    case VCARD_ORG:
                        break;
                    default:
                        return true;
                }
                break;
            case VCARD_EMAIL:
                // AA:will be updating everything, except for EMAIL, when
                // preferred details needs to be considered first
                switch (currentDetail.key) {
                    case VCARD_EMAIL:
                        break;
                    default:
                        return true;
                }
                break;

            default:
                return false;
        }
        if (currentDetail.order == null) {
            return true;
        }
        if (newDetail.order != null && newDetail.order.compareTo(currentDetail.order) < 0) {
            return true;
        }
        return false;
    }

    /**
     * Fetches a list of native contact IDs from the summary table (in ascending
     * order)
     * 
     * @param summaryList A list that will be populated by this function
     * @param readableDb Readable SQLite database
     * @return true if successful, false otherwise
     */
    public static boolean fetchNativeContactIdList(List<Integer> summaryList,
            SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchNativeContactIdList()");
        }
        summaryList.clear();
        Cursor c = null;
        try {
            c = readableDb.rawQuery("SELECT " + Field.NATIVEID + " FROM " + TABLE_NAME + " WHERE "
                    + Field.NATIVEID + " IS NOT NULL" + " ORDER BY " + Field.NATIVEID, null);
            while (c.moveToNext()) {
                if (!c.isNull(0)) {
                    summaryList.add(c.getInt(0));
                }
            }
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            CloseUtils.close(c);
            c = null;
        }
    }

    /**
     * Modifies the avatar loaded flag for a particular contact
     * 
     * @param localContactID The primary key ID of the contact
     * @param value Can be one of the following values:
     *            <ul>
     *            <li>true - The avatar has been loaded</li>
     *            <li>false - There contact has an avatar but it has not yet
     *            been loaded</li>
     *            <li>null - The contact does not have an avatar</li>
     *            </ul>
     * @param writeableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus modifyPictureLoadedFlag(Long localContactId, Boolean value,
            SQLiteDatabase writeableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(true,
                    "ContactSummeryTable.modifyPictureLoadedFlag() localContactId["
                            + localContactId + "] value[" + value + "]");
        }
        try {
            ContentValues cv = new ContentValues();
            cv.put(Field.PICTURELOADED.toString(), value);
            String[] args = {
                String.format("%d", localContactId)
            };
            if (writeableDb.update(TABLE_NAME, cv, Field.LOCALCONTACTID + "=?", args) <= 0) {
                LogUtils.logE("ContactSummeryTable.modifyPictureLoadedFlag() "
                        + "Unable to modify picture loaded flag");
                return ServiceStatus.ERROR_NOT_FOUND;
            }
        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.modifyPictureLoadedFlag() "
                    + "SQLException - Unable to modify picture loaded flag", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        }
        return ServiceStatus.SUCCESS;
    }

    /**
     * Get a group constraint for SQL query depending on the group type.
     * 
     * @param groupFilterId the group id
     * @return a String containing the corresponding group constraint
     */
    private static String getGroupConstraint(Long groupFilterId) {
        if ((groupFilterId != null) && (groupFilterId == GroupsTable.GROUP_PHONEBOOK)) {
            return " WHERE " + ContactSummaryTable.Field.SYNCTOPHONE + "=" + "1";
        }
        if ((groupFilterId != null) && (groupFilterId == GroupsTable.GROUP_CONNECTED_FRIENDS)) {
            return " WHERE " + ContactSummaryTable.Field.FRIENDOFMINE + "=" + "1";
        }
        if ((groupFilterId != null) && (groupFilterId == GroupsTable.GROUP_ONLINE)) {
            String inClause = getOnlineWhereClause();
            return " WHERE " + ContactSummaryTable.Field.LOCALCONTACTID + " IN " + (inClause == null? "()": inClause);
            
        }
        return " INNER JOIN " + ContactGroupsTable.TABLE_NAME + " WHERE "
                + ContactSummaryTable.TABLE_NAME + "." + ContactSummaryTable.Field.LOCALCONTACTID
                + "=" + ContactGroupsTable.TABLE_NAME + "."
                + ContactGroupsTable.Field.LOCALCONTACTID + " AND "
                + ContactGroupsTable.Field.ZYBGROUPID + "=" + groupFilterId;
    }

    /**
     * Fetches a contact list cursor for a given filter and search constraint
     * 
     * @param groupFilterId The server group ID or null to fetch all groups
     * @param constraint A search string or null to fetch without constraint
     * @param meProfileId The current me profile Id which should be excluded
     *            from the returned list.
     * @param readableDb Readable SQLite database
     * @return The cursor or null if an error occurred
     * @see #getQueryData(Cursor)
     */
    public static Cursor openContactSummaryCursor(Long groupFilterId, CharSequence constraint,
            Long meProfileId, SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() "
                    + "groupFilterId[" + groupFilterId + "] constraint[" + constraint + "]"
                    + " meProfileId[" + meProfileId + "]");
        }

        try {
            if (meProfileId == null) {
                // Ensure that when the profile is not available the function
                // doesn't fail
                // Since "Field <> null" always returns false
                meProfileId = -1L;
            }
            if (groupFilterId == null) {
                if (constraint == null) {
                    // Fetch all contacts
                    return openContactSummaryCursor(groupFilterId, meProfileId, readableDb);
                } else {
                    return openContactSummaryCursor(constraint, meProfileId, readableDb);
                }
            } else {
                // filtering by group id
                if (constraint == null) {
                    return openContactSummaryCursor(groupFilterId, meProfileId, readableDb);
                } else {
                    // filter by both group and constraint
                    final String dbSafeConstraint = DatabaseUtils.sqlEscapeString("%" + constraint
                            + "%");
                    return readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
                            + " FROM " + ContactSummaryTable.TABLE_NAME
                            + getGroupConstraint(groupFilterId) + " AND "
                            + ContactSummaryTable.Field.DISPLAYNAME + " LIKE " + dbSafeConstraint
                            + " AND " + ContactSummaryTable.TABLE_NAME + "."
                            + ContactSummaryTable.Field.LOCALCONTACTID + "<>" + meProfileId
                            + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")",
                            null);
                }
            }
        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.fetchContactList() "
                    + "SQLException - Unable to fetch filtered summary cursor", e);
            return null;
        }
    }

    public static Cursor fetchContactList(Long groupFilterId, CharSequence constraint,
            Long meProfileId, SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() "
                    + "groupFilterId[" + groupFilterId + "] constraint[" + constraint + "]"
                    + " meProfileId[" + meProfileId + "]");
        }
        try {
            if (meProfileId == null) {
                // Ensure that when the profile is not available the function
                // doesn't fail
                // Since "Field <> null" always returns false
                meProfileId = -1L;
            }
            if (groupFilterId == null) {
                if (constraint == null) {
                    // Fetch all contacts
                    return fetchContactList(groupFilterId, meProfileId, readableDb);
                } else {
                    return fetchContactList(constraint, meProfileId, readableDb);
                }
            } else {
                // filtering by group id
                if (constraint == null) {
                    return fetchContactList(groupFilterId, meProfileId, readableDb);
                } else {
                    // filter by both group and constraint
                    final String dbSafeConstraint = DatabaseUtils.sqlEscapeString("%" + constraint
                            + "%");
                    return readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
                            + " FROM " + ContactSummaryTable.TABLE_NAME
                            + getGroupConstraint(groupFilterId) + " AND "
                            + ContactSummaryTable.Field.DISPLAYNAME + " LIKE " + dbSafeConstraint
                            + " AND " + ContactSummaryTable.TABLE_NAME + "."
                            + ContactSummaryTable.Field.LOCALCONTACTID + "<>" + meProfileId
                            + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")",
                            null);
                }
            }
        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.fetchContactList() "
                    + "SQLException - Unable to fetch filtered summary cursor", e);
            return null;
        }
    }
   

    private static Cursor fetchContactList(Long groupFilterId, Long meProfileId,
            SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() groupFilterId["
                    + groupFilterId + "] meProfileId[" + meProfileId + "]");
        }
        try {
            if (groupFilterId == null) {
                // Fetch all contacts
                return readableDb.rawQuery(getOrderedQueryStringSql(ContactSummaryTable.TABLE_NAME
                        + "." + ContactSummaryTable.Field.LOCALCONTACTID + "<>" + meProfileId),
                        null);
            }
            return readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
                    + " FROM " + ContactSummaryTable.TABLE_NAME + getGroupConstraint(groupFilterId)
                    + " AND " + ContactSummaryTable.TABLE_NAME + "."
                    + ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
                    + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")", null);

        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.fetchContactList() "
                    + "SQLException - Unable to fetch filtered summary cursor", e);
            return null;
        }
    }
    
    /**
     * Fetches the number of contacts in a group
     * LocalId, Displayname, onlinestatus, summaryid from contact summary table
     * Phone number from Contact details table
     * latest feed type, time stamp, description
     * 
     * @param groupFilterId The server group ID 
     * @param meProfileId The current me profile Id which should be excluded
     *            from the returned list.
     * @param readableDb Readable SQLite database
     * @return The number of contacts belonging to the group
     */
//    private static Cursor fetchContactList(Long groupFilterId, Long meProfileId,
//            SQLiteDatabase readableDb) {
//        if (Settings.ENABLED_DATABASE_TRACE) {
//            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() groupFilterId["
//                    + groupFilterId + "] meProfileId[" + meProfileId + "]");
//        }
//        try {
//            
//        	
//            String str1 = "SELECT " 
//            	
//            			 //selecting local contact id, display name, online status from 
//            			 //Contact summary table
//            			 + TABLE_NAME + "." + Field.LOCALCONTACTID + ", "
//            			 + Field.DISPLAYNAME + ", "
//            			 + Field.ONLINESTATUS  + ", " 
//            			 + Field.SUMMARYID + ", "
//            			 
//            			//selecting contact first phone number from contact details table
//            			 + ContactDetailsTable.TABLE_NAME + "." + ContactDetailsTable.Field.STRINGVAL+ ", "
//            			 
//            			//selecting latest feed status from activites table
//            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + ", "
//            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TIMESTAMP + ", "
////            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TITLE + ", "
//           			  	 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.DESCRIPTION
//            			 + " FROM " + ContactSummaryTable.TABLE_NAME 
//            			 
//            			 //Joining Contact Details Table to get the phone no
//            			 + " LEFT OUTER JOIN "
//            			 + ContactDetailsTable.TABLE_NAME 
//            			 + " ON "
//            			 + ContactSummaryTable.TABLE_NAME + "."
//                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
//                         + ContactDetailsTable.TABLE_NAME + "."
//                         + ContactDetailsTable.Field.LOCALCONTACTID
//                         + " AND "
//                         + ContactDetailsTable.TABLE_NAME + "."
//                         + ContactDetailsTable.Field.KEY + "=4" 
//                         + " AND "
//                         + ContactDetailsTable.Field.DETAILLOCALID+"="
//                         + "(SELECT MIN("+ContactDetailsTable.Field.DETAILLOCALID+") FROM "
//                         + ContactDetailsTable.TABLE_NAME
//                         + " WHERE "
//                         + ContactSummaryTable.TABLE_NAME + "."
//                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
//                         + ContactDetailsTable.TABLE_NAME + "."
//                         + ContactDetailsTable.Field.LOCALCONTACTID
//                         + " AND "
//                         + ContactDetailsTable.TABLE_NAME + "."
//                         + ContactDetailsTable.Field.KEY + "=4)"
//                         
//                         //Joining Activities Table to get the latest status feed
//                         + " LEFT OUTER JOIN "
//                         + ActivitiesTable.TABLE_NAME 
//            			 + " ON "
//            			 + ContactSummaryTable.TABLE_NAME + "."
//                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
//                         + ActivitiesTable.TABLE_NAME + "."
//                         + ActivitiesTable.Field.LOCAL_CONTACT_ID
//                         + " AND "
//                         + ActivitiesTable.Field.LOCAL_ACTIVITY_ID+"="
//                         + "(SELECT MAX("+ActivitiesTable.Field.LOCAL_ACTIVITY_ID+") FROM "
//                         + ActivitiesTable.TABLE_NAME
//                         + " WHERE "
//                         + ContactSummaryTable.TABLE_NAME + "."
//                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
//                         + ActivitiesTable.TABLE_NAME + "."
//                         + ActivitiesTable.Field.LOCAL_CONTACT_ID
//                         + " AND "
//                         + " ( "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_dialed'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_missed'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_received'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_email_received'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_email_sent'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_im_conversation'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_mms_received'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_mms_sent'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_sms_received'" + " OR "
//                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_sms_sent'"
//                         + "))"
//                         
//                         + (groupFilterId == null ?" WHERE ":getGroupConstraint(groupFilterId)+ " AND ") 
//            			 
//            			 // where condition for the contact summary table
//            			 + ContactSummaryTable.TABLE_NAME + "."
//                         + ContactSummaryTable.Field.LOCALCONTACTID + "<>" + meProfileId
//                         + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")"
//                         ;
//            
//            
//            return readableDb.rawQuery(str1, null);           
//
//        } catch (SQLException e) {
//            LogUtils.logE("ContactSummeryTable.fetchContactList() "
//                    + "SQLException - Unable to fetch filtered summary cursor", e);
//            return null;
//        }
//    }
    
    /**
     * Fetches the number of contacts in a group
     * LocalId, Displayname, onlinestatus, summaryid from contact summary table
     * Phone number from Contact details table
     * latest feed type, time stamp, description
     * 
     * @param groupFilterId The server group ID 
     * @param meProfileId The current me profile Id which should be excluded
     *            from the returned list.
     * @param readableDb Readable SQLite database
     * @return The number of contacts belonging to the group
     */
    /*private static Cursor fetchContactList(Long groupFilterId, Long meProfileId,String constraint,
            SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() groupFilterId["
                    + groupFilterId + "] meProfileId[" + meProfileId + "]");
        }
        try {
            
            String str1 = "SELECT " 
            	
            			 //selecting local contact id, display name, online status from 
            			 //Contact summary table
            			 + TABLE_NAME + "." + Field.LOCALCONTACTID + ", "
            			 + Field.DISPLAYNAME + ", "
            			 + Field.ONLINESTATUS  + ", " 
            			 + Field.SUMMARYID + ", "
            			 
            			//selecting contact first phone number from contact details table
            			 + ContactDetailsTable.TABLE_NAME + "." + ContactDetailsTable.Field.STRINGVAL+ ", "
            			 
            			//selecting latest feed status from activites table
            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + ", "
            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TIMESTAMP + ", "
//            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TITLE + ", "
           			  	 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.DESCRIPTION
            			 + " FROM " + ContactSummaryTable.TABLE_NAME 
            			 
            			 //Joining Contact Details Table to get the phone no
            			 + " LEFT OUTER JOIN "
            			 + ContactDetailsTable.TABLE_NAME 
            			 + " ON "
            			 + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
                         + ContactDetailsTable.TABLE_NAME + "."
                         + ContactDetailsTable.Field.LOCALCONTACTID
                         + " AND "
                         + ContactDetailsTable.TABLE_NAME + "."
                         + ContactDetailsTable.Field.KEY + "=4" 
                         + " AND "
                         + ContactDetailsTable.Field.DETAILLOCALID+"="
                         + "(SELECT MIN("+ContactDetailsTable.Field.DETAILLOCALID+") FROM "
                         + ContactDetailsTable.TABLE_NAME
                         + " WHERE "
                         + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
                         + ContactDetailsTable.TABLE_NAME + "."
                         + ContactDetailsTable.Field.LOCALCONTACTID
                         + " AND "
                         + ContactDetailsTable.TABLE_NAME + "."
                         + ContactDetailsTable.Field.KEY + "=4)"
                         
                         //Joining Activities Table to get the latest status feed
                         + " LEFT OUTER JOIN "
                         + ActivitiesTable.TABLE_NAME 
            			 + " ON "
            			 + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
                         + ActivitiesTable.TABLE_NAME + "."
                         + ActivitiesTable.Field.LOCAL_CONTACT_ID
                         + " AND "
                         + ActivitiesTable.Field.LOCAL_ACTIVITY_ID+"="
                         + "(SELECT MAX("+ActivitiesTable.Field.LOCAL_ACTIVITY_ID+") FROM "
                         + ActivitiesTable.TABLE_NAME
                         + " WHERE "
                         + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
                         + ActivitiesTable.TABLE_NAME + "."
                         + ActivitiesTable.Field.LOCAL_CONTACT_ID
                         + " AND "
                         + " ( "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_dialed'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_missed'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_received'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_email_received'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_email_sent'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_im_conversation'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_mms_received'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_mms_sent'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_sms_received'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_sms_sent'"
                         + "))"
                         
                         + (groupFilterId == null ?" WHERE ":getGroupConstraint(groupFilterId)+ " AND ") 
            			 
            			 // where condition for the contact summary table
            			 + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "<>" + meProfileId
                         + getConstraint(constraint)
                         + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")"
                         ;
            
            return readableDb.rawQuery(str1, null);           

        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.fetchContactList() "
                    + "SQLException - Unable to fetch filtered summary cursor", e);
            return null;
        }
    }*/
    
    /**
     * Gets the constraint to be appended to the sql query
     * @param constraint The constraint that needs to be passed to the sql
     * @return the string to be appended to sql query for a constraint
     */
    
    private static String getConstraint(String constraint){
    	if(null == constraint){
    		return " ";
    	}
    	String search = null;
    	search = " AND "+ContactSummaryTable.Field.DISPLAYNAME + " LIKE " + DatabaseUtils.sqlEscapeString("%" + constraint + "%");
    	return search;
    	
    }
    
        
    /**
     * Fetches the number of contacts in a group
     * 
     * @param groupFilterId The server group ID 
     * @param meProfileId The current me profile Id which should be excluded
     *            from the returned list.
     * @param readableDb Readable SQLite database
     * @return The number of contacts belonging to the group
     */
    
    public static int getGroupContactListCount(Long groupFilterId, Long meProfileId, SQLiteDatabase readableDb)
    {
    	int groupCount = 0;
    	if (groupFilterId != null) {
    		Cursor c1 = null;
    		try{
    		c1 = readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
                + " FROM " + ContactSummaryTable.TABLE_NAME + getGroupConstraint(groupFilterId)
                + " AND " + ContactSummaryTable.TABLE_NAME + "."
                + ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
                + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")", null);
    		if(c1 != null)
    			groupCount = c1.getCount();
    		}
    		finally {
                CloseUtils.close(c1);
                c1 = null;
            }
    	}    	
    	return groupCount;
    }
    
    private static Cursor fetchContactList(CharSequence constraint, Long meProfileId,
            SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() constraint["
                    + constraint + "] meProfileId[" + meProfileId + "]");
        }
        try {
            if (constraint == null) {
                // Fetch all contacts
                return readableDb.rawQuery(getOrderedQueryStringSql(), null);
            }
            final String dbSafeConstraint = DatabaseUtils.sqlEscapeString("%" + constraint + "%");
            return readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
                    + " FROM " + ContactSummaryTable.TABLE_NAME + " WHERE "
                    + ContactSummaryTable.Field.DISPLAYNAME + " LIKE " + dbSafeConstraint + " AND "
                    + ContactSummaryTable.TABLE_NAME + "."
                    + ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
                    + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")", null);
        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.fetchContactList() "
                    + "SQLException - Unable to fetch filtered summary cursor", e);
            return null;
        }
    }
    
    //New method
    
    /*private static Cursor fetchContactList(CharSequence constraint,Long groupFilterId, Long meProfileId,
            SQLiteDatabase readableDb) {
    	
    		try {
            
            String str1 = "SELECT " 
            	
            			 //selecting local contact id, display name, online status from 
            			 //Contact summary table
            			 + TABLE_NAME + "." + Field.LOCALCONTACTID + ", "
            			 + Field.DISPLAYNAME + ", "
            			 + Field.ONLINESTATUS  + ", " 
            			 + Field.SUMMARYID + ", "
            			 
            			//selecting contact first phone number from contact details table
            			 + ContactDetailsTable.TABLE_NAME + "." + ContactDetailsTable.Field.STRINGVAL+ ", "
            			 
            			//selecting latest feed status from activites table
            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + ", "
            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TIMESTAMP + ", "
//            			 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TITLE + ", "
           			  	 + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.DESCRIPTION
            			 + " FROM " + ContactSummaryTable.TABLE_NAME 
            			 
            			 //Joining Contact Details Table to get the phone no
            			 + " LEFT OUTER JOIN "
            			 + ContactDetailsTable.TABLE_NAME 
            			 + " ON "
            			 + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
                         + ContactDetailsTable.TABLE_NAME + "."
                         + ContactDetailsTable.Field.LOCALCONTACTID
                         + " AND "
                         + ContactDetailsTable.TABLE_NAME + "."
                         + ContactDetailsTable.Field.KEY + "=4" 
                         + " AND "
                         + ContactDetailsTable.Field.DETAILLOCALID+"="
                         + "(SELECT MIN("+ContactDetailsTable.Field.DETAILLOCALID+") FROM "
                         + ContactDetailsTable.TABLE_NAME
                         + " WHERE "
                         + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
                         + ContactDetailsTable.TABLE_NAME + "."
                         + ContactDetailsTable.Field.LOCALCONTACTID
                         + " AND "
                         + ContactDetailsTable.TABLE_NAME + "."
                         + ContactDetailsTable.Field.KEY + "=4)"
                         
                         //Joining Activities Table to get the latest status feed
                         + " LEFT OUTER JOIN "
                         + ActivitiesTable.TABLE_NAME 
            			 + " ON "
            			 + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
                         + ActivitiesTable.TABLE_NAME + "."
                         + ActivitiesTable.Field.LOCAL_CONTACT_ID
                         + " AND "
                         + ActivitiesTable.Field.LOCAL_ACTIVITY_ID+"="
                         + "(SELECT MAX("+ActivitiesTable.Field.LOCAL_ACTIVITY_ID+") FROM "
                         + ActivitiesTable.TABLE_NAME
                         + " WHERE "
                         + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "=" 
                         + ActivitiesTable.TABLE_NAME + "."
                         + ActivitiesTable.Field.LOCAL_CONTACT_ID
                         + " AND "
                         + " ( "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_dialed'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_missed'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='call_received'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_email_received'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_email_sent'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_im_conversation'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_mms_received'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_mms_sent'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_sms_received'" + " OR "
                         + ActivitiesTable.TABLE_NAME + "." + ActivitiesTable.Field.TYPE + "='message_sms_sent'"
                         + "))"
                         
                         + (groupFilterId == null ?" WHERE ":getGroupConstraint(groupFilterId)+ " AND ") 
            			 
            			 // where condition for the contact summary table
            			 + ContactSummaryTable.TABLE_NAME + "."
                         + ContactSummaryTable.Field.LOCALCONTACTID + "<>" + meProfileId
                         + getConstraint(constraint.toString())
                         + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")"
                         ;
            
            return readableDb.rawQuery(str1, null);           

        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.fetchContactList() "
                    + "SQLException - Unable to fetch filtered summary cursor", e);
            return null;
        }
    	//return null;
    }*/
    /**
     * Fetches a contact list cursor for a given filter
     * 
     * @param groupFilterId The server group ID or null to fetch all groups
     * @param meProfileId The current me profile Id which should be excluded
     *            from the returned list.
     * @param readableDb Readable SQLite database
     * @return The cursor or null if an error occurred
     * @see #getQueryData(Cursor)
     */
    private static Cursor openContactSummaryCursor(Long groupFilterId, Long meProfileId,
            SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() groupFilterId["
                    + groupFilterId + "] meProfileId[" + meProfileId + "]");
        }
        try {
            if (groupFilterId == null) {
                // Fetch all contacts
                return readableDb.rawQuery(getOrderedQueryStringSql(ContactSummaryTable.TABLE_NAME
                        + "." + ContactSummaryTable.Field.LOCALCONTACTID + "<>" + meProfileId),
                        null);
            }
            return readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
                    + " FROM " + ContactSummaryTable.TABLE_NAME + getGroupConstraint(groupFilterId)
                    + " AND " + ContactSummaryTable.TABLE_NAME + "."
                    + ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
                    + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")", null);

        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.fetchContactList() "
                    + "SQLException - Unable to fetch filtered summary cursor", e);
            return null;
        }
    }

    /**
     * Fetches a contact list cursor for a given search constraint
     * 
     * @param constraint A search string or null to fetch without constraint
     * @param meProfileId The current me profile Id which should be excluded
     *            from the returned list.
     * @param readableDb Readable SQLite database
     * @return The cursor or null if an error occurred
     * @see #getQueryData(Cursor)
     */
    private static Cursor openContactSummaryCursor(CharSequence constraint, Long meProfileId,
            SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() constraint["
                    + constraint + "] meProfileId[" + meProfileId + "]");
        }
        try {
            if (constraint == null) {
                // Fetch all contacts
                return readableDb.rawQuery(getOrderedQueryStringSql(), null);
            }
            final String dbSafeConstraint = DatabaseUtils.sqlEscapeString("%" + constraint + "%");
            return readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
                    + " FROM " + ContactSummaryTable.TABLE_NAME + " WHERE "
                    + ContactSummaryTable.Field.DISPLAYNAME + " LIKE " + dbSafeConstraint + " AND "
                    + ContactSummaryTable.TABLE_NAME + "."
                    + ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
                    + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")", null);
        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.fetchContactList() "
                    + "SQLException - Unable to fetch filtered summary cursor", e);
            return null;
        }
    }

    /**
     * Fetches the current alternative field type for a contact This value
     * determines how the alternative detail is currently being used for the
     * record.
     * 
     * @param localContactID The primary key ID of the contact
     * @param readableDb Readable SQLite database
     * @return The alternative field type or null if a database error occurred
     */
    /*
     * private static AltFieldType fetchAltFieldType(long localContactId,
     * SQLiteDatabase readableDb) { if (Settings.ENABLED_DATABASE_TRACE) {
     * DatabaseHelper.trace(false,
     * "ContactSummeryTable.FetchAltFieldType() localContactId[" +
     * localContactId + "]"); } Cursor c = null; try { c =
     * readableDb.rawQuery("SELECT " + Field.ALTFIELDTYPE + " FROM " +
     * ContactSummaryTable.TABLE_NAME + " WHERE " + Field.LOCALCONTACTID + "=" +
     * localContactId, null); AltFieldType type = AltFieldType.UNUSED; if
     * (c.moveToFirst() && !c.isNull(0)) { int val = c.getInt(0); if (val <
     * AltFieldType.values().length) { type = AltFieldType.values()[val]; } }
     * return type; } catch (SQLException e) {
     * LogUtils.logE("ContactSummeryTable.fetchContactList() " +
     * "SQLException - Unable to fetch alt field type", e); return null; }
     * finally { CloseUtils.close(c); c = null; } }
     */

    /**
     * Fetches an SQLite statement object which can be used to merge the native
     * information from one contact to another.
     * 
     * @param writableDb Writable SQLite database
     * @return The SQL statement, or null if a compile error occurred
     * @see #mergeContact(ContactIdInfo, SQLiteStatement)
     */
     public static SQLiteStatement mergeContactStatement(SQLiteDatabase
             writableDb) { 
         if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(true, "ContactSummeryTable.mergeContact()"); 
         } try {
            return writableDb.compileStatement(UPDATE_NATIVE_ID_BY_LOCAL_CONTACT_ID); 
         } catch (SQLException e) {
            LogUtils.logE("ContactSummaryTable.mergeContactStatement() compile error:\n", e); 
            return null; 
        } 
     }
    
    /**
     * Copies the contact native information from one contact to another
     * 
     * @param info Copies the {@link ContactIdInfo#nativeId} value to the
     *            contact with local ID {@link ContactIdInfo#mergedLocalId}.
     * @param statement The statement returned by
     *            {@link #mergeContactStatement(SQLiteDatabase)}.
     * @return SUCCESS or a suitable error code
     * @see #mergeContactStatement(SQLiteDatabase)
     */
    public static ServiceStatus mergeContact(ContactIdInfo info, SQLiteStatement statement) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(true, "ContactSummeryTable.mergeContact()");
        }
        if (statement == null) {
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        }
        try {
            statement.bindLong(1, info.nativeId);
            statement.bindLong(2, info.mergedLocalId);
            statement.execute();
            return ServiceStatus.SUCCESS;
        } catch (SQLException e) {
            LogUtils.logE("ContactSummeryTable.mergeContact() "
                    + "SQLException - Unable to merge contact summary native info:\n", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        }
    }

    /**
     * TODO: be careful
     * 
     * @param user
     * @param writableDb
     * @return
     */
    public synchronized static ServiceStatus updateOnlineStatus(User user) {
         sPresenceMap.put(user.getLocalContactId(), user.isOnline());
         return ServiceStatus.SUCCESS;
    }
    
    /**
     * This method sets users offline except for provided local contact ids.
     * @param userIds - ArrayList of integer user ids, if null - all user will be removed from the presence hash.
     * @param writableDb - database.
     */
    public synchronized static void setUsersOffline(ArrayList<Long> userIds) {
        Iterator<Long> itr = sPresenceMap.keySet().iterator();
        Long localId = null;
        while(itr.hasNext()) {
            localId = itr.next();
            if (userIds == null || !userIds.contains(localId)) {
                itr.remove();
            }
        }
    }

    /**
     * @param user
     * @param writableDb
     * @return
     */
    public synchronized static ServiceStatus setOfflineStatus() {
        // If any contact is not present within the presenceMap, then its status
        // is considered as OFFLINE. This is taken care in the getPresence API.
        if (sPresenceMap != null) {
            sPresenceMap.clear();
        }
        return ServiceStatus.SUCCESS;

    }

    /**
     * @param localContactIdOfMe
     * @param writableDb
     * @return
     */
    public synchronized static ServiceStatus setOfflineStatusExceptForMe(long localContactIdOfMe) {
        // If any contact is not present within the presenceMap, then its status
        // is considered as OFFLINE. This is taken care in the getPresence API.
        if (sPresenceMap != null) {
            sPresenceMap.clear();
            sPresenceMap.put(localContactIdOfMe, OnlineStatus.OFFLINE.ordinal());
        }
        return ServiceStatus.SUCCESS;

    }

    /**
     * Updates the native IDs for a list of contacts.
     * 
     * @param contactIdList A list of ContactIdInfo objects. For each object,
     *            the local ID must match a local contact ID in the table. The
     *            Native ID will be used for the update. Other fields are
     *            unused.
     * @param writeableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus syncSetNativeIds(List<ContactIdInfo> contactIdList,
            SQLiteDatabase writableDb) {
        DatabaseHelper.trace(true, "ContactSummaryTable.syncSetNativeIds()");
        if (contactIdList.size() == 0) {
            return ServiceStatus.SUCCESS;
        }

        final SQLiteStatement statement1 = writableDb.compileStatement("UPDATE " + TABLE_NAME
                + " SET " + Field.NATIVEID + "=? WHERE " + Field.LOCALCONTACTID + "=?");
        
        
        for (int i = 0; i < contactIdList.size(); i++) {
            
            final ContactIdInfo info = contactIdList.get(i);
            
            try {
                writableDb.beginTransaction();
                
                if (info.nativeId == null) {
                    statement1.bindNull(1);
                } else {
                    statement1.bindLong(1, info.nativeId);
                }
                statement1.bindLong(2, info.localId);

                statement1.execute();

                writableDb.setTransactionSuccessful();
            } catch (SQLException e) {
                
                LogUtils.logE("ContactSummaryTable.syncSetNativeIds() "
                        + "SQLException - Unable to update contact native Ids", e);
                return ServiceStatus.ERROR_DATABASE_CORRUPT;
            } finally {
                
                writableDb.endTransaction();
            }
        }

        return ServiceStatus.SUCCESS;
    }

    private static boolean isEmpty(String string) {
        if (string == null)
            return true;
        if (string.trim().length() == 0)
            return true;
        return false;
    }

    /**
     * Updates the summary for a contact Replaces the complex logic of updating
     * the summary with a new contactdetail. Instead the method gets a whole
     * contact after it has been modified and builds the summary infos.
     * 
     * @param contact A Contact object that has been modified
     * @param writeableDb Writable SQLite database
     * @return SUCCESS or a suitable error code
     */
    public static ServiceStatus updateContactDisplayName(Contact contact, SQLiteDatabase writableDb) {
        // These two Arrays contains the order in which the details are queried.
        // First valid (not empty or unknown) detail is taken
        ContactDetail.DetailKeys prefferredNameDetails[] = {
                ContactDetail.DetailKeys.VCARD_NAME, ContactDetail.DetailKeys.VCARD_ORG,
                ContactDetail.DetailKeys.VCARD_EMAIL, ContactDetail.DetailKeys.VCARD_PHONE
        };
        ContactDetail name = null;

        // Query the details for the name field
        for (ContactDetail.DetailKeys key : prefferredNameDetails) {
            if ((name = contact.getContactDetail(key)) != null) {
                // Some contacts have only email but the name detail!=null
                // (gmail for example)
                if (key == ContactDetail.DetailKeys.VCARD_NAME && name.getName() == null){
                	continue;
                }
                if (key != ContactDetail.DetailKeys.VCARD_NAME && isEmpty(name.getValue())){
                	continue;
                }
            break;
            }
        }

        // Build the name
        String nameString = name != null ? name.getValue() : null;
        if (nameString == null)
            nameString = ContactDetail.UNKNOWN_NAME;
        if (name != null && name.key == ContactDetail.DetailKeys.VCARD_NAME)
            nameString = name.getName().toString();
        
        

        // Start updating the table

        SQLiteStatement statement = null;
        try {
            final StringBuffer updateQuery = StringBufferPool.getStringBuffer(SQLKeys.UPDATE);
            updateQuery.append(TABLE_NAME).append(SQLKeys.SET).append(Field.DISPLAYNAME).
            append("=? WHERE ").append(Field.LOCALCONTACTID).append("=?");
            statement = writableDb.compileStatement(StringBufferPool.toStringThenRelease(updateQuery));
            
            writableDb.beginTransaction();
            statement.bindString(1, nameString);
            statement.execute();
            writableDb.setTransactionSuccessful();
            
        } catch (SQLException e) {
            LogUtils.logE("ContactSummaryTable.updateNameAndStatus() "
                    + "SQLException - Unable to update contact native Ids", e);
            return ServiceStatus.ERROR_DATABASE_CORRUPT;
        } finally {
            writableDb.endTransaction();
            if (statement != null) {
                statement.close();
                statement = null;
            }
        }

        return ServiceStatus.SUCCESS;
    }
    
    /**
     * LocalId = ?
     */
    private final static String SQL_STRING_LOCAL_ID_EQUAL_QUESTION_MARK = Field.LOCALCONTACTID + " = ?";
    
    /**
     * 
     * @param localContactId
     * @param writableDb
     * @return
     */
    public static boolean setNativeContactId(long localContactId, long nativeContactId, SQLiteDatabase writableDb) {
        
        final ContentValues values = new ContentValues();
        
        values.put(Field.NATIVEID.toString(), nativeContactId);
        
        try {
            
            if (writableDb.update(TABLE_NAME, values, SQL_STRING_LOCAL_ID_EQUAL_QUESTION_MARK, new String[] { Long.toString(localContactId) }) == 1) {
                
                return true;
            }
            
        } catch (Exception e) {
            
            LogUtils.logE("ContactsTable.setNativeContactId() Exception - " + e);
        }
        
        return false;
    }

    /**
     * Clears the Presence Map table. This needs to be called whenever the ContactSummaryTable is cleared 
     * or recreated.
     */
    private synchronized static void clearPresenceMap() {
        sPresenceMap.clear();
        
    }
    
    /**
     * Fetches the presence of the contact with localContactID
     * 
     * @param localContactID
     * @return the presence status of the contact
     */
    public synchronized static OnlineStatus getPresence(Long localContactID) {
        OnlineStatus onlineStatus = OnlineStatus.OFFLINE;
        Integer val = sPresenceMap.get(localContactID);
        if (val != null) {
            if (val < ContactSummary.OnlineStatus.values().length) {
                onlineStatus = ContactSummary.OnlineStatus.values()[val];
            }
        }

        return onlineStatus;
    }
    
    /**
     * This API should be called whenever a contact is added. The presenceMap should be consistent
     * with the ContactSummaryTable. Hence the default status of OFFLINE is set for every contact added
     * @param localContactID
     */
    private synchronized static void addToPresenceMap(Long localContactID) {
        sPresenceMap.put(localContactID, OnlineStatus.OFFLINE.ordinal());
        
    }
    
    /**
     * This API should be called whenever a contact is deleted from teh ContactSUmmaryTable. This API 
     * removes the presence information for the given contact
     * @param localContactId
     */
    private synchronized static void deleteFromPresenceMap(Long localContactId) {
        sPresenceMap.remove(localContactId);
        
    }
    
    /**
     * This API creates the string to be used in the IN clause when getting the list of all
     * online contacts.
     * @return The list of contacts in the proper format for the IN list
     */
    private synchronized static String getOnlineWhereClause() {
        Set<Entry<Long, Integer>> set = sPresenceMap.entrySet();
        Iterator<Entry<Long, Integer>> i = set.iterator();
        String inClause = "(";
        boolean isFirst = true;

        while (i.hasNext()) {
            Entry<Long, Integer> me = (Entry<Long, Integer>) i.next();
            Integer value = me.getValue();
            if (value != null
                    && (value == OnlineStatus.ONLINE.ordinal() || value == OnlineStatus.IDLE
                            .ordinal())) {
                if (isFirst == false) {
                    inClause = inClause.concat(",");
                } else {
                    isFirst = false;
                }
                inClause = inClause.concat(String.valueOf(me.getKey()));
            }
        }
        if (isFirst == true) {
            inClause = null;
        } else {
            inClause = inClause.concat(")");
        }
        return inClause;

    }

    /**
     * Fetches the formattedName for the corresponding localContactId.
     *
     * @param localContactId The primary key ID of the contact to find
     * @param readableDb Readable SQLite database
     * @return String formattedName or NULL on error
     */
    public static String fetchFormattedNamefromLocalContactId(
            final long localContactId, final SQLiteDatabase readableDb) {
        if (Settings.ENABLED_DATABASE_TRACE) {
            DatabaseHelper.trace(false,
                    "ContactSummaryTable.fetchFormattedNamefromLocalContactId"
                    + " localContactId[" + localContactId + "]");
        }
        Cursor c1 = null;
        String formattedName = null;
        try {
            String query = "SELECT " + Field.DISPLAYNAME + " FROM " + TABLE_NAME
                + " WHERE " + Field.LOCALCONTACTID + "=" + localContactId;

            c1 = readableDb.rawQuery(query, null);
            if (c1 != null && c1.getCount() > 0) {
                c1.moveToFirst();
                formattedName = c1.getString(0);
            }

            return formattedName;
        } catch (SQLiteException e) {
            LogUtils
                    .logE(
                            "fetchFormattedNamefromLocalContactId() "
                            + "Exception - Unable to fetch contact summary",
                            e);
            return formattedName;
        } finally {
            CloseUtils.close(c1);
            c1 = null;
        }
    }

    
    /**
     * Fetches a chat contact list cursor for a given filter and search constraint
     * 
     * @param constraint A search string or null to fetch without constraint
     * @param meProfileId The current me profile Id which should be excluded
     *            from the returned list.
     * @param readableDb Readable SQLite database
     * @param sns The chatable identities
     * @return The cursor or null if an error occurred
     * @see #getQueryData(Cursor)
     */
    public static Cursor openChatContactSummaryCursor(CharSequence constraint,
            Long meProfileId,String sns, SQLiteDatabase readableDb,SQLiteDatabase writeableDb, int iOperationReq) 
    {
    	if (Settings.ENABLED_DATABASE_TRACE) 
    	{
    		DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() "
    				+ " constraint[" + constraint + "]"
    				+ " meProfileId[" + meProfileId + "]");
    	}
    	try 
    	{
    		if(iOperationReq == 0)
    		{
    			if (meProfileId == null) 
    			{
    				// Ensure that when the profile is not available the function
    				// doesn't fail Since "Field <> null" always returns false
    				meProfileId = -1L;
    			}
    			return openChatContactSummaryCursor( meProfileId,sns ,readableDb);
    		}
    		else 
    		{
    			final String  dbSafeConstraint = DatabaseUtils.sqlEscapeString("%" + constraint + "%");
    			StringBuilder sBuilder = new StringBuilder();
    			sBuilder.append("SELECT ");
    			sBuilder.append(ContactSummaryTable.getFullQueryList());
    			sBuilder.append(" FROM ");
    			sBuilder.append(ContactSummaryTable.TABLE_NAME);
    			sBuilder.append(" WHERE ");
    			sBuilder.append(ContactSummaryTable.Field.DISPLAYNAME);
    			sBuilder.append(" LIKE ");
    			sBuilder.append(dbSafeConstraint);
    			sBuilder.append(" AND ");
    			sBuilder.append(ContactSummaryTable.TABLE_NAME);
    			sBuilder.append(".");
    			sBuilder.append(ContactSummaryTable.Field.LOCALCONTACTID);
    			sBuilder.append("!=");
    			sBuilder.append(meProfileId);
    			sBuilder.append(" AND ");
    			sBuilder.append(ContactSummaryTable.Field.SNS);
    			sBuilder.append(" IN ");
    			sBuilder.append(" ( ");
    			sBuilder.append(sns);
    			sBuilder.append(" ) ");
    			sBuilder.append(" ORDER BY ");
    			sBuilder.append("LOWER(");
    			sBuilder.append(ContactSummaryTable.Field.DISPLAYNAME);
    			sBuilder.append(") ");
    			return readableDb.rawQuery(sBuilder.toString(), null);
    		}
    	} catch (SQLException e) {
    		LogUtils.logE("ContactSummeryTable.fetchContactList() "
    				+ "SQLException - Unable to fetch filtered summary cursor", e);
    		return null;
    	}
    }

    	/**
    	 * Fetches a chat contact list cursor for a given filter
    	 * 
    	 * @param meProfileId The current me profile Id which should be excluded
    	 *            from the returned list.
    	 * @param readableDb Readable SQLite database
    	 * @return The cursor or null if an error occurred
    	 * @see #getQueryData(Cursor)
    	 */
    	private static Cursor openChatContactSummaryCursor(Long meProfileId,String sns,
    			SQLiteDatabase readableDb) {
    		if (Settings.ENABLED_DATABASE_TRACE) {
    			DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactList() meProfileId[" + meProfileId + "]");
    		}
    		try {
    				if(sPresenceMap.size() > 1){
    				Cursor onlineCursor = null , offlineCursor = null;//, idleCursor = null;
    				String online = getOnlineWhereClause();
//    				    getOnlineWhereClause(OnlineStatus.ONLINE, meProfileId)  ;
    				if(online != null)
    					onlineCursor =   readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
    							+ " FROM " + ContactSummaryTable.TABLE_NAME + " WHERE " 
    							+ ContactSummaryTable.Field.LOCALCONTACTID + " IN " + online
    							+ " AND " + ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
    							+ " AND " + ContactSummaryTable.Field.SNS + " IN " + "("
    							+  sns + " )" + " ORDER BY " +
    							"LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ") ", null);
    				String offline = getOnlineWhereClause()  ;
    				if(offline != null){
    					offlineCursor =    readableDb.rawQuery("SELECT  " + ContactSummaryTable.getFullQueryList()
    							+ " FROM " + ContactSummaryTable.TABLE_NAME + " WHERE " 
    							+ ContactSummaryTable.Field.LOCALCONTACTID + " NOT IN " +  offline
    							+ " AND " + ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
    							+ " AND " + ContactSummaryTable.Field.SNS + " IN " + "("
    							+  sns + " )" + " ORDER BY " +
    							"LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ") ", null);
    				}
//    				String idle = getOnlineWhereClause(OnlineStatus.IDLE, meProfileId);  
//    				if(idle != null)
//    					idleCursor =    readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
//    							+ " FROM " + ContactSummaryTable.TABLE_NAME + " WHERE " 
//    							+ ContactSummaryTable.Field.LOCALCONTACTID + " IN " +  idle
//    							+" AND " + ContactSummaryTable.Field.SNS + " IN " + "("
//    							+  sns + " )" + " ORDER BY " +
//    							"LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ") ", null);
    				
    				Cursor cursor[] = {onlineCursor /*,idleCursor*/ ,offlineCursor};
    				MergeCursor mergeCursor = new MergeCursor(cursor);
    				return mergeCursor;
    			} else
    			{
    				return readableDb.rawQuery("SELECT  " + ContactSummaryTable.getFullQueryList()
    						+ " FROM " + ContactSummaryTable.TABLE_NAME + " WHERE " 
    						+ ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId + " AND " + ContactSummaryTable.Field.SNS + " IN " + "("
    						+  sns + " )" + " ORDER BY (" + ContactSummaryTable.Field.ONLINESTATUS + ") DESC ," +
    						"LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ") ", null);

    			}

    		} catch (SQLException e) {
    			LogUtils.logE("ContactSummeryTable.fetchContactList() "
    					+ "SQLException - Unable to fetch filtered summary cursor", e);
    			return null;
    		}
    	}
   
    	/**
    	 * This API creates the string to be used in the IN clause when getting the list of all
    	 * online contacts.
    	 * @return The list of contacts in the proper format for the IN list
    	 */
    	private synchronized static String getOnlineWhereClause(OnlineStatus status, Long meProfileID) {

    		Set<Entry<Long, Integer>> set = sPresenceMap.entrySet();
    		Iterator<Entry<Long, Integer>> i = set.iterator();
    		String inClause = "(";
    		boolean isFirst = true;

    		while (i.hasNext()) {
    			Entry<Long, Integer> me = (Entry<Long, Integer>) i.next();
    			Long id = me.getKey();
    			if(id == meProfileID) {
    				continue;
    			} else
    			{
    				Integer value = me.getValue();
    				if (value != null && (value == status.ordinal())) {
    					if (isFirst == false) {
    						inClause = inClause.concat(",");
    					} else {
    						isFirst = false;
    					}
    					inClause = inClause.concat(String.valueOf(me.getKey()));
    				}
    			}
    		}
    		if (isFirst == true) {
    			inClause = null;
    		} else {
    			inClause = inClause.concat(")");
    		}
    		return inClause;
    	}   

    	public static int getPresenceMapCount()
    	{
    		return sPresenceMap.size();
    	}
    	
    	/**
    	 * This method returns the cursor of recent contacts 
    	 * @param groupFilterId the group id to be filtered (For extention in case required. At present not used)
    	 * @param meProfileId the user's profile id
    	 * @param readableDb Readable database
    	 * @return A cursor of recent contacts or null
    	 */
    	
    	public static Cursor fetchRecentContactList(Long groupFilterId, Long meProfileId,SQLiteDatabase readableDb) {
    		LogUtils.logD("ContactSummeryTable.fetchRecentContactList()");
    		if (Settings.ENABLED_DATABASE_TRACE) {
    			DatabaseHelper.trace(false, "ContactSummeryTable.fetchRecentContactList() " + "]");
    		}
    		if(meProfileId == null)
    			meProfileId = -1L;
    		try {
    			
    			if(groupFilterId != null){
    				return readableDb.rawQuery("SELECT " +
        					"DISTINCT a."+Field.LOCALCONTACTID +
        					", a."+ Field.DISPLAYNAME+
        					", a."+ Field.ONLINESTATUS+
        					", a."+ Field.SUMMARYID+
        					", a."+ Field.ALTDETAILTYPE+
        					", b."+ ActivitiesTable.Field.TYPE+
        					", b."+ ActivitiesTable.Field.TIMESTAMP+
        					", b."+ ActivitiesTable.Field.DESCRIPTION+ " FROM " +
        					ContactSummaryTable.TABLE_NAME +" a, "+ ActivitiesTable.TABLE_NAME + " b "+getGroupConstraint(groupFilterId)+" AND a."+Field.LOCALCONTACTID+"=b."+
        					ActivitiesTable.Field.LOCAL_CONTACT_ID+" AND "
                            + "a."+ ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId+" ORDER BY b."+ActivitiesTable.Field.TIMESTAMP+" DESC", null);
    			
    			}else{
    				return readableDb.rawQuery("SELECT " +
    						ContactSummaryTable.TABLE_NAME+"."+Field.LOCALCONTACTID
    						+","+ContactSummaryTable.TABLE_NAME+"."+ Field.DISPLAYNAME
    						+","+ContactSummaryTable.TABLE_NAME+"."+ Field.ONLINESTATUS
    						+","+ContactSummaryTable.TABLE_NAME+"."+ Field.SUMMARYID
    						+","+ContactSummaryTable.TABLE_NAME+"."+ Field.ALTDETAILTYPE
    						+","+ActivitiesTable.TABLE_NAME+"."+ ActivitiesTable.Field.TYPE
    						+","+ActivitiesTable.TABLE_NAME+"."+ ActivitiesTable.Field.TIMESTAMP
    						+","+ActivitiesTable.TABLE_NAME+"."+ ActivitiesTable.Field.DESCRIPTION 
    						+" FROM "+ContactSummaryTable.TABLE_NAME +","+ ActivitiesTable.TABLE_NAME 
    						+" WHERE "+ContactSummaryTable.TABLE_NAME+"."+Field.LOCALCONTACTID+"="+ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.LOCAL_CONTACT_ID
        					+" AND ("+ getActivityTypes()
                            +") AND "+ ContactSummaryTable.TABLE_NAME+"."+ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
                            +" GROUP BY " +ContactSummaryTable.TABLE_NAME+"."+ContactSummaryTable.Field.LOCALCONTACTID
                            +" ORDER BY "+ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.TIMESTAMP+" DESC", null);
    			}
    		} catch (SQLException e) {
    			LogUtils.logE("ContactSummeryTable.fetchRecentContactList() "
    					+ "SQLException - Unable to fetch filtered summary cursor", e);
    			return null;
    		}
    	}
    	
    	/**
    	 * Returns a list of all the activity types to be included in the query
    	 * @return types 
    	 */
    	
    	private static String getActivityTypes(){
    		String types = "";
    		types = ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.TYPE+"='call_dialed' OR "
    				+ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.TYPE+"='call_received' OR "
    				+ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.TYPE+"='call_missed' OR "
    				+ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.TYPE+"='message_sms_sent' OR "
    				+ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.TYPE+"='messsage_sms_received' OR "
    				+ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.TYPE+"='message_email_received' OR "
    				+ActivitiesTable.TABLE_NAME+"."+ActivitiesTable.Field.TYPE+"='message_email_sent'";
    		return types;
    	}
    	
    	/**
    	 * This method returns a cursor to the most frequent contacts
    	 * @param readableDb the readable database
    	 * @return A cursor of frequent contacts or null
    	 */
    	
    	public static Cursor fetchFrequentContactList(Long groupFilterId, Long meProfileId,SQLiteDatabase readableDb) {
    		LogUtils.logD("ContactSummeryTable.fetchFrequentContactList()");
    		if (Settings.ENABLED_DATABASE_TRACE) {
    			DatabaseHelper.trace(false, "ContactSummeryTable.fetchFrequentContactList() " + "]");
    		}
    		if(meProfileId == null)
    			meProfileId = -1L;
    		try {
    			if(groupFilterId != null){
    			return readableDb.rawQuery("SELECT " +
    					"a."+Field.LOCALCONTACTID +
    					", a."+ Field.DISPLAYNAME+
    					", a."+ Field.ONLINESTATUS+
    					", a."+ Field.SUMMARYID+
    					", a."+ Field.ALTDETAILTYPE+
    					", b."+ ActivitiesTable.Field.TYPE+
    					", b."+ ActivitiesTable.Field.TIMESTAMP+
    					", b."+ ActivitiesTable.Field.DESCRIPTION+
    					", COUNT(b."+ ActivitiesTable.Field.LOCAL_CONTACT_ID+") FROM " +
    					ContactSummaryTable.TABLE_NAME +" a, "+ ActivitiesTable.TABLE_NAME + " b"+getGroupConstraint(groupFilterId)+" AND a."+Field.LOCALCONTACTID+"=b."+
    					ActivitiesTable.Field.LOCAL_CONTACT_ID+" AND "
                        + "a."+ ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId+
    					" GROUP BY b."+ActivitiesTable.Field.LOCAL_CONTACT_ID+" ORDER BY COUNT(b."+ActivitiesTable.Field.LOCAL_CONTACT_ID+") DESC", null);
    			}else{
    				LogUtils.logD("Group id is null");
    				return readableDb.rawQuery("SELECT " 
    						+"a."+Field.LOCALCONTACTID 
    						+", a."+ Field.DISPLAYNAME
    						+", a."+ Field.ONLINESTATUS
    						+", a."+ Field.SUMMARYID
    						+", a."+ Field.ALTDETAILTYPE
    						+", b."+ ActivitiesTable.Field.TYPE
    						+", b."+ ActivitiesTable.Field.TIMESTAMP
    						+", b."+ ActivitiesTable.Field.DESCRIPTION
    						+", COUNT(b."+ ActivitiesTable.Field.LOCAL_CONTACT_ID+") FROM " 
    						+ContactSummaryTable.TABLE_NAME +" a, "+ ActivitiesTable.TABLE_NAME + " b "
    						+"WHERE a."+Field.LOCALCONTACTID+"=b."+ActivitiesTable.Field.LOCAL_CONTACT_ID
    						+ " AND a."+Field.LOCALCONTACTID+"!="+meProfileId
    						+" AND ("+getActivityTypesCondition()+") GROUP BY b."+ActivitiesTable.Field.LOCAL_CONTACT_ID+" ORDER BY COUNT(b."+ActivitiesTable.Field.LOCAL_CONTACT_ID+") DESC", null);

    			}
    		} catch (SQLException e) {
    			LogUtils.logE("ContactSummeryTable.fetchFrequentContactList() "
    					+ "SQLException - Unable to fetch filtered summary cursor", e);
    			return null;
    		}
    	}
    	
    	/**
    	 * Returns a list of all the activity types to be included in the query for frequent contacts.
    	 * (Ideally must eb same as getActivityTypes() used in fetchRecentContactList(). May require optimization )
    	 * @return types
    	 */

    	private static String getActivityTypesCondition(){
    		String types = "";
    		types = "b."+ActivitiesTable.Field.TYPE+"='call_dialed' OR "
			+"b."+ActivitiesTable.Field.TYPE+"='call_received' OR "
			+"b."+ActivitiesTable.Field.TYPE+"='call_missed' OR "
			+"b."+ActivitiesTable.Field.TYPE+"='message_sms_sent' OR "
			+"b."+ActivitiesTable.Field.TYPE+"='messsage_sms_received' OR "
			+"b."+ActivitiesTable.Field.TYPE+"='message_email_received' OR "
			+"b."+ActivitiesTable.Field.TYPE+"='message_email_sent'";
    		return types;
    	}
    	
    	/**
    	 * This method returns the list of names sorted by last name(Support for groups yet to be added)
    	 */
    	 public static Cursor fetchContactListSortedByLastName(Long groupFilterId, CharSequence constraint,
    	            Long meProfileId, SQLiteDatabase readableDb) {
    		 
    		 if (Settings.ENABLED_DATABASE_TRACE) {
    	            DatabaseHelper.trace(false, "ContactSummeryTable.fetchContactListSortedByLastName() groupFilterId["
    	                    + groupFilterId + "] meProfileId[" + meProfileId + "]");
    	        }
    	        try {
    	            if (groupFilterId == null) {
    	                // Fetch all contacts
    	                return readableDb.rawQuery("SELECT " +ContactSummaryTable.TABLE_NAME+"."+Field.SUMMARYID//+ ContactSummaryTable.getFullQueryList()
    	                		+ ", " +ContactSummaryTable.TABLE_NAME+"."+ContactSummaryTable.Field.LOCALCONTACTID
    	                		+ ", " + ContactDetailsTable.TABLE_NAME+"."+ContactDetailsTable.Field.STRINGVAL
    	                		+ ", " + ContactSummaryTable.TABLE_NAME+"."+ContactSummaryTable.Field.STATUSTEXT
        	                    + ", " + ContactSummaryTable.TABLE_NAME+"."+ContactSummaryTable.Field.DISPLAYNAME
        	                    + " FROM " + ContactSummaryTable.TABLE_NAME+", "+ContactDetailsTable.TABLE_NAME
        	                    + " WHERE " + ContactSummaryTable.TABLE_NAME + "."+ ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
        	                    + " AND "+ContactSummaryTable.TABLE_NAME + "." + ContactSummaryTable.Field.LOCALCONTACTID + "=" + ContactDetailsTable.TABLE_NAME + "."+ContactDetailsTable.Field.LOCALCONTACTID
        	                    + " AND "+ContactDetailsTable.TABLE_NAME + "."+ ContactDetailsTable.Field.KEY+"='0'"
        	                    + " ORDER BY LOWER(" + ContactDetailsTable.TABLE_NAME + "."+ ContactDetailsTable.Field.STRINGVAL + ")", null);
    	            }
    	            return readableDb.rawQuery("SELECT " + ContactSummaryTable.getFullQueryList()
    	                    + " FROM " + ContactSummaryTable.TABLE_NAME + getGroupConstraint(groupFilterId)
    	                    + " AND " + ContactSummaryTable.TABLE_NAME + "."
    	                    + ContactSummaryTable.Field.LOCALCONTACTID + "!=" + meProfileId
    	                    + " ORDER BY LOWER(" + ContactSummaryTable.Field.DISPLAYNAME + ")", null);

    	        } catch (SQLException e) {
    	            LogUtils.logE("ContactSummeryTable.fetchContactListSortedByLastName() "
    	                    + "SQLException - Unable to fetch last name sorted summary cursor", e);
    	            return null;
    	        }
    		 
    	 }
    	
    	
}


