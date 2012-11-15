package net.java.sip.communicator.plugin.callmanager;

import java.util.Arrays;

import net.java.sip.communicator.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A copy of the Android SimpleICM.java which is itself a port of simpleicm.js.
 *
 * Provides the SimpleICM API which isolates the rest of the code from having to know
 * about the very complex internals of the ICM service indication.
 * <br>
 * Inner classes and enums define smaller parts of the structure, including
 * <ul>
 *   <li>SimpleICMPhoneNumber</li>
 *   <li>SimpleICMProfile</li>
 *   <li>SimpleICMActionType</li>
 *   <li>SimpleICMAction</li>
 *   <li>SimpleICMErrorType</li>
 * <ul>
 *
 * This Java implementation is very closely based on the JavaScript implementation contained in the
 * CommPortal Web file simpleicm.js, from which the following description has been lifted.
 * Where the javascript refers to something as being optional, in Java that means the particular field
 * may be null.
 * <pre>
 * The simple ICM configuration is represented by the following data structure
 *
 * PhoneNumber
 *   [optional] string name
 *   [optional] string number - if omitted, this represents the subscriber's own phone.
 *
 * Action
 *   enum { ring, voicemail, forward, reject, normal } type
 *   [optional] boolean screenFirst
 *   [optional] string destination (for forwarding, overrides global forwarding)
 *   [optional] boolean[] ringers
 *
 * SimpleICM
 *   [optional] enum {default, dnd, forward} profile - which overall behaviour is in use
 *                  default is asssumed if not set
 *
 *   [optional] string forward - set to the global forwarding number
 *
 *   [optional] boolean basic - set to true to use "normal" action for all calls
 *
 *   [optional] PhoneNumber[] numbers - the array of phones usable by actions
 *
 *   Action normal - action when "normal" calls are received
 *   [optional] Action vip - action when calls from VIP contacts are received
 *   [optional] Action withheld - action when calls are received from withheld numbers
 *   [optional] Action unwanted - action when calls from unwanted callers are received
 *
 *   [optional] integer ringTimeout - time that phones are rung for (in milliseconds)
 *   [optional] boolean forwardOnBusy - set to true to forward when busy (goes to voicemail if not set)
 *   [optional] boolean forwardOnNoAnswer - set to true to forward on no answer (goes to voicemail if not set)
 *
 * It is invalid to
 * - not have a normal action
 * - have a different number of values in the ringers array than in the numbers array
 * - not have at least one true value in the ringers array
 * - have a type other than the enum values
 * - specify forward for the profile, or either of forwardOnBusy/NoAnswer without
 *     having given the global forwarding number
 * - specify forward within an action without either having given a local destination
 *     or having given the global forwarding number
 *
 * - have a numeric rather than a string phone number
 * - have an invalid destination (too long, wrong characters)
 * - have an invalid phone number (too long, wrong characters)
 * - specify a profile and "DND" overrides at the same time
 *
 * - specify a type of "normal" for the normal action
 *
 * Phone numbers may consist of just digits, or the family mailbox form which
 * has digits plus a hypen and 1 to 3 further digits.
 *
 * There are no accessor methods to get or set fields in the data structure -
 * just access them directly.  Note that you can also store any additional fields
 * within the SimpleICM object that you want - they will simply be ignored by the
 * methods in the interface, including validate().
 *
 *
 * Instance methods:
 *   constructors - used to create the object from the json derived data objects
 *   fetchConfig() - converts back to a string containing JSON
 *   validate() - validates that the configuration is valid
 * </pre>
 */
public class SimpleICM
{
  private static Logger sLog = Logger.getLogger("SimpleICM");

  /**
   * A named phone number in the SimpleICM structure.
   */
  public class SimpleICMPhoneNumber
  {
    public String mName;
    public String mNumber;

    // We still want to allow the default constructor since we can
    // set the fields after construction if we want
    public SimpleICMPhoneNumber()
    {
    }

    // Provide a convenience constructor
    public SimpleICMPhoneNumber(String name,
                                String number)
    {
      mName = name;
      mNumber = number;
    }

    @Override
    public boolean equals(Object other)
    {
      boolean isEqual = false;

      if (other == null)
      {
        // By definition null input means they are not equal
        isEqual = false;
      }
      else if (this == other)
      {
        // Point to the same object, must be equal
        isEqual = true;
      }
      else
      {
        try
        {
          SimpleICMPhoneNumber pn = (SimpleICMPhoneNumber)other;

          isEqual = pn != null &&
                    equalsOrBothNull(mName, pn.mName) &&
                    equalsOrBothNull(mNumber, pn.mNumber);
        }
        catch (ClassCastException e)
        {
          // Not the same type, not the same object
        }
      }

      return isEqual;
    };
  }

  /**
   * @return true if a and b are equal, including if they are both null.
   */
  private boolean equalsOrBothNull(String a, String b)
  {
    boolean returnValue;

    if (a == null)
    {
      returnValue = (b == null);
    }
    else
    {
      returnValue = a.equals(b);
    }

    return returnValue;
  }

  /**
   * Possible values of the profile setting.
   */
  public enum SimpleICMProfile
  {
    DEFAULT,
    DND,
    FORWARD
  }

  /**
   * Possible values of the type of a SimpleICM action.
   */
  public enum SimpleICMActionType
  {
    RING,
    VOICEMAIL,
    FORWARD,
    REJECT,
    NORMAL
  }

  /**
   * An action within the SimpleICM structure
   */
  public class SimpleICMAction
  {
    public SimpleICMActionType mType;
    public boolean mScreenFirst;
    public boolean[] mRingers;
    public String mDestination; // Not normally used by ECM which only has global forwarding number

    private transient String mInternalName; // Not part of the external interface - used to identify what this action is used for

    /**
     * @return A readable description of the action
     */
    @Override
    public String toString()
    {
      StringBuilder text = new StringBuilder();

      if (mInternalName != null)
      {
        text.append(mInternalName + " ");
      }

      text.append("Type " + mType);

      if (mScreenFirst)
      {
        text.append(" screened");
      }

      if (mDestination != null)
      {
        text.append(" Forward to " + mDestination);
      }

      if (mRingers != null)
      {
        text.append(" rings ");
        for (boolean i : mRingers)
        {
          text.append(i ? "y" : "n");
        }
      }

      return text.toString();
    }
  }

  /**
   * Possible error types thrown when validating the SimpleICM structure.
   */
  enum SimpleICMErrorType
  {
    ERROR_MISSING_NORMAL,
    ERROR_REQUIRES_DESTINATION,
    ERROR_INVALID_TYPE,
    ERROR_INVALID_RINGERS,
    ERROR_MISSING_RINGER,
    ERROR_MISSING_DESTINATION
  }

  /**
   * Error class used when validating the SimpleICM structure.
   */
  @SuppressWarnings("serial")
  public class SimpleICMError extends Throwable
  {
    SimpleICMErrorType mError;
    String mHint;

    SimpleICMError(SimpleICMErrorType error, String hint)
    {
      mError = error;
      mHint = hint;
    }

    @Override
    public String toString()
    {
      return mError + " on " + mHint;
    }
  }

  // Private constants, which define the ECM conventions within the ICM service indication
  private static final String PROFILE_DND              = "ProfileECMDoNotDisturb";
  private static final String PROFILE_FORWARDING       = "ProfileECMForwardAllCalls";
  private static final String PROFILE_SIMPLE_ICM       = "ProfileECMAvailable";
  private static final String PROFILE_ADVANCED_CALLERS = "ProfileECMAdvanced";
  private static final String PROFILE_HIDDEN           = "ProfileECMHidden";

  private static final String RULESET_RING      = "RuleSetRingPhones";
  private static final String RULESET_FORWARD   = "RuleSetForward";
  private static final String RULESET_VOICEMAIL = "RuleSetVoicemail";
  private static final String RULESET_REJECT    = "RuleSetRejectCall";
  private static final String RULESET_HIDDEN    = "RuleSetHidden";

  private static final String RULE_SCREEN_CHALLENGE = "ChallengeRuleScreen";
  private static final String RULE_RING_FORWARD     = "ForwardingRuleRing";
  private static final String RULE_FORWARD          = "ForwardingRuleForward";
  private static final String RULE_FORWARD_HIDDEN   = "ForwardingRuleHidden";

