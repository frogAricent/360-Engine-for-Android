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

package com.vodafone360.people.tests.datatypes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import android.test.AndroidTestCase;

import com.vodafone360.people.datatypes.ActivityContact;
import com.vodafone360.people.datatypes.Album;
import com.vodafone360.people.datatypes.AlbumListResponse;
import com.vodafone360.people.datatypes.AlbumResponse;
import com.vodafone360.people.datatypes.BaseDataType;
import com.vodafone360.people.datatypes.Comment;
import com.vodafone360.people.datatypes.CommentListResponse;
import com.vodafone360.people.datatypes.CommentsResponse;
import com.vodafone360.people.datatypes.Contact;
import com.vodafone360.people.datatypes.ContactChanges;
import com.vodafone360.people.datatypes.ContactDetail;
import com.vodafone360.people.datatypes.ContactDetailDeletion;
import com.vodafone360.people.datatypes.ContactListResponse;
import com.vodafone360.people.datatypes.Content;
import com.vodafone360.people.datatypes.ContentListResponse;
import com.vodafone360.people.datatypes.ContentResponse;
import com.vodafone360.people.datatypes.EntityKey;
import com.vodafone360.people.datatypes.FriendshipRequest;
import com.vodafone360.people.datatypes.Group;
import com.vodafone360.people.datatypes.GroupIdListResponse;
import com.vodafone360.people.datatypes.GroupItem;
import com.vodafone360.people.datatypes.IdentitiesTextResponse;
import com.vodafone360.people.datatypes.Identity;
import com.vodafone360.people.datatypes.IdentityCapability;
import com.vodafone360.people.datatypes.IdentityText;
import com.vodafone360.people.datatypes.ItemList;
import com.vodafone360.people.datatypes.ListOfLong;
import com.vodafone360.people.datatypes.LocationNudgeResult;
import com.vodafone360.people.datatypes.LongGeocodeAddress;
import com.vodafone360.people.datatypes.MusicDDForTrack;
import com.vodafone360.people.datatypes.MusicDDObject;
import com.vodafone360.people.datatypes.MusicDownloadableTrack;
import com.vodafone360.people.datatypes.PrivacySetting;
import com.vodafone360.people.datatypes.PrivacySettingList;
import com.vodafone360.people.datatypes.PublicKeyDetails;
import com.vodafone360.people.datatypes.PushEvent;
import com.vodafone360.people.datatypes.StatusMsg;
import com.vodafone360.people.datatypes.Tag;
import com.vodafone360.people.datatypes.UserProfile;
import com.vodafone360.people.datatypes.IdentityCapability.CapabilityID;
import com.vodafone360.people.engine.EngineManager.EngineId;
import com.vodafone360.people.engine.music.MusicDownloader;
import com.vodafone360.people.service.io.rpg.PushMessageTypes;
import com.vodafone360.people.service.io.rpg.RpgPushMessage;

public class NowPlusDatatypesTests extends AndroidTestCase {

