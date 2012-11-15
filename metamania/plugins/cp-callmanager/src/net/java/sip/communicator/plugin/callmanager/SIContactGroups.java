package net.java.sip.communicator.plugin.callmanager;

import net.java.sip.communicator.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A port of SIContactGroups from the Android Accesion Project.  Used by the
 * SimpleICM class (which itself is ported from the Android project).
 */
public class SIContactGroups
{
  private static final Logger sLog = Logger.getLogger("SIContactGroups");
  private final JSONArray mGroups;

  /**
   * Constructor for the SIContactsGroup object
   *
   * @param data The data that this object represents
   */
  public SIContactGroups(JSONObject data)
  {
    // We hang on to the whole of the Groups data
    mGroups = data.optJSONArray("Group");

    if (mGroups != null)
    {
      sLog.info("Received " + mGroups.length() + " groups.");
    }
  }

  // Returns the contacts group object for the given UniqueID, or null if not found.
  public JSONObject findGroupFromId(String id)
  {
    JSONObject group = null;

    int numGroups = mGroups.length();
    for (int i = 0; i < numGroups; i++)
    {
      group = mGroups.optJSONObject(i);
      if (group != null)
      {
        String groupId = group.optString("UniqueID");
        if (id.equals(groupId))
        {
          break;
        }
      }

      // ensure that if we drop out of the loop, we have a null return value
      group = null;
    }

    return group;
  }

  public String findGroupIdByTag(String groupTag)
  {
    String id = null;

    int numGroups = (mGroups == null) ? 0 : mGroups.length();
    for (int i = 0; id == null && i < numGroups; i++)
    {
      JSONObject group = mGroups.optJSONObject(i);
      if (group != null)
      {
        JSONArray serviceTags = group.optJSONArray("ServiceTags");
        if (serviceTags != null)
        {
          int numTags = serviceTags.length();
          for (int t = 0; t < numTags; t++)
          {
            String tag = serviceTags.optString(t);
            if (groupTag.equals(tag))
            {
              id = group.optString("UniqueID");
              break;
            }
          }
        }
      }
    }

    return id;
  }
}