  private static final String PERSONAL_DESTINATION_FORWARD = "PersonalDestinationForward";
  private static final String PERSONAL_DESTINATION_PHONES  = "PersonalDestinationRing";

  private static final String DESTINATION_GROUP = "DestinationGroup";

  private static final String CONTACT_GROUP_UNWANTED_TAG = "@EcmUnwantedCallers=true";
  private static final String CONTACT_GROUP_VIP_TAG      = "@EcmVipCallers=true";

  private final static int MAX_NUMBERS = 5;

  // Public fields
  public SimpleICMProfile mProfile;
  public String mForward;
  public SimpleICMPhoneNumber[] mNumbers;
  public SimpleICMAction mNormal;
  public SimpleICMAction mVIP;
  public SimpleICMAction mWithheld;
  public SimpleICMAction mUnwanted;

  public Integer mRingTimeout; // Note this is an Integer not int - it can be null
  public boolean mForwardOnBusy;
  public boolean mForwardOnNoAnswer;

  public boolean mBasic; // not used by ECM, but was defined by the JavaScript version of SimpleICM

  private transient SIContactGroups mContactGroups;

  /**
   * Constructor
   * @param data The data that initializes the structure - may be null
   * @param contactGroups ContactsGroups data - may also be null
   */
  public SimpleICM(JSONObject data,
                   SIContactGroups contactGroups)
  {
    // Store the contacts groups for later
    mContactGroups = contactGroups;

    // We start from the very simplest definition
    mNormal = new SimpleICMAction();
    mNormal.mType = SimpleICMActionType.RING;
    mNormal.mInternalName = "normal";

    if (data != null)
    {
      // We were passed a data object to initialize from
      if (isSimpleICM(data))
      {
        // This is a config that we can handle
        String active = getActiveProfile(data);

        sLog.debug("Active SimpleICM profile is " + active);

        if (PROFILE_DND.equals(active))
        {
          mProfile = SimpleICMProfile.DND;
        }
        else if (PROFILE_FORWARDING.equals(active))
        {
          mProfile = SimpleICMProfile.FORWARD;
        }
        else
        {
          mProfile = SimpleICMProfile.DEFAULT;
        }

        // We know that the forwarding number ends up in a personal
        // destination, so we just grab it from there
        JSONObject forward = findPersonalDestinationForId(data,
                                                          PERSONAL_DESTINATION_FORWARD);
        if (forward != null)
        {
          // We have a personal destination, but if it does not have a number
          // then that means we only wrote this to make the "write all profiles"
          // consistent
          // We always get the phone numbers in their RAW format.
          String number = getString(forward, "Number.Number._");
          if (number != null)
          {
            sLog.debug("Forwarding number is " + number);
            mForward = number;

            // The best place we have found to store the global flags relating
            // to forwarding on busy or no answer is within the presentation name
            // of the forwarding destination we just found.
            String name = getString(forward, "PresentationName._");
            mForwardOnBusy = name.charAt(0) == '1';
            mForwardOnNoAnswer = name.charAt(1) == '1';
          }
        }

        // Recreate the numbers list from relevant personal destinations
        SimpleICMPhoneNumber[] numbers = new SimpleICMPhoneNumber[MAX_NUMBERS];
        int numbersLength = 0;
        JSONObject destination;
        do
        {
          destination = findPersonalDestinationForId(data,
                                 PERSONAL_DESTINATION_PHONES + numbersLength);

          if (destination != null)
          {
            // The number is optional - if absent this means the ICM concept of "This Device"
            // (which is typically the subscriber's land line)
            String number = getString(destination, "Number.Number._");

            // The name is optional
            String name = getString(destination, "PresentationName._");

            numbers[numbersLength++] = new SimpleICMPhoneNumber(name, number);

            sLog.debug("Phone: " + name + " " + number);
          }
        } while (destination != null);
        if (numbersLength > 0)
        {
          mNumbers = new SimpleICMPhoneNumber[numbersLength];
          for (int i = 0; i < numbersLength; i++)
          {
            mNumbers[i] = numbers[i];
          }
        }

        // The caller types we handle are defined by the simple ICM profile,
        // with some caller types moved out to the advanced profiles if we
        // are in "basic" mode
        String[] profilesToCheck = { PROFILE_SIMPLE_ICM, PROFILE_ADVANCED_CALLERS };
        for (String profileToCheck : profilesToCheck)
        {
          JSONObject profile = findById(data, "Profiles", profileToCheck, null);
          if (profile != null)
          {
            sLog.debug("Processing profile " + profileToCheck);

            // See which types of call handling the profile defines.
            JSONArray ifsArray = getArray(profile, "Action.Ifs");

            // Consider each of the separate "if" actions in the profile
            int ifCount = ifsArray.length();
            for (int eachIf = 0; eachIf < ifCount; eachIf++)
            {
              JSONObject thisIf = ifsArray.optJSONObject(eachIf);

              String thisClass = getString(thisIf, "Condition._CLASS");
              String ruleset = getString(thisIf, "Action.RuleSetId._");

              SimpleICMActionType type = determineTypeFromRuleSetId(ruleset);

              SimpleICMAction which = new SimpleICMAction();
              SimpleICMAction originalWhich = null;

              if ("ICM_IsAnonymousCall".equals(thisClass))
              {
                originalWhich = mWithheld;
                mWithheld = which;
                which.mInternalName = "withheld";
              }
              else if ("ICM_InContactGroup".equals(thisClass))
              {
                // Profile is for a contact group, find out which group
                String whichGroup = getString(thisIf,
                                              "Condition.ContactGroupId._");

                JSONObject group = contactGroups.findGroupFromId(whichGroup);
                if (group != null)
                {
                  if (isVIPGroup(group))
                  {
                    originalWhich = mVIP;
                    mVIP = which;
                    which.mInternalName = "vip";
                  }
                  else if (isUnwantedCallersGroup(group))
                  {
                    originalWhich = mUnwanted;
                    mUnwanted = which;
                    which.mInternalName = "unwanted";
                  }
                }
              }
              else
              {
                mNormal = which;
                which.mInternalName = "normal";
              }

              // Before overwriting the action with "which", we saved off the original
              // value.  If that original value was non null, it means we had
              // already set up a structure for this "which", which in turn means
              // that we have two ifs for the same action type.
              // That occurs because this action was marked as being
              // "handleAsNormal", which we handle by making some adjustments
              // so that setActionDetails works with an alternate type of NORMAL
              boolean handleAsNormal = originalWhich != null;
              if (handleAsNormal)
              {
                type = SimpleICMActionType.NORMAL;
              }

              setActionDetails(data, type, ruleset, which);
            }

            if (PROFILE_ADVANCED_CALLERS.equals(profileToCheck))
            {
              // The existence of an advanced profile means that there was data that
              // we needed to squirrel off as advanced because we are actually in
              // a basic mode at the moment.
              mBasic = true;
            }
            else
            {
              // Also handle the "normal" action which is the otherwise clause
              String ruleset = getString(profile, "Action.Otherwise.RuleSetId._");
              SimpleICMActionType type = determineTypeFromRuleSetId(ruleset);
              setActionDetails(data, type, ruleset, mNormal);
            }
          }
        }

        mRingTimeout = getInt(data, "MasterNarrative.DefaultRingTimeout._");
      }
      else
      {
        // This does not look to be a SimpleICM config so we will just return
        // the default config.
      }

      // Finally, the server data may be blank if a value has not been changed
      // from the default.  In this case we explicitly set the value to be the
      // same as the default values.
      if (mNormal == null)
      {
        sLog.debug("No normal action");
        mNormal = new SimpleICMAction();
        mNormal.mType = SimpleICMActionType.RING;
        mNormal.mInternalName = "normal";
      }

      if (mWithheld == null)
      {
        sLog.debug("No with-held action");

        // Default withheld action is to ring like normal
        mWithheld = new SimpleICMAction();
        mWithheld.mType = SimpleICMActionType.NORMAL;
        mWithheld.mInternalName = "withheld";
      }

      if (mVIP == null)
      {
        sLog.debug("No VIP action");

        // Again, default is to ring like normal
        mVIP = new SimpleICMAction();
        mVIP.mType = SimpleICMActionType.RING;
        mVIP.mInternalName = "vip";
      }

      if (mUnwanted == null)
      {
        sLog.debug("No unwanted action");

        // Default to reject
        mUnwanted = new SimpleICMAction();
        mUnwanted.mType = SimpleICMActionType.REJECT;
        mUnwanted.mInternalName = "unwanted";
      }
    }
  }