	public void testActivityContact() {
		ActivityContact input = new ActivityContact();
		input.mAddress = "foo";
		input.mAvatarUrl = "foo";
		input.mContactId = 1L;
		input.mName = "bar";
		input.mNetwork = "mob";

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("address", input.mAddress);
		hash.put("avatarurl", input.mAvatarUrl);
		hash.put("contactid", input.mContactId);
		hash.put("name", input.mName);
		hash.put("network", input.mNetwork);

		ActivityContact output = ActivityContact.createFromHashTable(hash);

		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.mAddress, output.mAddress);
		assertEquals(input.mAvatarUrl, output.mAvatarUrl);
		assertEquals(input.mContactId, output.mContactId);
		assertEquals(input.mName, output.mName);
		assertEquals(input.mNetwork, output.mNetwork);
	}

	public void testContactChanges() {
		
		List<Contact> contacts = new ArrayList<Contact>();
		long currentServerVersion = 1;
		long versionAnchor = 2;
		int numberOfPages = 3;
		long serverRevisionBefore = 4;
		long serverRevisionAfter = 5;
		Hashtable<String, Object> hashUserProfile = new Hashtable<String, Object>();

		ContactChanges input = new ContactChanges();
		input.mContacts = contacts;
		input.mCurrentServerVersion = ((Long) currentServerVersion).intValue();
		input.mVersionAnchor = ((Long) versionAnchor).intValue();
		input.mNumberOfPages = numberOfPages;
		input.mServerRevisionBefore = ((Long) serverRevisionBefore).intValue();
		input.mServerRevisionAfter = ((Long) serverRevisionAfter).intValue();
		input.mUserProfile = UserProfile.createFromHashtable(hashUserProfile);

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("contact", contacts);
		hash.put("currentserverrevision", currentServerVersion);
		hash.put("serverrevisionanchor", versionAnchor);
		hash.put("numpages", numberOfPages);
		hash.put("serverrevisionbefore", serverRevisionBefore);
		hash.put("serverrevisionafter", serverRevisionAfter);
		hash.put("userprofile", hashUserProfile);

		ContactChanges helper = new ContactChanges();
		ContactChanges output = helper.createFromHashtable(hash);

		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.mContacts, output.mContacts);
		assertEquals(input.mCurrentServerVersion, output.mCurrentServerVersion);
		assertEquals(input.mNumberOfPages, output.mNumberOfPages);
		assertEquals(input.mServerRevisionBefore, output.mServerRevisionBefore);
		assertEquals(input.mServerRevisionAfter, output.mServerRevisionAfter);
	}

	public void testContactDetailDeletion() {
		long serverVersionBefore = 1;
		long serverVersionAfter = 2;
		long contactId = 3;

		ContactDetailDeletion input = new ContactDetailDeletion();
		input.mServerVersionBefore = ((Long) serverVersionBefore).intValue();
		input.mServerVersionAfter = ((Long) serverVersionAfter).intValue();
		input.mContactId = ((Long) contactId).intValue();

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("serverrevisionbefore", serverVersionBefore);
		hash.put("serverrevisionafter", serverVersionAfter);
		hash.put("contactid", contactId);

		ContactDetailDeletion helper = new ContactDetailDeletion();
		ContactDetailDeletion output = helper.createFromHashtable(hash);

		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.mServerVersionBefore, output.mServerVersionBefore);
		assertEquals(input.mServerVersionAfter, output.mServerVersionAfter);
		assertEquals(input.mContactId, output.mContactId);
	}

	public void testContactListResponse() {
		long serverRevisionBefore = 1;
		long serverRevisionAfter = 2;
		List<Integer> contactIdList = new ArrayList<Integer>();
		Integer contactId = 3;

		ContactListResponse input = new ContactListResponse();
		input.mServerRevisionBefore = ((Long) serverRevisionBefore).intValue();
		input.mServerRevisionAfter = ((Long) serverRevisionAfter).intValue();
		input.mContactIdList = contactIdList;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("serverrevisionbefore", serverRevisionBefore);
		hash.put("serverrevisionafter", serverRevisionAfter);
		hash.put("contactidlist", contactIdList);
		hash.put("contactid", contactId);

		ContactListResponse helper = new ContactListResponse();
		ContactListResponse output = helper.createFromHashTable(hash); // createFromHashTable
		// should
		// be
		// static

		input.mContactIdList.add(contactId);
		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.mServerRevisionBefore, output.mServerRevisionBefore);
		assertEquals(input.mServerRevisionAfter, output.mServerRevisionAfter);
		assertEquals(input.mContactIdList, output.mContactIdList);
	}

	public void testGroupItem() {
		int groupType = 1;
		boolean isReadOnly = true;
		boolean requiresLocalisation = true;
		boolean isSystemGroup = true;
		boolean isSmartGroup = true;
		long id = 3;
		long userId = 4;
		String name = "foo";

		GroupItem input = new GroupItem();
		input.mGroupType = (Integer) groupType;
		input.mIsReadOnly = (Boolean) isReadOnly;
		input.mRequiresLocalisation = (Boolean) requiresLocalisation;
		input.mIsSystemGroup = (Boolean) isSystemGroup;
		input.mIsSmartGroup = (Boolean) isSmartGroup;
		input.mId = (Long) id;
		input.mUserId = (Long) userId;
		input.mName = name;
		input.mColor = "red";

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("grouptype", groupType);
		hash.put("isreadonly", isReadOnly);
		hash.put("requireslocalisation", requiresLocalisation);
		hash.put("issystemgroup", isSystemGroup);
		hash.put("issmartgroup", isSmartGroup);
		hash.put("id", id);
		hash.put("userid", userId);
		hash.put("name", name);
		hash.put("color", input.mColor);

		GroupItem helper = new GroupItem();
		GroupItem output = helper.createFromHashtable(hash);

		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.mGroupType, output.mGroupType);
		assertEquals(input.mIsReadOnly, output.mIsReadOnly);
		assertEquals(input.mRequiresLocalisation, output.mRequiresLocalisation);
		assertEquals(input.mIsSystemGroup, output.mIsSystemGroup);
		assertEquals(input.mIsSmartGroup, output.mIsSmartGroup);
		assertEquals(input.mId, output.mId);
		assertEquals(input.mUserId, output.mUserId);
		assertEquals(input.mName, output.mName);
		assertEquals(input.mColor, output.mColor);
	}
	
	public void testGroupIdListResponse()
	{
		GroupIdListResponse input = new GroupIdListResponse();
		input.mCode = "code";
		input.mErr = "error";
		input.mItem = new Long(0);
		input.mStat = "stat";
	
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("code",input.mCode);
		hash.put("err",input.mErr);
		hash.put("item",input.mItem);
		hash.put("stat",input.mStat);

		GroupIdListResponse output = (new GroupIdListResponse()).createFromHashTable(hash);
		assertEquals(BaseDataType.GROUP_ID_LIST_DATATYPE, output.getType());
		assertEquals(input.mCode,output.mCode);
		assertEquals(input.mErr,output.mErr);
		assertEquals(input.mStat,output.mStat);
		assertEquals(input.mItem,output.mItem);
	}
	
	public void testIdentityCapability() {
		IdentityCapability input = new IdentityCapability();
		input.mCapability = CapabilityID.share_media;
		input.mDescription = "des";
		input.mName = "name";
		input.mValue = true;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("capabilityid", input.mCapability.name());
		hash.put("description", input.mDescription);
		hash.put("name", input.mName);
		hash.put("value", input.mValue);

		IdentityCapability helper = new IdentityCapability();
		IdentityCapability output = helper.createFromHashtable(hash);

		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.describeContents(), output.describeContents());
		assertEquals(input.mCapability, output.mCapability);
		assertEquals(input.mDescription, output.mDescription);
		assertEquals(input.mName, output.mName);
		assertEquals(input.mValue, output.mValue);
	}

	public void testIdentity() {
		Identity input = new Identity();
		input.mPluginId = "pluginid";
		input.mNetwork = "network";
		input.mIdentityId = "identityId";
		input.mDisplayName = "displayname";
		input.mCreated = new Long(12);
		input.mUpdated = new Long(23);
		input.mActive = true;
		input.mAuthType = "none";
		input.mIdentityType = "chat";
		input.mUserId = new Integer(1234);
		input.mUserName = "bob";
		input.mCountryList = new ArrayList<String>();

		String urlString = "http://www.mobica.com/";
		try {
			input.mNetworkUrl = new URL(urlString);
		} catch (MalformedURLException e) {
			input.mNetworkUrl = null;
		}

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("pluginid", input.mPluginId);
		hash.put("network", input.mNetwork);
		hash.put("identityid", input.mIdentityId);
		hash.put("displayname", input.mDisplayName);
		hash.put("networkurl", urlString);
		hash.put("created", input.mCreated);
		hash.put("updated", input.mUpdated);
		hash.put("active", true);
		hash.put("authtype", input.mAuthType);
		hash.put("identitytype", input.mIdentityType);
		hash.put("userid", new Long(1234));
		hash.put("username", input.mUserName);
		hash.put("countrylist", input.mCountryList);

		Identity helper = new Identity();
		Identity output = helper.createFromHashtable(hash);

		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertTrue(input.isSameAs(output));
	}

	public void testItemList() {
		ItemList groupPriv = new ItemList(ItemList.Type.group_privacy);
		int groupType = 1;
		boolean isReadOnly = true;
		boolean requiresLocalisation = true;
		boolean isSystemGroup = true;
		boolean isSmartGroup = true;
		long id = 3;
		long userId = 4;
		String name = "foo";
		Hashtable<String, Object> hashGroup = new Hashtable<String, Object>();
		hashGroup.put("grouptype", groupType);
		hashGroup.put("isreadonly", isReadOnly);
		hashGroup.put("requireslocalisation", requiresLocalisation);
		hashGroup.put("issystemgroup", isSystemGroup);
		hashGroup.put("issmartgroup", isSmartGroup);
		hashGroup.put("id", id);
		hashGroup.put("userid", userId);
		hashGroup.put("name", name);

		Vector<Hashtable<String, Object>> vect = new Vector<Hashtable<String, Object>>();
		vect.add(hashGroup);
		Hashtable<String, Object> hashItemListGroup = new Hashtable<String, Object>();
		hashItemListGroup.put("itemlist", vect);

		groupPriv.populateFromHashtable(hashItemListGroup);
		GroupItem helper = new GroupItem();

		GroupItem input = helper.createFromHashtable(hashGroup);
		GroupItem output = (GroupItem) groupPriv.mItemList.get(0);
		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.mGroupType, output.mGroupType);
		assertEquals(input.mIsReadOnly, output.mIsReadOnly);
		assertEquals(input.mRequiresLocalisation, output.mRequiresLocalisation);
		assertEquals(input.mIsSystemGroup, output.mIsSystemGroup);
		assertEquals(input.mIsSmartGroup, output.mIsSmartGroup);
		assertEquals(input.mId, output.mId);
		assertEquals(input.mUserId, output.mUserId);
		assertEquals(input.mName, output.mName);
	}

	public void testPublicKeyDetails() {
		byte[] modulo = new byte[] { 0, 0 };
		byte[] exponential = new byte[] { 0, 1 };
		byte[] key = new byte[] { 1, 1 };
		String keyBase64 = "64";

		PublicKeyDetails input = new PublicKeyDetails();
		input.mModulus = modulo;
		input.mExponential = exponential;
		input.mKeyX509 = key;
		input.mKeyBase64 = keyBase64;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("modulo", modulo);
		hash.put("exponential", exponential);
		hash.put("key", key);
		hash.put("keybase64", keyBase64);

		PublicKeyDetails output = PublicKeyDetails.createFromHashtable(hash);

		assertEquals(input.describeContents(), output.describeContents());
		assertEquals(input.getType(), output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.mModulus, output.mModulus);
		assertEquals(input.mExponential, output.mExponential);
		assertEquals(input.mKeyX509, output.mKeyX509);
		assertEquals(input.mKeyBase64, output.mKeyBase64);
	}

	public void testCreatePushEvent() {
		RpgPushMessage msg = new RpgPushMessage();
		msg.mType = PushMessageTypes.CONTACTS_CHANGE;
		EngineId engId = EngineId.ACTIVITIES_ENGINE;

		PushEvent input = (PushEvent) PushEvent.createPushEvent(msg, engId);

		assertEquals(BaseDataType.PUSH_EVENT_DATA_TYPE, input.getType());
		assertEquals(msg.mType, input.mMessageType);
		assertEquals(engId, input.mEngineId);
	}

	public void testStatusMsg() {
		boolean status = true;
		boolean dryRun = true;

		StatusMsg input = new StatusMsg();
		input.mStatus = (Boolean) status;
		input.mDryRun = (Boolean) dryRun;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("status", status);
		hash.put("dryrun", dryRun);

		StatusMsg helper = new StatusMsg();
		StatusMsg output = helper.createFromHashtable(hash);

		assertEquals(BaseDataType.STATUS_MSG_DATA_TYPE, output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.mStatus, output.mStatus);
		assertEquals(input.mDryRun, output.mDryRun);
	}

	public void testUserProfile() {
		
		UserProfile input = new UserProfile();
		input.userID = 50L;
		input.aboutMe = "newAboutMe";
		input.contactID = 10L;
		input.gender = 1;
		input.profilePath = "foo";
		input.updated = 2L;
		ContactDetail contactDetail = new ContactDetail();
		contactDetail.value = "00000000";
		Vector<Long> gL  = new Vector<Long>();
		gL.add(new Long(2));
		Vector<String> vals = new Vector<String>();
		vals.add("sources");
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("userid", input.userID);
		hash.put("aboutme", input.aboutMe);
		hash.put("contactid", input.contactID);
		hash.put("gender", input.gender);
		hash.put("profilepath", input.profilePath);
		hash.put("updated", input.updated);
		hash.put("foflist", gL);
		hash.put("me", true);
		hash.put("sources",vals);
		hash.put("detail", new Integer(0));

		UserProfile output = UserProfile.createFromHashtable(hash);

		assertEquals(BaseDataType.USER_PROFILE_DATA_TYPE, output.getType());
		assertEquals(input.toString(), output.toString());
		assertEquals(input.userID, output.userID);
		assertEquals(input.aboutMe, output.aboutMe);
		assertEquals(input.contactID, output.contactID);
		assertEquals(input.gender, output.gender);
		assertEquals(input.profilePath, output.profilePath);
		assertEquals(input.updated, output.updated);
	}

	/**
	 * Method to test Comment Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testComment() {
	
		Comment input = new Comment();
		input.mCommentId = 0L;
		input.mText = "Test comment";
		input.mInappropriate = false;
		input.mExtCommentId = "comment";
		
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("commentid", input.mCommentId);
		hash.put("text", input.mText);
		hash.put("inappropriate", input.mInappropriate);
		hash.put("extcommentid", input.mExtCommentId);

		Comment output = Comment.createFromHashtable(hash);

		assertEquals(BaseDataType.COMMENTS_DATATYPE, output.getType());
		assertEquals(input.mCommentId, output.mCommentId);
		assertEquals(input.mExtCommentId, output.mExtCommentId);
		assertEquals(input.mText, output.mText);
		assertEquals(input.mInappropriate, output.mInappropriate);
		

	}

	/**
	 * Method to test CommentsResponse Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testCommentsResponse() {

		Vector<Long> commentid = new Vector<Long>();
		commentid.add(1L);

		CommentsResponse input = new CommentsResponse();
		input.mCommentIdList = commentid;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("commentidlist",commentid);

		CommentsResponse output = new CommentsResponse().createFromHashtable(hash);
		assertEquals(BaseDataType.COMMENTS_RESPONSE_DATATYPE, output.getType());
		assertEquals(input.mCommentIdList, output.mCommentIdList);
	}

	/**
	 * Method to test CommentListResponse Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testCommentsListResponse() {
		
		Vector<Comment> commentList = new Vector<Comment>();
		Comment comment = new Comment();
		comment.mCommentId = 1L;
		comment.mText = "Test comment";
		comment.mInappropriate = false;
		commentList.add(comment);
	
		Hashtable<String, Object> hashgrp = new Hashtable<String, Object>();
		hashgrp.put("commentid", comment.mCommentId);
		
		Vector<Hashtable<String, Object>> vect = new Vector<Hashtable<String, Object>>();
		vect.add(hashgrp);
		
		CommentListResponse input = new CommentListResponse();
		input.mCommentList = commentList;
		input.mItems = 1;
		input.mUpdated = 1L;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("commentlist", vect);
		hash.put("items", input.mItems);
		hash.put("updated", input.mUpdated);

		CommentListResponse output = new CommentListResponse().createFromHashTable(hash);

		assertEquals(BaseDataType.COMMENT_LIST_DATATYPE, output.getType());
		//assertEquals(input.toString(), output.toString());
		assertEquals(input.mItems, output.mItems);
		assertEquals(input.mUpdated, output.mUpdated);
	}

	/**
	 * Method to test EntityKey Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testEntityKey() {
		EntityKey input = new EntityKey();
		input.setEntityId(new Long(1));
		input.setEntityType("Album");
		input.setUserId(new Long(50));
		
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("entityid", input.getEntityId());
		hash.put("userid", input.getUserId());
		hash.put("entitytype",input.getEntityType());
		input.getType();

		EntityKey output = new EntityKey();
		output.createFromHashtable(hash);
		
		assertEquals(BaseDataType.ENTITY_KEY_DATATYPE, output.getType());
		assertEquals(input.mEntityId, output.mEntityId);
		assertEquals(input.mEntityType, output.mEntityType);
	}

	/**
	 * Method to test Album Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testAlbum() {
		Album input = new Album();
		input.mAlbumid = 1L;
		input.mCreated = 0L;
		input.mIconid = 2L;
		input.mIconurl = "www.facebook.com/album1";
		input.mReadonly = false;
		input.mSlug = "Test album";
		input.mUpdated = 0L;
		input.mTitle = "Album";

		GroupItem grpItm = new GroupItem();
		grpItm.mId = 2L;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("albumid", input.mAlbumid);
		hash.put("created", input.mCreated);
		hash.put("iconid", input.mIconid);
		hash.put("iconurl", input.mIconurl);
		hash.put("readonly", input.mReadonly);
		hash.put("slug", input.mSlug);
		hash.put("updated", input.mUpdated);
		hash.put("title", input.mTitle);

		Album output = new Album().createFromHashtable(hash);

		assertEquals(BaseDataType.ALBUM_DATATYPE, output.getType());
		assertEquals(input.mAlbumid, output.mAlbumid);
		assertEquals(input.mCreated, output.mCreated);
		assertEquals(input.mIconid, output.mIconid);
		assertEquals(input.mIconurl, output.mIconurl);
		assertEquals(input.mReadonly, output.mReadonly);
		assertEquals(input.mSlug, output.mSlug);
		assertEquals(input.mUpdated, output.mUpdated);
		assertEquals(input.mTitle, output.mTitle);
	}

	/**
	 * Method to test AlbumListResponse Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testAlbumListResponse() {
		List<Album> album = new ArrayList<Album>();
		Album albumList = new Album();
		albumList.mAlbumid = 1L;
		albumList.mCreated = 0L;
		albumList.mIconid = 2L;
		albumList.mIconurl = "www.facebook.com/album1";
		albumList.mReadonly = false;
		albumList.mSlug = "Test album";
		albumList.mUpdated = 0L;
		albumList.mTitle = "Album";
		album.add(albumList);
		
		Hashtable<String, Object> hashgrp = new Hashtable<String, Object>();
		hashgrp.put("albumlist", album);
		
		Vector<Hashtable<String, Object>> vect = new Vector<Hashtable<String, Object>>();
		vect.add(hashgrp);
		
		AlbumListResponse input = new AlbumListResponse();
		input.mAlbumList = album;
		input.mItems = 1;
		input.mUpdated = 0L;

		
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("items", input.mItems);
		hash.put("updated", input.mUpdated);
		hash.put("albumlist", vect);
		
			
		AlbumListResponse output = new AlbumListResponse();
		output.createFromHashTable(hash);

		assertEquals(BaseDataType.ALBUM_LIST_RESPONSE_DATATYPE, output
				.getType());
		assertEquals(input.mItems, output.mItems);
		assertEquals(input.mUpdated, output.mUpdated);
		

	}

	/**
	 * Method to test AlbumResponse Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testAlbumResponse() {
		Vector<Long> albumList = new Vector<Long>();
		albumList.add(0L);
		albumList.add(2L);
		albumList.add(3L);

		Vector<Long> groupList = new Vector<Long>();
		groupList.add(4L);
		groupList.add(5L);
		groupList.add(6L);
		
		AlbumResponse input = new AlbumResponse();
		input.mAlbumList = albumList;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("albumidlist", input.mAlbumList);
		hash.put("groupidlist", groupList);
		hash.put("userid", new Long(1));

		AlbumResponse output = new AlbumResponse().createFromHashtable(hash);

		assertEquals(BaseDataType.ALBUM_RESPONSE_DATATYPE, output.getType());
		assertEquals(input.mAlbumList, output.mAlbumList);
		assertEquals(input.mListSize, output.mListSize);
	}

	/**
	 * Method to test Content Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	
	public void testContent() {
		
		Content input = new Content();
		
		Content output = null;
		input.mCommentscount = new Integer(7);
		input.mContentid = 0L;
		input.mRemoteid = "abc";
		input.mFilesize = 0L;
		input.mMaxage = 1L;
		input.mPreviewurl = "www.mobica.com";
		input.mUrl = "www.mobica.com/content";
		byte[] bytes = new byte[1];
		input.mBytes = bytes;
		input.mBytesmime = "bytesMime";

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("commentscount", input.mCommentscount);
		hash.put("contentid", input.mContentid);
		hash.put("remoteid", input.mRemoteid);
		hash.put("filesize", input.mFilesize);
		hash.put("maxage", input.mMaxage);
		hash.put("previewurl", input.mPreviewurl);
		hash.put("url", input.mUrl);
		hash.put("bytes", input.mBytes);
		hash.put("bytesmime", input.mBytesmime);
		try{
		output = new Content().createFromHashtable(hash);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(BaseDataType.CONTENT_DATATYPE, output.getType());
		assertEquals(input.mPreviewurl, output.mPreviewurl);
		assertEquals(input.mCommentscount, output.mCommentscount);
		assertEquals(input.mContentid, output.mContentid);
		assertEquals(input.mRemoteid, output.mRemoteid);
		assertEquals(input.mPreviewurl, output.mPreviewurl);
		assertEquals(input.mMaxage, output.mMaxage);
		assertEquals(input.mFilesize, output.mFilesize);
		//assertEquals(input.mBytesmime, output.mBytesmime);
	
	}

	/**
	 * Method to test ContentListResponse Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testContentListResponse() {
		Vector<Long> contentIdList = new Vector<Long>();
		contentIdList.add(1L);
		ContentListResponse input = new ContentListResponse();
		input.mContentIdList = contentIdList;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("contentidlist", input.mContentIdList);

		ContentListResponse output = new ContentListResponse().createFromHashtable(hash);

		assertEquals(BaseDataType.CONTENT_LIST_RESPONSE_DATATYPE, output.getType());
		assertEquals(input.mContentIdList, output.mContentIdList);

	}

	/**
	 * Method to test Contentresponse Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testContentResponse() {
		
		List<Long> mContentIdList = new ArrayList<Long>();
		mContentIdList.add(new Long(2324));
		
		Hashtable<String, Object> object = new Hashtable<String, Object>();
		object.put("contentlist",mContentIdList);
		
		Vector<Hashtable<String, Object>> contentsVector = new Vector<Hashtable<String,Object>>();
		contentsVector.add(object);
		
		ContentResponse input = new ContentResponse();
		ContentResponse output = null;
		input.mItems = 2;
		input.mUpdated = 0L;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("contentlist", contentsVector);
		hash.put("items", input.mItems);
		hash.put("updated", input.mUpdated);
		try{
		output = (new ContentResponse()).createFromHashtable(hash);
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}

		assertEquals(BaseDataType.CONTENT_RESPONSE_DATATYPE, output.getType());
		assertEquals(input.mItems, output.mItems);
		assertEquals(input.mUpdated, output.mUpdated);
	}

	/**
	 * Method to test Tag Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testTag()
	{
		Tag input = new Tag();
		input.tagid = 1L;
		input.userid = 2L;
		input.name = "Name";
		input.type = "type";
		input.exttagid = "ExttagId";
		input.count = 3;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("tagid", input.tagid);
		hash.put("userid", input.userid);
		hash.put("name", input.name);
		hash.put("type", input.type);
		hash.put("exttagid", input.exttagid);
		hash.put("count", input.count);
		
		Tag output = Tag.createFromHashtable(hash);
		
		assertEquals(BaseDataType.TAG_DATATYPE, output.getType());
		assertEquals(input.tagid, output.tagid);
		assertEquals(input.userid, output.userid);
		assertEquals(input.name, output.name);
		assertEquals(input.type, output.type);
		assertEquals(input.exttagid, output.exttagid);
		assertEquals(input.count, output.count);
	}

	/**
	 * Method to test LongGeocodeAddress Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testLongGeocodeAddress() {

		LongGeocodeAddress input = new LongGeocodeAddress();
		input.AREA_NAME = "area name ";
		input.CITY_NAME = "city name ";
		input.COUNTRY_NAME = "country name ";
		input.LATITUDE = "latitude ";
		input.LONGITUDE = "longitude";
		input.STREET_NAME = "";
		

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("areaname", input.AREA_NAME);
		hash.put("cityname", input.CITY_NAME);
		hash.put("countryname", input.COUNTRY_NAME);
		hash.put("latitude", input.LATITUDE);
		hash.put("longitude", input.LONGITUDE);		
		hash.put("streetname", input.STREET_NAME);
		
			
		LongGeocodeAddress output = new LongGeocodeAddress();

		assertEquals(BaseDataType.LONG_GEOCODE_ADDRESS_DATATYPE, output.getType());
		assertEquals(input.AREA_NAME.toString(), output.AREA_NAME);
		assertEquals(input.CITY_NAME, output.CITY_NAME);
		assertEquals(input.COUNTRY_NAME, output.COUNTRY_NAME);
		assertEquals(input.LATITUDE, output.LATITUDE);
		assertEquals(input.LONGITUDE, output.LONGITUDE);
		assertEquals(input.STREET_NAME, output.STREET_NAME);
	}

	/**
	 * Method to test LocationNudgeResult Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testLocationNudgeResult() {
		LocationNudgeResult input = new LocationNudgeResult();
		input.reason = "reason";
		input.success = true;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("reason", input.reason);
		hash.put("success", input.success);

		LocationNudgeResult output = (new LocationNudgeResult()).createFromHashtable(hash);
		
		assertEquals(BaseDataType.LOCATION_NUDGE_RESULT_DATATYPE, output.getType());
		assertEquals(input.reason, output.reason);
		assertEquals(input.success, output.success);
	}

	/**
	 * Method to test PrivacySetting Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testPrivacySetting() {
		PrivacySetting input = new PrivacySetting();
		input.mContentType = 1;
		input.mGroupId = 1L;
		input.mState = 2;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("contenttype", input.mContentType);
		hash.put("groupid", input.mGroupId);
		hash.put("state", input.mState);

		PrivacySetting output = (new PrivacySetting())
				.createFromHashtable(hash);

		assertEquals(BaseDataType.PRIVACY_SETTING_DATATYPE, output.getType());
		assertEquals(input.mContentType, output.mContentType);
		assertEquals(input.mState, output.mState);
		assertEquals(input.mGroupId, output.mGroupId);
	}

	/**
	 * Method to test PrivacySettingList Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testPrivacySettingList() {

		PrivacySetting privacySetting = new PrivacySetting();
		privacySetting.mContentType = 1;
		privacySetting.mGroupId = 2L;
		privacySetting.mState = 3;

		List<PrivacySetting> privSetList = new ArrayList<PrivacySetting>();
		//privSetList.add(privacySetting);

		PrivacySettingList input = new PrivacySettingList();
		input.mItemList = privSetList;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("ItemList", input.mItemList);
		PrivacySettingList output = new PrivacySettingList();
		output.populateFromHashtable(hash);
		
		assertEquals(BaseDataType.PRIVACY_SETTING_LIST_DATATYPE, output.getType());
		assertEquals(input.mItemList, output.mItemList);

	}

	/**
	 * Method to test FriendshipRequest Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testFriendshipRequest() {
		
		List<ContactDetail> myList = new ArrayList<ContactDetail>();
		ContactDetail conDet = new ContactDetail();
		conDet.localContactID = 0L;
		myList.add(conDet);
	
		UserProfile profile = new UserProfile();
		profile.userID = 50L;
		profile.aboutMe = "newAboutMe";
		profile.contactID = 10L;
		profile.gender = 1;
		profile.profilePath = "foo";
		profile.updated = 2L;

		Hashtable<String, Object> hash1 = new Hashtable<String, Object>();
		hash1.put("userid", profile.userID);
		hash1.put("aboutme", profile.aboutMe);
		hash1.put("contactid", profile.contactID);
		hash1.put("gender", profile.gender);
		hash1.put("profilepath", profile.profilePath);
		hash1.put("updated", profile.updated);
		
		FriendshipRequest input = new FriendshipRequest();
		input.mMessage = "message";
		input.mRequestId = 1L;
		input.mTimeStamp = 2L;
		
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("userprofile", hash1);
		hash.put("message", input.mMessage);
		hash.put("requestid", input.mRequestId);
		hash.put("timestamp", input.mTimeStamp);

		FriendshipRequest output = FriendshipRequest.createFromHashtable(hash);
		assertEquals(BaseDataType.FRIENDSHIP_REQUEST_DATATYPE, output.getType());
		assertEquals(input.mMessage, output.mMessage);
		assertEquals(input.mRequestId, output.mRequestId);
		assertEquals(input.mTimeStamp, output.mTimeStamp);
	}

	/**
	 * Method to test Group Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testGroup() {
		Group input = new Group();
		input.mId = 1L;
		input.mColor = "color";
		input.mName = "Name";
		input.mNetwork = "Network";
		input.mUserId = 2L;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("id", input.mId);
		hash.put("color", input.mColor);
		hash.put("name", input.mName);
		hash.put("network", input.mNetwork);
		hash.put("userid", input.mUserId);

		Group output = (new Group()).createFromHashtable(hash);
		input.createHashtable();
		assertEquals(BaseDataType.GROUP_DATATYPE, output.getType());
		assertEquals(input.mId, output.mId);
		assertEquals(input.mColor, output.mColor);
		assertEquals(input.mName, output.mName);
		assertEquals(input.mNetwork, output.mNetwork);
		assertEquals(input.mUserId, output.mUserId);
	}

	/**
	 * Method to test ListofLong Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testListofLong()
	{
		Vector<Long> mList = new Vector<Long>();

		ListOfLong input = new ListOfLong();
		input.mLongList = mList;
		input.mListSize = 0;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("approvedrequests", input.mLongList);
		hash.put("rejectedrequests", input.mLongList);
		hash.put("deleteduserids", input.mLongList);
		hash.put("userid", new Long(0L));
		hash.put("itemlist",input.mLongList);
		hash.put("groupidlist",input.mLongList);
		hash.put("albumidlist",input.mLongList);
		hash.put("listSize", input.mListSize);

		ListOfLong output = (new ListOfLong()).createFromHashtable(hash);
		
		assertEquals(BaseDataType.LIST_OF_LONG_DATATYPE, output.getType());
		assertEquals(input.mLongList, output.mLongList);
		assertEquals(input.mListSize, output.mListSize);
	}
	
	/**
	 * Method to test Identity Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testIdentityText()
	{

		IdentityText input = new IdentityText();
		input.mNetwork = "facebook.com";
		input.mText = "Terms and Condition";

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("network", input.mNetwork);
		hash.put("text", input.mText);

		IdentityText output = IdentityText.createFromHashtable(hash);
		
		assertEquals(BaseDataType.IDENTITY_TEXT_RESPONSE, output.getType());
		assertEquals(input.mNetwork, output.mNetwork);
		assertEquals(input.mText, output.mText);
	}

	/**
	 * Method to test Identity Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */
	public void testGetIdentitiesText()
	{
		IdentityText input1 = new IdentityText();
		input1.mNetwork = "facebook.com";
		input1.mText = "Terms and Condition";

		Hashtable<String, Object> hash1 = new Hashtable<String, Object>();
		hash1.put("network", input1.mNetwork);
		hash1.put("text", input1.mText);
		
		Vector<Hashtable<String, Object>> v = new Vector<Hashtable<String,Object>>();
		v.add(hash1);
		
		IdentitiesTextResponse input = new IdentitiesTextResponse();
		input.items = 0;
		input.updated = 0L;

		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("identityText", v);
		hash.put("item", input.items);
		hash.put("updated", input.updated);

		IdentitiesTextResponse output = IdentitiesTextResponse.createFromHashtable(hash);
		assertEquals(BaseDataType.GET_IDENTITIES_TEXT_RESPONSE, output.getType());
		assertEquals(input.items, output.items);
		assertEquals(input.updated, output.updated);
	}
	/**
	 * 
	 */
	public void testMusicDDForTrack()
	{
		
		MusicDDForTrack input = new MusicDDForTrack();
		input.ddResultCode = "ddResultCode";
		input.ddResultText = "ddresulttext";
		input.downloadDescriptor = "downloaddescriptor";
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("ddresultcode", input.ddResultCode);
		hash.put("ddresulttext", input.ddResultText);
		hash.put("downloaddescriptor", input.downloadDescriptor);
		MusicDDForTrack output = (new MusicDDForTrack()).createFromHashtable(hash);
		input.createHashtable();
		assertEquals(BaseDataType.DD_FOR_TRACKS, output.getType());
		assertEquals(input.ddResultCode, output.ddResultCode);
		assertEquals(input.ddResultText, output.ddResultText);
	}
	
	public void testMusicDownloadableTrack() 
	{
		MusicDownloadableTrack input = new MusicDownloadableTrack();
		Vector<String> trackIdList = new Vector<String>();
		trackIdList.add("Track");
		input.trackIdList = trackIdList;
		input.resultCode = "result code";
		input.success = true;
		
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("resultcode", input.resultCode);
		hash.put("success", input.success);
		hash.put("trackidlist", input.trackIdList);
		
		MusicDownloadableTrack output = new MusicDownloadableTrack().createFromHashtable(hash);
		input.createHashtable();
		
		assertEquals(BaseDataType.DOWNLOADABLE_MUSIC, output.getType());
		assertEquals(input.resultCode, output.resultCode);
		assertEquals(input.success, output.success);
		assertEquals(input.trackIdList, output.trackIdList);
	}
	
	public void testMusicDDObject() 
	{
		MusicDDObject input = new MusicDDObject();
		input.setName("bob");
		input.setInstallNotifyURI("sampleurl");
		input.setFileType("filetype");
		input.setTrackID("DE-17209806");
		input.setDownloadURL("http://localhost:8080");
		input.setIconURI("http://localhost:8080");
		input.setSuppressUserConfirmation(getName());
		input.setSize(1);
		
		Hashtable<String, Object> hash = new Hashtable<String, Object>();
		hash.put("Name", input.getName());
		hash.put("InstallNotifyURI", input.getInstallNotifyURI());
		hash.put("DownloadURL", input.getDownloadURL());
		hash.put("fileType", input.getFileType());
		hash.put("IconURI", input.getIconURI());
		hash.put("SuppressUserConfirmation", input.getSuppressUserConfirmation());
		hash.put("Size", input.getSize());
		hash.put("name", input.getName());
	
		assertEquals(input.getName(),"bob");
		assertEquals(input.getFileType(),"filetype");
	}
	
	/**
	 * Method to test MusicDownlader Datatype
	 * 
	 * @param : null
	 * @returntype : null
	 */

	public void testMusicDownlader()
	{
		MusicDDObject musicDDObject = new MusicDDObject();
		musicDDObject.setDownloadURL("http://www.vodafone.in");
		musicDDObject.setFileType("http://track1.3gp");
		musicDDObject.setSize(1024*10);
		musicDDObject.setTrackID("DE-17209806");
		musicDDObject.setInstallNotifyURI("http://www.java-samples.com/j2me/");
		musicDDObject.setName("Track1");
		musicDDObject.setSuppressUserConfirmation(null);
		
		MusicDownloader downloader = new MusicDownloader(musicDDObject);
		downloader.setFileExtn("file extn");
		downloader.setFileName("filename");
		downloader.setFilePath("C:\\MusicDownload\\");
		downloader.setId("123");
		downloader.setInstallNotifyURI(musicDDObject.getInstallNotifyURI());
		downloader.setRetryCount(1);
		downloader.getFileExtn();
		downloader.getFileName();
		downloader.getFilePath();
		downloader.getId();
		downloader.getInstallNotifyURI();
		downloader.getProgress();
		downloader.getRetryCount();
		downloader.getSize();
		downloader.getStatus();
		downloader.getUrl();
		downloader.cancel();
		downloader.resume();
		assertEquals(downloader.getFileName(), "filename");
	
	}
}
