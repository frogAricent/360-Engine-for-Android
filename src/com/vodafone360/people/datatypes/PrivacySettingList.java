
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

package com.vodafone360.people.datatypes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * BaseDataType encapsulating an PrivacySettingList retrieved from, or to be
 * issued to, Now + server
 */
public class PrivacySettingList extends BaseDataType {

	private Integer mPrivacyListSize = null;

	/** List-array of PrivacySettingList. */
	public List<PrivacySetting> mItemList = new ArrayList<PrivacySetting>();

	private final String PRIVSETSTRING = "statusprivacysettinglist";
	
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
	@SuppressWarnings("unchecked")
	public void populateFromHashtable(Hashtable<String, Object> hash) {

		List<PrivacySetting> privSetList = new ArrayList<PrivacySetting>();

		Vector<Vector<Hashtable<String, Object>>> privGroupList = (Vector<Vector<Hashtable<String, Object>>>) hash
				.get(PRIVSETSTRING);

		if (privGroupList != null) {
			Enumeration er = privGroupList.elements();
			while (er.hasMoreElements()) {
				Object key = er.nextElement();
				privSetList = PrivacySetting
						.populatefromHashtableGroupSetting((Hashtable) key);
			}
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
