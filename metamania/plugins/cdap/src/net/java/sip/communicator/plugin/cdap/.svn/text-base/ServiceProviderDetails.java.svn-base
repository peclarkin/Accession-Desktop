// $Workfile:   ServiceProviderDetails.java  $ $Revision:   1.0  $
// (C) COPYRIGHT DATA CONNECTION LIMITED 2011
package net.java.sip.communicator.plugin.cdap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class which contains the service provider details retrieved from CDAP.
 *
 * This file has been copied verbatim from the AccessionAndroid source.
 */
public class ServiceProviderDetails implements Serializable
{
  private final String mId;
  private final String mName;
  private final String mRegion;
  private final Boolean mHidden;
  private static final String TRIAL_PROVIDERS = "Trial Providers";

  /**
   * The service provider details can only be set on construction. Once the
   * object has been initialized, they are read-only.
   * @param id - service provider ID (as defined on CDAP)
   * @param name - service provide name
   * @param region - service provider region
   * @param hidden - whether this service provider is hidden
   */
  public ServiceProviderDetails(String id, String name, String region, Boolean hidden)
  {
    mId = id;
    mName = name;
    mRegion = region;
    mHidden = hidden;
  }

  /**
   * @return service provider's CDAP ID.
   */
  public String getId()
  {
    return mId;
  }

  /**
   * @return service provider's CDAP name.
   */
  public String getName()
  {
    return mName;
  }

  /**
   * @return service provider's CDAP region
   */
  public String getRegion()
  {
    return mRegion;
  }

  /**
   * @return whether the service provider's name should be hidden in the list
   *         of service providers.
   */
  public boolean isHidden()
  {
    return mHidden;
  }

  /**
   * Given a prefix, get a list of service provider names satisfying one of the
   * following criteria:
   * - the SP name starts with the prefix
   * - the SP name contains the prefix preceeded by a space
   * - the user has entered the special code 'trial providers' and the service
   *   provider is under trial.
   *
   * @param prefix - string to match in the service provider name
   * @param serviceProviderDetailsMap - map of service provider name to
   *                                    ServiceProviderDetails objects.
   * @return Alphabetically ordered list of service provider names matching the
   *         given prefix.
   */
  public static ArrayList<String> getPrefixMatches(
                  String prefix,
                  HashMap<String, ServiceProviderDetails> serviceProviderDetailsMap)
  {
    ArrayList<String> prefixMatches = new ArrayList<String>();

    Iterator<String> it = serviceProviderDetailsMap.keySet().iterator();

    while (it.hasNext())
    {
      String serviceProviderName = it.next();
      int prefixLength = prefix.length();
      boolean prefixMatch = false;

      // For matching, replace all accented unicode characters with ASCII and
      // convert everything to lower case.
      String prefixLowerCase = removeAccents(prefix.toLowerCase());
      String serviceProviderNameLowerCase =
                        removeAccents(serviceProviderName.toLowerCase());

      // Match if the prefix matches the start of the service provider name or
      // the service provider name from any space onwards.
      if ((prefixLength <= serviceProviderName.length()) &&
          ((serviceProviderNameLowerCase.startsWith(prefixLowerCase)) ||
           (serviceProviderNameLowerCase.contains(" " + prefixLowerCase))))
      {
        prefixMatch = true;
      }

      boolean isHidden =
                 serviceProviderDetailsMap.get(serviceProviderName).isHidden();

      // Add service providers if they match one of the criteria in the
      // javadoc.
      if ((prefixMatch && !isHidden)||
          (prefix.equalsIgnoreCase(TRIAL_PROVIDERS) && isHidden))
      {
        prefixMatches.add(serviceProviderName);
      }
    }

    // Put the names in alphabetical order.
    Collections.sort(prefixMatches, String.CASE_INSENSITIVE_ORDER);

    if (prefix.equalsIgnoreCase(TRIAL_PROVIDERS))
    {

    }

    return prefixMatches;
  }