  /**
   * Provide a text summary of the SimpleICM structure.
   */
  @Override
  public String toString()
  {
    StringBuilder text = new StringBuilder();

    text.append("Profile " + mProfile);
    if (mForward != null)
    {
      text.append(", Global forward number " + mForward);
    }

    text.append(", Normal: " + mNormal);
    if (mVIP != null)
    {
      text.append(", VIP: " + mVIP);
    }
    if (mUnwanted != null)
    {
      text.append(", Unwanted: " + mUnwanted);
    }
    if (mWithheld != null)
    {
      text.append(", Withheld: " + mWithheld);
    }

    if (mNumbers != null)
    {
      for (int i = 0; i < mNumbers.length; i++)
      {
        SimpleICMPhoneNumber number = mNumbers[i];
        if (number != null)
        {
          text.append(", Number " + i + ": " + number.mName + " " + number.mNumber);
        }
      }
    }

    return text.toString();
  }

  /**
   * Set the ringers array up for a particular action.
   * This is called after the mNumbers array has already been set up.
   * @param data the ICM data
   * @param which the action whose ringers need to be set up
   * @param name the name of this action
   */
  private void setRingersForAction(JSONObject data,
                                   SimpleICMAction which,
                                   String name)
  {
    // Find which numbers are to be rung
    JSONObject group = findById(data,
                                "DestinationGroups",
                                DESTINATION_GROUP + "@" + name,
                                null);

    if (group != null)
    {
      which.mRingers = new boolean[mNumbers.length];

      for (int n = 0; n < which.mRingers.length; n++)
      {
        // If we find a destination phone for this index within the group,
        // then that means it is a ringer in this action
        which.mRingers[n] =
            null != findById(group,
                             "Pattern.Pattern.Args",
                             PERSONAL_DESTINATION_PHONES + n,
                             "PersonalDestination");
      }
    }
  }

  /**
   * Search the given main object, to find the given id in the named array,
   * with an optional override for the field that holds ids
   * @param data the main object to look in
   * @param arrayName the name of the array of data to look at
   * @param id the id to search for
   * @param useField the field where the id may be found, or null to use the default
   * @return the required object, or null if not found
   */
  private JSONObject findById(JSONObject data,
                              String arrayName,
                              String id,
                              String useField)
  {
    // If no particular field was given we use the standard "Id" field
    useField = useField != null ? useField : "Id";

    JSONObject matched = null;

    JSONArray array = getArray(data, arrayName);
    if (array != null)
    {
      int count = array.length();
      for (int i = 0; i < count; i++)
      {
        matched = array.optJSONObject(i);
        if (id.equals(getString(matched, useField + "._")))
        {
          break;
        }

        // Ensure we return failure if we drop out of the bottom of the loop
        matched = null;
      }
    }

    return matched;
  }

  /**
   * Determine the action type from the ruleset
   * @param ruleset the ruleset whose type needs to be determined
   * @return the determined type
   */
  private SimpleICMActionType determineTypeFromRuleSetId(String ruleset)
  {
    SimpleICMActionType type = null;

    if (RULESET_VOICEMAIL.equals(ruleset))
    {
      type = SimpleICMActionType.VOICEMAIL;
    }
    else if (RULESET_REJECT.equals(ruleset))
    {
      type = SimpleICMActionType.REJECT;
    }
    else if (ruleset != null && ruleset.startsWith(RULESET_RING))
    {
      type = SimpleICMActionType.RING;
    }
    else if (ruleset != null && ruleset.startsWith(RULESET_FORWARD))
    {
      type = SimpleICMActionType.FORWARD;
    }

    return type;
  }

  /**
   * Set up all the fields for an action
   * @param data the action object to be adjusted
   * @param type the type for the action
   * @param ruleset the ruleset
   * @param which the action
   */
  private void setActionDetails(JSONObject data,
                                SimpleICMActionType type,
                                String ruleset,
                                SimpleICMAction which)
  {
    which.mType = type;

    String name = which.mInternalName + (type == SimpleICMActionType.NORMAL ? "-normal" : "");

    JSONObject destination = findPersonalDestinationForId(data,
                                                          PERSONAL_DESTINATION_FORWARD + "@" + name);
    if (destination != null)
    {
      which.mDestination = getString(destination, "Number.Number._");
    }

    setRingersForAction(data, which, name);

    JSONObject screeningRule = findById(data,
                                        "Rules",
                                        RULE_SCREEN_CHALLENGE + "@" + name,
                                        null);

    if (screeningRule != null)
    {
      which.mScreenFirst = true;
    }

    sLog.debug("Action " + which);
  }

  private boolean isUnwantedCallersGroup(JSONObject group)
  {
    return isSpecificGroup(group, CONTACT_GROUP_UNWANTED_TAG);
  }

  private boolean isVIPGroup(JSONObject group)
  {
    return isSpecificGroup(group, CONTACT_GROUP_VIP_TAG);
  }

  private boolean isSpecificGroup(JSONObject group, String tag)
  {
    boolean rc = false;

    if (group != null)
    {
      JSONArray serviceTags = group.optJSONArray("ServiceTags");
      if (serviceTags != null)
      {
        String serviceTag = serviceTags.optString(0);
        rc = tag.equals(serviceTag);
      }
    }

    return rc;
  }

  private JSONObject findPersonalDestinationForId(JSONObject data,
                                                  String id)
  {
    return findById(data, "PersonalDestinations", id, null);
  }

  private String getActiveProfile(JSONObject data)
  {
    String activeProfile = null;

    JSONArray ifs = getArray(data, "MasterNarrative.Action.Ifs");
    int len = ifs.length();
    for (int i = 0; i < len; i++)
    {
      JSONObject entry = ifs.optJSONObject(i);

      if (getBool(entry, "Condition.Value._"))
      {
        activeProfile = getString(entry, "Action.ProfileId._");
        break;
      }
    }

    return activeProfile;
  }

  // A number of static helper methods that allow us to dereference items in a JSONObject via
  // the dotted string notation that we would use in JavaScript.
  static Integer getInt(JSONObject data, String string)
  {
    // Work through the structure defined in the string
    data = stepThroughStructure(data, string);

    // Now get the final component as the required type
    Integer result = data == null ? null :
                                    data.optInt(string.substring(string.lastIndexOf('.') + 1));

    return result;
  }

  static String getString(JSONObject data, String string)
  {
    // Work through the structure defined in the string
    data = stepThroughStructure(data, string);

    // Now get the final component as the required type
    String result = data == null ? null :
                                   data.optString(string.substring(string.lastIndexOf('.') + 1), null);

    return result;
  }

  static JSONArray getArray(JSONObject data, String string)
  {
    // Work through the structure defined in the string
    data = stepThroughStructure(data, string);

    // Now get the final component as the required type
    JSONArray result = data == null ? null :
                                      data.optJSONArray(string.substring(string.lastIndexOf('.') + 1));

    return result;
  }

  static boolean getBool(JSONObject data, String string)
  {
    // Work through the structure defined in the string
    data = stepThroughStructure(data, string);

    // Now get the final component as the required type
    boolean result = data == null ? false :
      data.optBoolean(string.substring(string.lastIndexOf('.') + 1));

    return result;
  }

  static JSONObject stepThroughStructure(JSONObject data, String string)
  {
    String[] components = string.split("\\.");

    // Step through the components, except for the last one which is the
    // name of the field within the object
    for (int i = 0; i < components.length - 1; i++)
    {
      if (data != null)
      {
        data = data.optJSONObject(components[i]);
      }
    }

    return data;
  }

  private boolean isSimpleICM(JSONObject data)
  {
    String profile = getActiveProfile(data);

    return PROFILE_SIMPLE_ICM.equals(profile) ||
           PROFILE_DND.equals(profile) ||
           PROFILE_FORWARDING.equals(profile);
  }

