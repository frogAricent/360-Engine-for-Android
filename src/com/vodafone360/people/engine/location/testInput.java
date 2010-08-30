package com.vodafone360.people.engine.location;

import android.os.Bundle;

public class testInput {
	
	public testInput()
	{
		
	}
	
	public Bundle passGetGeoCodeInput()
	{
		Bundle b1=new Bundle();
        b1.putString("countrycode", "IN-KA");
        b1.putString("cityname", "Bangalore");
        return b1;
	}
	
	public Bundle passSendLocationNudgeInput()
	{
		Bundle b1=new Bundle();
        b1.putString("latitude", "12.97622");
        b1.putString("longitude", "77.603294");
        return b1;
	}

}
