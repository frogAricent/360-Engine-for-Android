package com.vodafone360.people.datatypes;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.vodafone360.people.utils.LogUtils;

/**
 * BaseDataType encapsulating privacy settings list. 
 */
public class PrivacySettingList extends BaseDataType{

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
	
	public void populateFromHashtable(Hashtable<String, Object> hash) {
		
		List<PrivacySetting> privSetList = new ArrayList<PrivacySetting>();
		
		@SuppressWarnings("unchecked")
		String privSetString = "statusprivacysettinglist";
		Vector<Vector<Hashtable<String,Object>>> privGroupList = (Vector<Vector<Hashtable<String,Object>>>) hash.get(privSetString);
		privSetString = "privacysettinglist";
		if(privGroupList!=null){
			Enumeration er;
			er = privGroupList.elements();
			while(er.hasMoreElements()){
				Object key = er.nextElement();
				privSetList=PrivacySetting.populatefromHashtableGroupSetting((Hashtable)key);
			}
			/*if(privSetList == null){
				LogUtils.logE("Cannot typecast hash to vector:privSet is null");
			}else{
				LogUtils.logI("privSet:["+privSetList+"]");
			}*/
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