  /**
   * Return the icm config as an ICM Service Indication.
   * Throws an error if the config is invalid.
   * @throws JSONException
   * @throws SimpleICMError
   */
  public String fetchConfig() throws JSONException, SimpleICMError
  {
    validate();

    // We build up the data in this JSONObject
    JSONObject icmData = new JSONObject();

    boolean isDND = mProfile == SimpleICMProfile.DND;
    boolean isForward = mProfile == SimpleICMProfile.FORWARD;

    // We have up to three functional profiles
    // - one to give us DND
    // - one to give us Forwarding
    // - one that holds the rest of the settings
    // (In addition there may be other hidden profiles to stop things being
    // pruned away as orphans).
    String useProfile = isDND ? PROFILE_DND : isForward ? PROFILE_FORWARDING : PROFILE_SIMPLE_ICM;

    // The MasterNarrative, laid out in string form
    String master =
    "{" +
      "\"Action\":{" +
      "\"_CLASS\":\"ICM_Choose\"," +
      "\"Ifs\":[" +
        "{\"Condition\":{\"Value\":{\"_\":false},\"_CLASS\":\"ICM_Value\"}," +
         "\"Action\":{\"_CLASS\":\"ICM_LoadProfile\"," +
         "\"ProfileId\":{\"_\":\"ProfileRingYourPhone\"}},\"_CLASS\":\"ICM_ICMIf\"}," +
        "{\"Condition\":{\"Value\":{\"_\":false},\"_CLASS\":\"ICM_Value\"}," +
         "\"Action\":{\"_CLASS\":\"ICM_LoadProfile\"," +
          "\"ProfileId\":{\"_\":\"ProfileForward\"}},\"_CLASS\":\"ICM_ICMIf\"}," +
        "{\"Condition\":{\"Value\":{\"_\":true},\"_CLASS\":\"ICM_Value\"}," +
         "\"Action\":{\"_CLASS\":\"ICM_LoadProfile\"," +
         "\"ProfileId\":{\"_\":\"" + useProfile + "\"}},\"_CLASS\":\"ICM_ICMIf\"}]," +
      "\"Otherwise\":{" +
        "\"_CLASS\":\"ICM_Choose\"," +
        "\"Ifs\":[{" +
          "\"Condition\":{" +
            "\"ScheduleId\":{\"_\":\"schedulesSpecialDays\"}," +
            "\"_CLASS\":\"ICM_InSchedule\"}," +
          "\"Action\":{" +
            "\"_CLASS\":\"ICM_LoadProfile\"," +
            "\"ProfileId\":{\"_\":\"ProfileNormal\"}}," +
          "\"_CLASS\":\"ICM_ICMIf\"}]," +
        "\"Otherwise\":{\"_CLASS\":\"ICM_LoadProfile\"," +
        "\"ProfileId\":{\"_\":\"ProfileNormal\"}}}}," +
      "\"_CLASS\":\"ICM_MasterNarrative\"," +
      "\"Id\":{\"_\":\"\"}" +
    "}";

    // Convert it to a JSONObject
    JSONObject masterNarrative = objectFromString(master);

    // Place it in the data we are accumulating
    icmData.put("MasterNarrative", masterNarrative);

    if (mRingTimeout != null)
    {
      JSONObject ringTimeout = new JSONObject();
      ringTimeout.put("_", mRingTimeout);
      masterNarrative.put("DefaultRingTimeout", ringTimeout);
    }

    sLog.debug("MasterNarrative " + masterNarrative);

    // We accumulate all data other than the MasterNarrative as additionalData which we merge across later.
    // This is done to more closely follow the javascript SimpleICM algorithm.
    String icmAdditionalData =
    "{" +
      "\"Profiles\":[" +
        "{\"Action\":{" +
            "\"_CLASS\":\"ICM_LoadRuleSet\"," +
            "\"RuleSetId\":{\"_\":\"RuleSetForward\"}}," +
          "\"_CLASS\":\"ICM_Profile\"," +
          "\"Id\":{\"_\":\"ProfileForward\"}," +
          "\"PresentationName\":{\"_\":\"Forward all calls\"}}," +
        "{\"Action\":{" +
            "\"_CLASS\":\"ICM_Choose\"," +
            "\"Ifs\":[]," +
            "\"Otherwise\":{\"_CLASS\":\"ICM_LoadRuleSet\"," +
              "\"RuleSetId\":{\"_\":\"RuleSetNormal\"}}}," +
          "\"_CLASS\":\"ICM_Profile\"," +
          "\"SpeedNumber\":{\"_\":\"2\"}," +
          "\"Id\":{\"_\":\"ProfileNormal\"}," +
          "\"PresentationName\":{\"_\":\"Normal\"}}," +
        "{\"Action\":{" +
            "\"_CLASS\":\"ICM_LoadRuleSet\"," +
            "\"RuleSetId\":{\"_\":\"RuleSetRingYourPhone\"}}," +
          "\"_CLASS\":\"ICM_Profile\",\"SpeedNumber\":{\"_\":\"1\"}," +
          "\"Id\":{\"_\":\"ProfileRingYourPhone\"}," +
          "\"PresentationName\":{\"_\":\"Ring your phone\"}}," +
        "{\"Action\":{" +
            "\"_CLASS\":\"ICM_Choose\"," +
            "\"Ifs\":[ ], " + // filled in below
            "\"Otherwise\":{" +
              "\"_CLASS\":\"ICM_LoadRuleSet\"," +
              "\"RuleSetId\":{ \"_\" : \"\" }}}," + // filled in below
          "\"_CLASS\":\"ICM_Profile\"," +
          "\"Id\":{\"_\":\"" + PROFILE_SIMPLE_ICM + "\"}," +
          "\"PresentationName\":{\"_\": \"ECM Available\"}}" +
      "]," +

      "\"PersonalDestinations\":[{" +
        "\"Action\":{" +
          "\"Args\":[{" +
            "\"DistinctiveRing\":{" +
              "\"Value\":{\"_\":\"STANDARD\"}," +
              "\"_CLASS\":\"ICM_DistinctiveRing\"}," +
            "\"_CLASS\":\"ICM_SetRingType\"}," +
            "{\"_CLASS\":\"ICM_SetNoCallScreening\"}]," +
          "\"_CLASS\":\"ICM_ICMDo\"}," +
        "\"_CLASS\":\"ICM_PersonalDestination\"," +
        "\"Number\":{\"_CLASS\":\"ICM_ThisDevice\"}," +
        "\"Id\":{\"_\":\"PersonalDestinationDefault\"}," +
        "\"PresentationName\":{\"_\":\"My Phone\"}" +
      "}]," +

      "\"RuleSets\" : [" +
        "{\"_CLASS\":\"ICM_RuleSet\"," +
          "\"Id\":{\"_\":\"RuleSetNormal\"}," +
          "\"Rules\":[" +
            "{\"_\":\"RingbackRuleDefault\"}," +
            "{\"_\":\"RejectionRuleDefault\"}," +
            "{\"_\":\"ForwardingRuleDefault\"}," +
            "{\"_\":\"ChallengeRuleDefault\"}]" +
        "}," +
        "{\"_CLASS\":\"ICM_RuleSet\"," +
          "\"Id\":{\"_\":\"RuleSetForward\"}," +
          "\"Rules\":[{\"_\":\"RingbackRuleDefault\"}," +
            "{\"_\":\"RejectionRuleDefault\"}," +
            "{\"_\":\"ForwardingRuleForward\"}," +
            "{\"_\":\"ChallengeRuleDefault\"}]" +
        "}," +
        "{\"_CLASS\":\"ICM_RuleSet\"," +
          "\"Id\":{\"_\":\"RuleSetRingYourPhone\"}," +
          "\"Rules\":[" +
            "{\"_\":\"RingbackRuleDefault\"}," +
            "{\"_\":\"RejectionRuleDefault\"}," +
            "{\"_\":\"ForwardingRuleDefault\"}," +
            "{\"_\":\"ChallengeRuleDefault\"}]" +
        "}" +
      "]," +

      "\"Rules\":[" +
        "{\"Action\":{\"_CLASS\":\"ICM_RingbackDefault\"}," +
          "\"_CLASS\":\"ICM_RingbackRule\"," +
          "\"Id\":{\"_\":\"RingbackRuleDefault\"}}," +
        "{\"Action\":{\"_CLASS\":\"ICM_Allow\"}," +
          "\"_CLASS\":\"ICM_RejectionRule\"," +
          "\"Id\":{\"_\":\"RejectionRuleDefault\"}}," +
        "{\"Action\":{\"_CLASS\":\"ICM_Permit\"}," +
          "\"_CLASS\":\"ICM_ChallengeRule\"," +
          "\"Id\":{\"_\":\"ChallengeRuleDefault\"}}," +
        "{\"Action\":{\"RingTimeout\":{\"_\":0}," +
          "\"_CLASS\":\"ICM_Destination\"," +
          "\"VoicemailOnBusy\":{\"_\":true}," +
          "\"VoicemailOnNoAnswer\":{\"_\":true}," +
          "\"PersonalDestination\":{\"_\":\"PersonalDestinationDefault\"}," +
          "\"ForwardingPermitted\":{\"_\":false}}," +
          "\"_CLASS\":\"ICM_ForwardingRule\"," +
          "\"Id\":{\"_\":\"ForwardingRuleDefault\"}}," +
        "{\"Action\":{\"_CLASS\":\"ICM_Voicemail\"}," +
          "\"_CLASS\":\"ICM_ForwardingRule\"," +
          "\"Id\":{\"_\":\"ForwardingRuleForward\"}}" +
      "]" +
    "};";

    JSONObject additionalData = objectFromString(icmAdditionalData);

    // Get some references to the parts of the additionalData that we need to fix up
    JSONArray profiles = additionalData.optJSONArray("Profiles");
    JSONObject simpleICMProfile = findById(additionalData, "Profiles", PROFILE_SIMPLE_ICM, null);
    JSONObject action = simpleICMProfile.optJSONObject("Action");
    JSONArray ifs = action.optJSONArray("Ifs");

    // First we check for VIP callers
    // Note that the way the ICM engine works is that even though a number was
    // withheld, this is purely presentational - the number is still used
    // to decide if the call was a member of the VIP or unwanted groups.
    if (mVIP != null)
    {
      addIf(ifs, mVIP, "vip", additionalData, SimpleICM.CONTACT_GROUP_VIP_TAG);
    }

    // Then we check for unwanted callers - again this check ignores whether
    // the number was "withheld" or not
    if (mUnwanted != null)
    {
      addIf(ifs, mUnwanted, "unwanted", additionalData, SimpleICM.CONTACT_GROUP_UNWANTED_TAG);
    }

    // Next we check withheld
    if (mWithheld != null)
    {
      addIf(ifs, mWithheld, "withheld", additionalData, null);
    }

    // The normal action is the "otherwise" clause of the if
    String actionRuleSet = determineActionRuleSet(mNormal, "normal", additionalData);
    action.optJSONObject("Otherwise").optJSONObject("RuleSetId").put("_", actionRuleSet);

    // We add the numbers as personal destinations
    if (this.mNumbers != null)
    {
      JSONArray personalDestinations = additionalData.optJSONArray("PersonalDestinations");

      int countNum = 0;
      for (SimpleICMPhoneNumber number : this.mNumbers)
      {
        if (number != null)
        {
          JSONObject pd = formatPersonalDestination(PERSONAL_DESTINATION_PHONES + countNum++,
                                                    number.mNumber,
                                                    number.mName);

          personalDestinations.put(pd);

          sLog.debug("Personal destination " + number.mName + " " + number.mNumber);
        }
      }

      // We need to add all the numbers as a destination group that we can add to
      // a hidden profile, so that even if nothing uses them they are kept
      boolean[] allRingers = new boolean[countNum];
      Arrays.fill(allRingers, true);

      JSONObject allNumbersGroup = innerAddRingers("hidden", allRingers, additionalData);
      addToHiddenProfile(allNumbersGroup, "numbers", additionalData);
    }

    // We loop twice - once for DND and once for Forward
    for (int pass = 0; pass < 2; pass++)
    {
      boolean dnd = pass == 0;

      // DND and Forward are pretty similar - they both send non-rejected calls
      // to a particular destination
      String profileAsString =
      "{" +
        "\"_CLASS\":\"ICM_Profile\"," +
        "\"PresentationName\":{\"_\": \"" + (dnd ? "ECM Do Not Disturb" : "ECM Forward All Calls") + "\"}," +
        "\"Id\":{\"_\" : \"" + (dnd ? PROFILE_DND : PROFILE_FORWARDING) + "\"}," +
        "\"Action\":{" +
          "\"_CLASS\":\"ICM_Choose\"," +
          "\"Ifs\":[]," +  // Filled in below
          "\"Otherwise\":{" +
            "\"_CLASS\":\"ICM_LoadRuleSet\"," +
            "\"RuleSetId\":{ \"_\" : \"" + (dnd ? RULESET_VOICEMAIL : (RULESET_FORWARD + "@forward")) + "\"}}}" +
      "}";

      JSONObject profile = objectFromString(profileAsString);

      profiles.put(profile);

      if (dnd)
      {
        addVoicemailRuleSet(additionalData);
      }
      else
      {
        addForwardingRuleSet(RULESET_FORWARD + "@forward",
                             RULE_FORWARD + "@forward",
                             "forward",
                             PERSONAL_DESTINATION_FORWARD,
                             additionalData);
      }

      // Do not disturb and forwarding send all non-rejected calls to
      // respectively voicemail or the given number.
      // Sending on in this way is done by the "otherwise" clause we have
      // already set - we just need to fill in any "ifs" that are needed to
      // perform rejections.
      JSONArray rejectIfs = profile.optJSONObject("Action").optJSONArray("Ifs");
      for (int i = 0; i < ifs.length(); i++)
      {
        JSONObject item = ifs.optJSONObject(i);
        if (RULESET_REJECT.equals(getString(item, "Action.RuleSetId._")))
        {
          rejectIfs.put(item);
        }
      }
    }

    // Yes, this condition is redundant, since it is always "true" but is kept
    // to match the javascript logic.
    if (true /* writeAllProfiles || mForward != null */)
    {
      // We have a global forwarding number given.
      // Set up the personal destination that stores the phone number we
      // forward to.
      //
      // The name of this does not really matter, so we use it as a convenient
      // place to store the two flags we need for forwardOnBusy and
      // forwardOnNoAnswer
      //
      // We can distinguish between when a forward was really wanted, and when
      // we were just including one to satisfy the write all profiles because
      // if this.forward is not set, we end up with a numberless destination.
      //
      // The default server config gives a name of "00", so we make sure that
      // whenever we write this data our name is longer than that, simply so
      // that we can determine when the value is no longer the default value.
      // (see hasSimpleICMBeenConfiguredYet).
      String name = (mForwardOnBusy ? "1" : "0") +
                    (mForwardOnNoAnswer ? "1" : "0") +
                    "OK";
      JSONObject pd = formatPersonalDestination(PERSONAL_DESTINATION_FORWARD,
                                                mForward,
                                                name);

      JSONArray personalDestinations = additionalData.optJSONArray("PersonalDestinations");

      personalDestinations.put(pd);

      sLog.debug("Forwarding number " + mForward);

      addToHiddenProfile(pd, "forward", additionalData);
    }

    // The Java version of SimpleICM does not handle the "basic" flag which is never used in ECM.
    // However the Javascript code includes this - even though it is never exercised.
    // The Javascript code is retained below as a comment merely so that it is easier to compare the
    // two implementations, and it is clear that omitting this is a deliberate decision.
    //
    // -- from the simpleicm.js file
    //
    //    if (this.basic)
    //    {
    //      // We are only using the normal data at the moment, so we move any
    //      // other handling off in to a hidden profile
    //      var mainProfile = icmAdditionalData.Profiles[0];
    //      var copiedProfile = Utils.clone(mainProfile, true);
    //
    //      copiedProfile.PresentationName._ = "@advanced@";
    //      copiedProfile.Id._ = PROFILE_ADVANCED_CALLERS;
    //
    //      mainProfile.Action.Ifs = [];
    //
    //      icmAdditionalData.Profiles.push(copiedProfile);
    //    }
    //
    // -- end of include JavaScript

    // Merge the additional data in with the main data
    String[] copyThese = { "PersonalDestinations", "Rules", "RuleSets", "Profiles", "DestinationGroups" };
    for (String copyThis : copyThese)
    {
      JSONArray source = additionalData.optJSONArray(copyThis);
      if (source != null)
      {
        icmData.put(copyThis, source);
      }
    }

    sLog.debug("Returning " + icmData);

    return icmData.toString();
  }

