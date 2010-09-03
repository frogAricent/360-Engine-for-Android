package com.vodafone360.people.datatypes;

/*
 ****************************************************************
 * Copyright (c) 2010 Aricent Technologies (Holdings) Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information 
 * of Aricent Technologies ("Confidential Information").You 
 * shall not disclose such Confidential Information and shall use 
 * it only in accordance with the terms of the license agreement 
 * you entered into with Aricent.
 ****************************************************************
 */
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.vodafone360.people.utils.LogUtils;

/**
 * BaseDataType encapsulating an PrivacySettingList retrieved from, or to be
 * issued to, Now + server
 */
public class PrivacySettingList extends BaseDataType {

	private Integer mPrivacyListSize = null;

	/** List-array of PrivacySettingList. */
	public List<PrivacySetting> mItemList = new ArrayList<PrivacySetting>();

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(name() + "(" + mPrivacyListSize + ")");
		for (int i = 0; i < mItemList.size(); i++) {
			ret.append("\n  " + mItemList.get(i).toString());
		}
		return ret.toString();
	}

	/**
	 * Populates data from Hashtable
	 * 
	 * @param hash
	 *            Hashtable containing populateFromHashtable data
	 * @return void
	 */
	public void populateFromHashtable(Hashtable<String, Object> hash) {

		List<PrivacySetting> privSetList = new ArrayList<PrivacySetting>();

		@SuppressWarnings("unchecked")
		String privSetString = "statusprivacysettinglist";
		Vector<Vector<Hashtable<String, Object>>> privGroupList = (Vector<Vector<Hashtable<String, Object>>>) hash
				.get(privSetString);
		privSetString = "privacysettinglist";
		if (privGroupList != null) {
			Enumeration er;
			er = privGroupList.elements();
			while (er.hasMoreElements()) {
				Object key = er.nextElement();
				privSetList = PrivacySetting
						.populatefromHashtableGroupSetting((Hashtable) key);
			}
			/*
			 * if(privSetList == null){
			 * LogUtils.logE("Cannot typecast hash to vector:privSet is null");
			 * }else{ LogUtils.logI("privSet:["+privSetList+"]"); }
			 */
		}

		mItemList = privSetList;
		this.mPrivacyListSize = mItemList.size();
	}

	@Override
	public int getType() {
		return PRIVACY_SETTING_LIST_DATATYPE;
	}

	public String name() {
		return "PrivacySetting";
	}
}
