package net.java.sip.communicator.impl.commportal;

import net.java.sip.communicator.service.commportal.CPCos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A java representation of the Class of Service JSON object.
 * <p>
 * Note that this does not contain all the fields in the CoS, only the ones we
 * currently use
 */
class ParsedClassOfService implements CPCos
{
    private final boolean mIchAvailable;
    private final String mIchServiceLevel;

    ParsedClassOfService(String dataString) throws JSONException
    {
        // As we only request the Class of Service on its own, the data we need
        // will be the first (and only) element of the array
        JSONArray dataArray = new JSONArray(dataString);
        JSONObject data = dataArray.getJSONObject(0).getJSONObject("data");

        mIchAvailable = data.getBoolean("IchAvailable");
        mIchServiceLevel = data.getString("IchServiceLevel");
    }

    public String getIchServiceLevel()
    {
        return mIchServiceLevel;
    }

    public boolean getIchAllowed()
    {
        return mIchAvailable;
    }
}