  /**
   * A helper function to take a string containing json, and get it as a JSONObject.
   * @param data The json text
   * @return The JSONObject - will be null on an error
   */
  private JSONObject objectFromString(String data)
  {
    JSONObject rc = null;
    try
    {
      rc = new JSONObject(data);
    }
    catch (JSONException e)
    {
      sLog.warn("Error in JSON " + data);
    }

    return rc;
  }

  /**
   * Create a PersonalDestination from the given parameters
   * @param id
   * @param number
   * @param name
   * @return
   */
  private JSONObject formatPersonalDestination(String id,
                                               String number,
                                               String name)
  {
    // No number means this is our subscriber's device
    String numberClass = "ICM_ThisDevice";
    String numberNumber = "";
    if (number != null)
    {
      // A number was provided - which means we use it
      numberClass = "ICM_TelephoneNumber";
      numberNumber = ",\"Number\":{\"_\":\"" + number + "\"}";
    }

    String data =
    "{" +
      (name != null ? ("\"PresentationName\":{ _ : \"" + name + "\"},") : "") +
      "\"_CLASS\":\"ICM_PersonalDestination\"," +
      "\"Id\":{\"_\":\"" + id + "\"}," +
      "\"Action\":{" +
        "\"_CLASS\":\"ICM_ICMDo\"," +
        "\"Id\":{\"_\":\"$Do" + id + "\"}," +
        "\"Args\":[{" +
          "\"_CLASS\":\"ICM_SetNoCallScreening\"," +
          "\"Id\":{\"_\":\"$SetNoCallScreening" + id + "\"}}]}," +
      "\"Number\":{" +
        "\"Id\":{\"_\":\"$TelephoneNumber" + id + "\"}," +
        "\"_CLASS\":\"" + numberClass + "\"" +
        numberNumber +
        "}" +
    "}";

    return objectFromString(data);
  }