  @Override
  public boolean equals(Object object)
  {
    boolean isEqual = false;

    if (object == null)
    {
      // By definition not equal
      isEqual = false;
    }
    else if (this == object)
    {
      // Refer to the same object, must be the same
      isEqual = true;
    }
    else
    {
      try
      {
        ServiceProviderDetails serviceProviderDetails =
                                                (ServiceProviderDetails)object;

        isEqual = mId.equals(serviceProviderDetails.getId()) &&
                  mName.equals(serviceProviderDetails.getName()) &&
                  mRegion.equals(serviceProviderDetails.getRegion()) &&
                  mHidden == serviceProviderDetails.isHidden();
      }
      catch (ClassCastException e)
      {
        // Do nothing, they aren't the same type
      }
    }

    return isEqual;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder(128);

    builder.append("\tmId=").append(mId)
           .append("\n\tmName=").append(mName)
           .append("\n\tmRegion=").append(mRegion)
           .append("\n\tmHidden=").append(mHidden);

    return builder.toString();
  }

  /**
   * String containing plain ASCII characters which can have a unicode
   * representation with a diacritical mark.
   */
  public static final String PLAIN_ASCII =
      "AaEeIiOoUu" // grave
    + "AaCcEeIiLlNnOoRrSsUuYyZz" // acute
    + "AaEeIiOoUuYy" // circumflex
    + "AaOoNn" // tilde
    + "AaEeIiOoUuYy" // umlaut
    + "AaUu" // ring
    + "CcSsTt" // cedilla
    + "OoUu" // double acute
    + "LlOo" // stroke
    + "AaOo" // AE, OE
    + "Ss" // section sign
    + "Ss" // eszett
    + "AaEeIiOoUu" // macron
    + "Gg" // breve
    + "AaEe" // ogonek
    + "IZz" // dot
    + "i" // dotless
    + "CcDdLlNnRrSsTtZz" // caron
    + "Tt" // comma below
    ;

  /**
   * Unicode representation of the the plain ASCII String above with
   * the indicated diacritical marks.
   */
  public static final String UNICODE =
    "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
    + "\u00C1\u00E1\u0106\u0107\u00C9\u00E9\u00CD\u00ED\u0139\u013A" +
        "\u0143\u0144\u00D3\u00F3\u0154\u0155\u015A\u015B" +
        "\u00DA\u00FA\u00DD\u00FD\u0179\u017A"
    + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
    + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
    + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
    + "\u00C5\u00E5\u016E\u016F"
    + "\u00C7\u00E7\u015E\u015F\u0162\u0163"
    + "\u0150\u0151\u0170\u0171"
    + "\u0141\u0142\u00D8\u00F8"
    + "\u00C6\u00E6\u0152\u0153"
    + "\u00a7\u00a7"
    + "\u00df\u00df"
    + "\u0100\u0101\u0112\u0113\u012A\u012B\u014C\u014D\u016A\u016B"
    + "\u011E\u011F"
    + "\u0104\u0105\u0118\u0119"
    + "\u0130\u017B\u017C"
    + "\u0131"
    + "\u010C\u010D\u010E\u010F\u013D\u013E\u0147\u0148\u0158\u0159" +
            "\u0160\u0161\u0164\u0165\u017D\u017E"
    ;

  /**
   * Replace accented characters with ascii equivalent.
   * Note that this could be replaced using the Normalizer class but this is
   * only available from Android 2.3 onwards and this does not presently cover
   * all supported versions. Implementing this would be much better than
   * doing a lookup on the Strings above.
   */
  public static String removeAccents(String accentedString)
  {
    int length = accentedString.length();
    StringBuilder stringBuilder = new StringBuilder(length);
    int positionInCharList = -1;
    char character;

    if (accentedString != null)
    {
      // remove accents from string
      // The for loop is deliberately un-logged
      for (int i = 0; i < length; i++)
      {
        character = accentedString.charAt(i);
        positionInCharList = (character <= 126) ? -1 : UNICODE.indexOf(character);

        if (positionInCharList > -1)
        {
          // Replace the unicode character with a plain ASCII character.
          stringBuilder.append(PLAIN_ASCII.charAt(positionInCharList));
        }
        else
        {
          // Already plain ASCII - just add to StringBuilder.
          stringBuilder.append(character);
        }
      }
    }

    String plainString = stringBuilder.toString();

    return plainString;
  }
}