  /**
   * Adds an If entry to the additionalData
   * @param ifs
   * @param action
   * @param which
   * @param additionalData
   * @param group
   */
  private void addIf(JSONArray ifs, SimpleICMAction action, String which, JSONObject additionalData, String group)
  {
    String actionRuleSet = determineActionRuleSet(action, which, additionalData);

    String condition;
    if (group != null)
    {
      // Find the id of the group with the given tag
      String id = mContactGroups.findGroupIdByTag(group);

      // It's a group check, so set the condition appropriately
      condition =
      "{" +
        "\"_CLASS\":\"ICM_InContactGroup\"," +
        "\"ContactGroupId\":{\"_\":\"" + id + "\"}" +
      "}";
    }
    else
    {
      // Must be a check for withheld numbers
      condition = "{\"_CLASS\":\"ICM_IsAnonymousCall\"}";
    }

    String data =
    "{" +
      "\"_CLASS\":\"ICM_ICMIf\"," +
      "\"Action\": {" +
        "\"_CLASS\":\"ICM_LoadRuleSet\"," +
        "\"RuleSetId\":{\"_\":\"" + actionRuleSet + "\"}}," +
      "\"Condition\":" + condition +
    "}";

    ifs.put(objectFromString(data));

    // If the type was normal, we have stored a combined setting, which is
    // what we need the ICM engine to use, but we also need to store a
    // second set to ensure we are storing the data in a lossless form.
    //
    // Because we store two ifs with the same condition, we will always
    // trigger on just the first of these - or on neither - this second
    // rule is effectively inert and just used to store the data we need to
    // retain.
    if (action.mType == SimpleICMActionType.NORMAL)
    {
      String replacementWhich = which + "-normal";

      action.mType = SimpleICMActionType.VOICEMAIL;
      addIf(ifs, action, replacementWhich, additionalData, group);
      action.mType = SimpleICMActionType.NORMAL;
    }
  }

  /**
   * Construct the other data structures associated with an Action RuleSet
   * @param action the action itself
   * @param which name of the action
   * @param additionalData where data will be written
   * @return the name of the RuleSet for the given action
   */
  private String determineActionRuleSet(SimpleICMAction action,
                                        String which,
                                        JSONObject additionalData)
  {
    SimpleICMActionType type = action.mType;

    if (type == SimpleICMActionType.NORMAL)
    {
      // We are handling this like the normal type
      SimpleICMAction combined = new SimpleICMAction();

      // First copy everything from normal
      combined.mDestination = mNormal.mDestination;
      combined.mInternalName = mNormal.mInternalName;
      combined.mScreenFirst = mNormal.mScreenFirst;
      combined.mRingers = mNormal.mRingers;
      combined.mType = mNormal.mType;

      // Then overlay it with the specifics, except for the type field
      if (action.mDestination != null)
      {
        combined.mDestination = action.mDestination;
      }
      if (action.mInternalName != null)
      {
        combined.mInternalName = action.mInternalName;
      }
      combined.mScreenFirst = action.mScreenFirst;
      if (action.mRingers != null)
      {
        combined.mRingers = action.mRingers;
      }

      // Now make sure the rest of the code uses the resultant values
      action = combined;
      type = combined.mType;
    }

    String destination = action.mDestination;
    String personalDestination = PERSONAL_DESTINATION_FORWARD + "@" + which;

    // Define the destination group that rings the required phones
    // (which may be undefined if there are no phones to ring)
    JSONObject group = addRingers(which, action, additionalData);

    boolean isScreening = action.mScreenFirst;

    // The challenge rule controls whether we screen these calls or not
    String challengeRule = isScreening ? (RULE_SCREEN_CHALLENGE + "@" + which) :
                                         "ChallengeRuleDefault";

    String forwardingRule;
    String actionRuleSet;

    // If we are screening, then we also need a screening rule
    if (isScreening)
    {
      String rule =
      "{" +
        "\"_CLASS\":\"ICM_ChallengeRule\"," +
        "\"Id\":{\"_\":\"" + challengeRule + "\"}," +
        "\"Action\":{\"_CLASS\":\"ICM_AnnouncedCaller\"}" +
      "}";

      JSONObject ruleObject = addToArray(additionalData, "Rules", rule);

      if (type != SimpleICMActionType.RING)
      {
        // The screening is "hidden" so we need to make sure we add this to
        // a hidden profile so it is not orphaned
        addToHiddenProfile(ruleObject, which + "1", additionalData);
      }
    }

    if (group != null && type != SimpleICMActionType.RING)
    {
      // The simring group is hidden
      addToHiddenProfile(group, which + "2", additionalData);
    }

    if (type == SimpleICMActionType.RING)
    {
      // We need a separate version of this ruleSet for each caller type
      actionRuleSet = RULESET_RING + "@" + which;

      if (group == null)
      {
        // There are no phones to simring, so just use the default
        forwardingRule = "ForwardingRuleDefault";
      }
      else
      {
        forwardingRule = RULE_RING_FORWARD + "@" + which;

        // Define the rule that the ruleset references, and make it ring the
        // destination group of phones
        String rule =
          "{" +
            "\"_CLASS\":\"ICM_ForwardingRule\"," +
            "\"Id\":{\"_\":\"" + forwardingRule + "\"}," +
            "\"Action\":{\"_CLASS\":\"ICM_Group\"," +
              "\"Id\":{\"_\":\"$Group" + "@" + which + "\"}," +
              "\"DestinationGroup\":{\"_\":\"" + getString(group, "Id._") + "\"}}" +
            "}";

        addToArray(additionalData, "Rules", rule);
      }

      // Define that ruleSet
      String ruleset =
        "{" +
          "\"_CLASS\":\"ICM_RuleSet\"," +
          "\"Rules\":[{\"_\":\"RingbackRuleDefault\"}," +
                     "{\"_\":\"RejectionRuleDefault\"}," +
                     "{\"_\":\"" + forwardingRule + "\"}," +
                     "{\"_\":\"" + challengeRule + "\"}]," +
          "\"Id\":{\"_\":\"" + actionRuleSet + "\"}" +
        "}";

      addToArray(additionalData, "RuleSets", ruleset);
    }
    else if (type == SimpleICMActionType.FORWARD)
    {
      // We need a separate version of this ruleset for each caller type
      actionRuleSet = RULESET_FORWARD + "@" + which;
      forwardingRule = RULE_FORWARD + "@" + which;

      String forwardDestination = destination != null ? personalDestination :
                                                        PERSONAL_DESTINATION_FORWARD;

      addForwardingRuleSet(actionRuleSet,
                           forwardingRule,
                           which,
                           forwardDestination,
                           additionalData);
    }
    else if (type == SimpleICMActionType.VOICEMAIL)
    {
      // This ruleset is the same for all caller types
      actionRuleSet = RULESET_VOICEMAIL;

      addVoicemailRuleSet(additionalData);
    }
    else if (type == SimpleICMActionType.REJECT)
    {
      // This rule is the same for all caller types
      actionRuleSet = RULESET_REJECT;

      // Add the ruleset and rule if they are not already present
      if (!isRulesetPresent(RULESET_REJECT, additionalData))
      {
        String ruleset =
        "{" +
          "\"_CLASS\":\"ICM_RuleSet\"," +
          "\"Rules\":[{\"_\":\"RingbackRuleDefault\"}," +
                     "{\"_\":\"RejectionRuleReject\"}," +
                     "{\"_\":\"ForwardingRuleDefault\"}," +
                     "{\"_\":\"ChallengeRuleDefault\"}]," +
          "\"Id\":{\"_\":\"" + RULESET_REJECT + "\"}" +
        "}";

        addToArray(additionalData, "RuleSets", ruleset);

        String rule =
        "{" +
          "\"Action\":{\"_CLASS\":\"ICM_RejectAnnouncement\"}," +
          "\"_CLASS\":\"ICM_RejectionRule\"," +
          "\"Id\":{\"_\":\"RejectionRuleReject\"}" +
        "}";

        addToArray(additionalData, "Rules", rule);
      }
    }
    else
    {
      sLog.debug("Unexpected type " + type);
      actionRuleSet = null;
    }

    if (destination != null)
    {
      // Set up the personal destination that stores the phone number we forward to
      JSONObject pd = formatPersonalDestination(personalDestination,
                                                destination,
                                                null);

      JSONArray personalDestinations = additionalData.optJSONArray("PersonalDestinations");
      personalDestinations.put(pd);

      if (type != SimpleICMActionType.FORWARD)
      {
        addToHiddenProfile(pd, which, additionalData);
      }
    }

    return actionRuleSet;
  }

  private JSONObject addToArray(JSONObject additionalData, String where, String what)
  {
    JSONObject ruleObject = objectFromString(what);
    JSONArray array = additionalData.optJSONArray(where);
    array.put(ruleObject);

    sLog.debug(where + " are now " + array);

    return ruleObject;
  }

  /**
   * Adds things to the hidden profile.
   * A hidden profile is needed because there are bits of configuration that are not active
   * but which still need to be stored - such as the forwarding number setting when
   * Do not Disturb is the current profile.
   * @param item
   * @param which
   * @param additionalData
   */
  private void addToHiddenProfile(JSONObject item,
                                  String which,
                                  JSONObject additionalData)
  {
    sLog.debug("Adding " + which + " with hidden data " + item);

    String id = getString(item, "Id._");
    String ruleSetId = RULESET_HIDDEN + "@" + id;

    String itemClass = item.optString("_CLASS");

    String challengeRule;
    String forwardingRule;
    if ("ICM_ChallengeRule".equals(itemClass))
    {
      challengeRule = id;
      forwardingRule = "ForwardingRuleDefault";
    }
    else
    {
      challengeRule = "ChallengeRuleDefault";
      forwardingRule = RULE_FORWARD_HIDDEN + "@" + which;

      // We need to create a forwarding rule that references the given item
      String action;
      if ("ICM_DestinationGroup".equals(itemClass))
      {
        // We need to link to a group
        action =
          "{" +
            "\"_CLASS\":\"ICM_Group\"," +
            "\"Id\":{\"_\":\"$GroupHidden" + "@" + which + "\"}," +
            "\"DestinationGroup\":{\"_\":\"" + id + "\"}" +
          "}";
      }
      else
      {
        // We need to link to a personal destination
        action =
          "{" +
            "\"_CLASS\":\"ICM_Destination\"," +
            "\"Id\":{\"_\":\"$DestinationHidden" + "@" + which + "\"}," +
            //"VoicemailOnBusy":{"_":false},
            //"VoicemailOnNoAnswer":{"_":false},
            //"ForwardingPermitted":{"_":true},
            "\"PersonalDestination\":{\"_\":\"" + id + "\"}" +
          "}";
      }

      String rule =
        "{" +
          "\"_CLASS\":\"ICM_ForwardingRule\"," +
          "\"Id\":{\"_\":\"" + forwardingRule + "\"}," +
          "\"Action\":" + action +
         "}";

      addToArray(additionalData, "Rules", rule);
    }

    // Create a profile, which will reference a ruleset
    // You might think that this could be just a simple ICM_LoadRuleSet, but
    // the existing code assumes that all user provided profiles are
    // implemented as ICM_Choose
    String profile =
    "{" +
      "\"_CLASS\":\"ICM_Profile\"," +
      "\"PresentationName\":{\"_\": \"ECM hidden:" + which + "\"}," +
      "\"Id\":{\"_\":\"" + PROFILE_HIDDEN + "" + "@" + id + "\"}," +
      "\"Action\":{" +
        "\"_CLASS\":\"ICM_Choose\"," +
        "\"Ifs\":[]," +
        "\"Otherwise\":{" +
          "\"_CLASS\":\"ICM_LoadRuleSet\"," +
          "\"RuleSetId\":{ \"_\" : \"" + ruleSetId + "\"}}}" +
    "}";

    addToArray(additionalData, "Profiles", profile);

    // Create the RuleSet that that references the rules
    String ruleSet =
    "{" +
      "\"_CLASS\":\"ICM_RuleSet\"," +
      "\"Rules\":[{\"_\":\"RingbackRuleDefault\"}," +
                 "{\"_\":\"RejectionRuleDefault\"}," +
                 "{\"_\":\"" + forwardingRule + "\"}," +
                 "{\"_\":\"" + challengeRule + "\"}]," +
      "\"Id\":{\"_\":\"" + ruleSetId + "\"}" +
    "}";

    addToArray(additionalData, "RuleSets", ruleSet);
  }

  private JSONObject addRingers(String which, SimpleICMAction action, JSONObject additionalData)
  {
    // Add any ringers as a destination group
    boolean[] ringers = action.mRingers;

    JSONObject ringersGroup = (ringers == null) ? null :
                                                  innerAddRingers(which, ringers, additionalData);

    return ringersGroup;
  }

  private JSONObject innerAddRingers(String which, boolean[] ringers, JSONObject additionalData)
  {
    String groupId = DESTINATION_GROUP + "@" + which;

    // Find which are the destinations for OnBusy/OnNoAnswer
    String onBusyEntry = ringerForward(mForwardOnBusy, "OnBusy", which);
    String onNoAnswerEntry = ringerForward(mForwardOnNoAnswer, "OnNoAnswer", which);

    String ringersGroup =
    "{" +
      "\"_CLASS\":\"ICM_DestinationGroup\"," +
      "\"Pattern\":{" +
        "\"_CLASS\":\"ICM_Pattern\"," +
        "\"Pattern\":{" +
          "\"Args\":[]," +  // filled in below
          "\"_CLASS\":\"ICM_Par\"" +
        "}," +
        "\"CommitType\":{\"Value\":{\"_\":\"explicit\"}," +
          "\"_CLASS\":\"ICM_DestinationCommitType\"" +
        "}" +
      "}," +
      "\"Id\":{\"_\":\"" + groupId + "\"}," +
      "\"OnBusy\":" + onBusyEntry + "," +
      "\"OnNoAnswer\":" + onNoAnswerEntry +
    "}";

    JSONObject ringersObject = objectFromString(ringersGroup);

    JSONArray args = ringersObject.optJSONObject("Pattern").optJSONObject("Pattern").optJSONArray("Args");
    for (int i = 0; i < ringers.length; i++)
    {
      if (ringers[i])
      {
        // This ringer is enabled, so add it to the ring pattern
        // @@@ 30000 should not be hardcoded!
        String patternItem =
        "{" +
          "\"_CLASS\":\"ICM_ICMTry\"," +
          "\"Duration\":{\"_\" : " + (mRingTimeout != null ? mRingTimeout : 30000) + "}," +
          "\"StartDelay\":{\"_\":0}," +
          "\"PersonalDestination\":{\"_\":\"" + PERSONAL_DESTINATION_PHONES + i + "\"}," +
          "\"ForwardingPermitted\":{\"_\":false}" +
        "}";

        args.put(objectFromString(patternItem));
      }
    }

    JSONArray destinationGroups = additionalData.optJSONArray("DestinationGroups");
    if (destinationGroups == null)
    {
      destinationGroups = new JSONArray();
      try
      {
        additionalData.put("DestinationGroups", destinationGroups);
      }
      catch (JSONException e)
      {
      }
    }
    destinationGroups.put(ringersObject);

    return ringersObject;
  }

  private String ringerForward(boolean required, String id, String which)
  {
    // The default is to go to voicemail
    String rc = "{\"_CLASS\":\"ICM_Voicemail\"}";

    if (required)
    {
      // busy/noAnswer forwarding uses the global forwarding destination
      String personalDestination = PERSONAL_DESTINATION_FORWARD;

      rc =
      "{" +
        "\"_CLASS\":\"ICM_Destination\"," +
        "\"Id\":{\"_\":\"$Destination" + id + "@" + which + "\"}," +
        "\"VoicemailOnBusy\":{\"_\":false}," +
        "\"VoicemailOnNoAnswer\":{\"_\":false}," +
        "\"ForwardingPermitted\":{\"_\":false}," +
        "\"PersonalDestination\":{\"_\":\"" + personalDestination + "\"}" +
      "}";
    }

    return rc;
  }

  private void addVoicemailRuleSet(JSONObject additionalData)
  {
    // Add the ruleset and rule if they are not already present
    if (!isRulesetPresent(RULESET_VOICEMAIL, additionalData))
    {
      String ruleset =
      "{" +
        "\"_CLASS\":\"ICM_RuleSet\"," +
        "\"Rules\":[{\"_\":\"RingbackRuleDefault\"}," +
                   "{\"_\":\"RejectionRuleDefault\"}," +
                   "{\"_\":\"ForwardingRuleVoicemail\"}," +
                   "{\"_\":\"ChallengeRuleDefault\"}]," +
        "\"Id\":{\"_\":\"" + RULESET_VOICEMAIL + "\"}" +
      "}";

      addToArray(additionalData, "RuleSets", ruleset);

      String rule =
      "{" +
        "\"Action\":{\"_CLASS\":\"ICM_Voicemail\"}," +
        "\"_CLASS\":\"ICM_ForwardingRule\"," +
        "\"Id\":{\"_\":\"ForwardingRuleVoicemail\"}" +
      "}";

      addToArray(additionalData, "Rules", rule);
    }
  }

  /**
   * Adds a forwarding rule to the structure
   * @param actionRuleSet
   * @param forwardingRule
   * @param which
   * @param personalDestination
   * @param additionalData
   */
  private void addForwardingRuleSet(String actionRuleSet,
                                    String forwardingRule,
                                    String which,
                                    String personalDestination,
                                    JSONObject additionalData)
  {
    // Define that ruleSet
    String ruleset =
    "{" +
      "\"_CLASS\":\"ICM_RuleSet\"," +
      "\"Rules\":[{\"_\":\"RingbackRuleDefault\"}," +
                 "{\"_\":\"RejectionRuleDefault\"}," +
                 "{\"_\":\"" + forwardingRule + "\"}," +
                 "{\"_\":\"ChallengeRuleDefault\"}]," +
      "\"Id\":{\"_\":\"" + actionRuleSet + "\"}" +
    "}";

    addToArray(additionalData, "RuleSets", ruleset);

    // Define the rule that the ruleset references, and make it
    // forward to a personal destination
    String rule =
    "{" +
      "\"Action\":{\"_CLASS\":\"ICM_Destination\"," +
        "\"Id\":{\"_\":\"$Destination" + "@" + which + "\"}," +
        "\"VoicemailOnBusy\":{\"_\":false}," +
        "\"VoicemailOnNoAnswer\":{\"_\":false}," +
        "\"ForwardingPermitted\":{\"_\":true}," +
        "\"PersonalDestination\":{\"_\":\"" + personalDestination + "\"}}," +
      "\"_CLASS\":\"ICM_ForwardingRule\"," +
      "\"Id\":{\"_\":\"" + forwardingRule + "\"}" +
    "}";

    addToArray(additionalData, "Rules", rule);
  }

  private boolean isRulesetPresent(String id, JSONObject additionalData)
  {
    return isIdPresent("RuleSets", id, additionalData);
  }

  private boolean isIdPresent(String name, String id, JSONObject additionalData)
  {
    JSONArray array = additionalData.optJSONArray(name);

    boolean matched = false;
    if (array != null)
    {
      for (int i = 0; !matched && i < array.length(); i++)
      {
        JSONObject item = array.optJSONObject(i);
        if (item != null)
        {
          String itemId = getString(item, "Id._");
          matched = id.equals(itemId);
        }
      }
    }
    return matched;
  }

  /**
   * Validates the SimpleICM structure.
   *
   * @throws SimpleICMError
   */
  public void validate() throws SimpleICMError
  {
    // Set the internal name appropriately for all the actions
    if (mNormal != null)
    {
      mNormal.mInternalName = "normal";
    }
    if (mVIP != null)
    {
      mVIP.mInternalName = "vip";
    }
    if (mUnwanted != null)
    {
      mUnwanted.mInternalName = "unwanted";
    }
    if (mWithheld != null)
    {
      mWithheld.mInternalName = "withheld";
    }

    if (mNormal == null)
    {
      throw new SimpleICMError(SimpleICMErrorType.ERROR_MISSING_NORMAL, "normal");
    }
    else
    {
      validateAction(mNormal);
      validateAction(mVIP);
      validateAction(mUnwanted);
      validateAction(mWithheld);
    }

    if (mNumbers != null)
    {
      for (int i = 0; i < mNumbers.length; i++)
      {
        if (mNumbers[i] != null)
        {
          // If a number is present, it must be valid
          validateNumber(mNumbers[i].mNumber, "numbers[" + i + "].number");
        }
      }
    }

    if (mForward != null)
    {
      // The forwarding number must validate
      validateNumber(mForward, "forward");
    }
    else if (mForwardOnBusy ||
             mForwardOnNoAnswer ||
             SimpleICMProfile.FORWARD.equals(mProfile))
    {
      // These things all require a forwarding number, but none was given.
      // To allow the cases to be more easily related back to what caused this
      // we report the error on the field whose presence requires the number,
      // rather than on the missing number itself.
      String field = mForwardOnNoAnswer ? "forwardOnNoAnswer" :
                     mForwardOnBusy ? "forwardOnBusy" :
                     "profile";

      throw new SimpleICMError(SimpleICMErrorType.ERROR_REQUIRES_DESTINATION,
                               field);
    }
  }

  private void validateAction(SimpleICMAction action) throws SimpleICMError
  {
    if (action != null && action.mType != null)
    {
      if (action.mType.equals(SimpleICMActionType.NORMAL) && action == mNormal)
      {
        // You can't refer back to normal from the normal action itself
        throw new SimpleICMError(SimpleICMErrorType.ERROR_INVALID_TYPE,
                                 action.mInternalName + ".type");
      }

      int numberCount = mNumbers != null ? mNumbers.length : 0;

      if (action.mRingers != null)
      {
        if (action.mRingers.length != numberCount)
        {
          throw new SimpleICMError(SimpleICMErrorType.ERROR_INVALID_RINGERS,
                                   action.mInternalName + ".ringers");
        }

        boolean atLeastOne = false;
        for (int i = 0; !atLeastOne && i < action.mRingers.length; i++)
        {
          atLeastOne |= action.mRingers[i];
        }

        if (!atLeastOne)
        {
          // There was no phone actually set to ring
          throw new SimpleICMError(SimpleICMErrorType.ERROR_MISSING_RINGER,
                                   action.mInternalName + ".ringers");
        }
      }

      if (action.mType.equals(SimpleICMActionType.FORWARD) &&
          mForward == null &&
          action.mDestination == null)
      {
        // This action is doing a forward, but we don't have either a global
        // forwarding number defined, or an action specific one
        throw new SimpleICMError(SimpleICMErrorType.ERROR_MISSING_DESTINATION,
                                 action.mInternalName + ".destination");
      }

      if (action.mDestination != null)
      {
        validateNumber(action.mDestination, action.mInternalName + ".destination");
      }
    }
  }

  private void validateNumber(String number,
                              String hint)
  {
    // @@@ Not yet validated!
  }
}
