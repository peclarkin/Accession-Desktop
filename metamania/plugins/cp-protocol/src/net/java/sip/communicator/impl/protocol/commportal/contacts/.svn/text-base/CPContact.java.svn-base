/* (c) Copyright Metaswitch Networks 2011 */

package net.java.sip.communicator.impl.protocol.commportal.contacts;

import java.util.*;
import java.util.concurrent.*;

import net.java.sip.communicator.impl.protocol.commportal.contacts.CPContactAddress.CPContactAddressType;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FaxDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FirstNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.LastNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.MobilePhoneDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.NicknameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkOrganizationNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkPhoneDetail;
import net.java.sip.communicator.util.*;

import org.json.*;

/**
 * Represents a CommPortal contact, used for parsing JSON objects returned from
 * the CommPortal server into a ContactSipImpl object
 */
public class CPContact implements Contact
{
    private static final Logger sLog = Logger.getLogger(CPContact.class);

    /**
     * Enum containing the different types of data that CommPortal can return
     * <p/>
     * Note that the name of the enum object matches what it is called in the
     * JSON from CommPortal.
     * <p/>
     * Note also that the address types are covered elsewhere
     */
    private enum CommPortalDataTypeEnum
    {
        // NAMES:
        GivenName
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new FirstNameDetail(dataValue);
            }
        },

        FamilyName
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new LastNameDetail(dataValue);
            }
        },

        Nickname
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new NicknameDetail(dataValue);
            }
        },

        // WORK INFORMATION
        JobTitle
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new PersonalContactDetails.WorkTitleDetail(dataValue);
            }
        },

        Organization
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new WorkOrganizationNameDetail(dataValue);
            }
        },

        // PHONE NUMBERS:
        WorkPhone
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new WorkPhoneDetail(dataValue);
            }
        },

        HomePhone
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new PersonalContactDetails.HomePhoneDetail(dataValue);
            }
        },

        CellPhone
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new MobilePhoneDetail(dataValue);
            }
        },

        Fax
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new FaxDetail(dataValue);
            }
        },

        OtherPhone
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new PersonalContactDetails.OtherPhoneDetail(dataValue);
            }
        },

        // EMAIL ADDRESSES
        Email1
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new PersonalContactDetails.EmailAddress1Detail(dataValue);
            }
        },

        Email2
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new PersonalContactDetails.EmailAddress2Detail(dataValue);
            }
        },

        // SMS
        SMS
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new PersonalContactDetails.SMSDetail(dataValue);
            }
        },

        // PREFERENCES
        PreferredPhone
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new PersonalContactDetails.PreferredNumberDetail(dataValue);
            }
        },

        PreferredEmail
        {
            @Override
            GenericDetail createDetailForType(String dataValue)
            {
                return new PersonalContactDetails.PreferredEmailDetail(dataValue);
            }
        };

        /**
         * Create a GenericDetail object for this type
         *
         * @param dataValue The number to contain in the type
         * @return The generic detail
         */
        abstract GenericDetail createDetailForType(String dataValue);

        /**
         * Possibly create a detail for this number type depending on whether we
         * have any data for this object type
         *
         * @param jsonData the data of the contact
         * @return a generic detail for the tpye or null
         */
        GenericDetail maybeCreateDetailForType(JSONObject jsonData)
        {
            // Note that the name of the enum object matches what it is called
            // in the JSON from CommPortal
            String dataValue = jsonData.optString(toString());
            return (dataValue == null || dataValue.length() == 0) ?
                                          null : createDetailForType(dataValue);
        }
    }

    /** The list of details associated with this contact */
    private ArrayList<GenericDetail> mDetails = new ArrayList<GenericDetail>();

    /** A map from the CommPortal Address type to the Address object itself */
    private final HashMap<CPContactAddressType, CPContactAddress> mAddressMap =
                          new HashMap<CPContactAddressType, CPContactAddress>();

    /** Display name for this contact */
    private final String mDisplayName;

    /** UID for this contact */
    private final String mUID;

    /**
     * Address of this contact - this is not the same as the UID which comes
     * from the server.  The address is not part of CommPortal at all
     */
    private final String mAddress;

    /** Persistent data of this contact */
    private String mPersistentData;

    /** True if this contact is resolved */
    private boolean mResolved;

    /** Protocol provider for this contact */
    private final ProtocolProviderService mProvider;

    /** The group to which this contact belongs */
    private ContactGroup mGroup;

    /**
     * Creates and returns an instance of the user from the provided JSON data.
     *
     * @param data     The JSONObject containing user data
     * @param provider The provider that is creating us
     * @param group    The group to which this contact is added
     * @param resolved True if this contact is resolved
     */
    public CPContact(JSONObject data,
                     ProtocolProviderService provider,
                     ContactGroup group,
                     boolean resolved)
    {
        mPersistentData = data.toString();
        mGroup = group;
        mProvider = provider;
        mResolved = resolved;

        mUID = data.optString("UID");
        mAddress = "CommPortal@" + mUID;

        // Get the details of the contact
        for (CommPortalDataTypeEnum type : CommPortalDataTypeEnum.values())
        {
            GenericDetail genericDetail = type.maybeCreateDetailForType(data);

            if (genericDetail != null)
            {
                mDetails.add(genericDetail);
            }
        }

        // And add the address details
        for (CPContactAddressType type : CPContactAddressType.values())
        {
            JSONObject jsonAddr = data.optJSONObject(type.toString());

            if (jsonAddr != null)
            {
                CPContactAddress address = new CPContactAddress(jsonAddr, type);
                mDetails.addAll(address.getDetails());
                mAddressMap.put(type, address);
            }
        }

        // And create the display name
        mDisplayName = createDisplayName(data);
    }

    /**
     * Create a contact from an address - i.e. a very "bare bones" contact. This
     * is called when creating a contact on the client.
     *
     * @param group The group to which this contact belongs
     * @param address The UID of this contact
     * @param provider The provider that is creating us
     */
    public CPContact(ContactGroup group,
                     String address,
                     ProtocolProviderService provider)
    {
        mUID = null;
        mGroup = group;
        mAddress = address;
        mProvider = provider;

        // We don't care about the persistent data or display name as this
        // contact won't be displayed until we've got some from the server
        mPersistentData = "";
        mDisplayName = "";

        // Not resolved as it's only just been created locally!
        mResolved = false;
    }

    /**
     * Create the display name for a contact
     *
     * @param data JSONObject containing the details of the contact
     * @return The display name
     */
    private String createDisplayName(JSONObject data)
    {
        String displayName;

        String firstName = data.optString(CommPortalDataTypeEnum.GivenName.toString());
        String surname = data.optString(CommPortalDataTypeEnum.FamilyName.toString());

        if (firstName == null || firstName.length() == 0)
        {
            displayName = surname;
        }
        else if (surname == null || surname.length() == 0)
        {
            displayName = firstName;
        }
        else
        {
            displayName = firstName + " " + surname;
        }

        return displayName;
    }

    public String getAddress()
    {
        return mAddress;
    }

    /**
     * @return The CommPortal UID of this contact
     */
    String getUID()
    {
        return mUID;
    }

    /**
     * Convert this object into a JSON representation as a string
     *
     * @param builder The builder to add the representation to
     * @return the JSON representation as a string builder
     */
    JSONObject toJsonString()
    {
        JSONObject json = new JSONObject();

        try
        {
            // The UID is not stored as a generic detail so we must add it
            // ourselves. Add with putOpt so that it is not added if UID is null
            json.putOpt("UID", mUID);

            HashMap<String, JSONObject> addressDetails =
                                              new HashMap<String, JSONObject>();

            // Go through each detail and, either add it straight to the builder
            // (if it is not an address) or add it to the map of address details
            // to be added later
            for (GenericDetail detail : mDetails)
            {
                String addressType =
                           CPContactDetailMap.getCPAddressTypeForDetail(detail);
                String detailName =
                                  CPContactDetailMap.getCPNameForDetail(detail);

                if (addressType != null)
                {
                    // This is an address type thus add it to the map of address
                    // types.  First, create a json object for this address type
                    // if we haven't already got one
                    if (!addressDetails.containsKey(addressType))
                    {
                        addressDetails.put(addressType, new JSONObject());
                    }

                    // Detail name will not be null as this is a CP address type
                    addressDetails.get(addressType).put(detailName,
                                                       detail.getDetailValue());
                }
                else if (detailName != null)
                {
                    // Not an address type so we can just add it
                    json.put(detailName, detail.getDetailValue());
                }
                else
                {
                    sLog.warn("Unknown data type " + detail.getDetailValue());
                }
            }

            // So we have added every non-address detail.  Now we just need to add
            // the address details
            for (String key : addressDetails.keySet())
            {
                JSONObject address = addressDetails.get(key);
                json.put(key, address);
            }
        }
        catch (JSONException e)
        {
            // This should never happen
            sLog.error("Unexpected JSON error while converting contact", e);
        }

        return json;
    }

    /**
     * Get the details of a contact
     *
     * @param detailClass The sort of details that we are interested in. If null
     *                    then all details will be returned.
     * @return the details of a contact
     */
    public ArrayList<GenericDetail> getDetails(
                                     Class<? extends GenericDetail> detailClass)
    {
        ArrayList<GenericDetail> details;

        if (detailClass == null)
        {
            details = mDetails;
        }
        else
        {
            details = new ArrayList<ServerStoredDetails.GenericDetail>();
            for (ServerStoredDetails.GenericDetail detail : mDetails)
            {
                if (detail.getClass().equals(detailClass))
                {
                    details.add(detail);
                }
            }
        }

        return details;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof CPContact))
        {
            // Passed null or a non-CPContact object
            return false;
        }

        CPContact contact = (CPContact)obj;

        if (!getAddress().equals(contact.getAddress()) ||
            !mDisplayName.equals(contact.getDisplayName()))
        {
            return false;
        }

        // UIDs are identical as are the display names.
        // Now to try the details.
        CopyOnWriteArrayList<GenericDetail> contactDetails =
                      new CopyOnWriteArrayList<GenericDetail>(getDetails(null));

        // A detail has been added or removed, therefore they are not the same
        if (mDetails.size() != contactDetails.size())
        {
            return false;
        }

        // For each local detail, check that there is a corresponding detail in
        // the contact that was passed in
        for (GenericDetail localDetail : mDetails)
        {
            boolean matchFound = false;

            for (GenericDetail contactDetail : contactDetails)
            {
                if (localDetail.equals(contactDetail))
                {
                    // We've found a match so remove this detail - otherwise we
                    // might count two identical details as once
                    matchFound = true;
                    contactDetails.remove(contactDetail);
                    break;
                }
            }

            if (!matchFound)
            {
                // We didn't find a match for a particular number so they aren't
                // equal.
                return false;
            }

            // Note that we don't need to check the other way round, as we know
            // that there are the same number of details in both
        }

        // The objects have the same display name, same details and same UID.
        // They are equal
        return true;
    }

    public byte[] getImage()
    {
        return new byte[0];
    }

    public PresenceStatus getPresenceStatus()
    {
        return CommPortalPresenceStatus.NA;
    }

    public ContactGroup getParentContactGroup()
    {
        return mGroup;
    }

    public ProtocolProviderService getProtocolProvider()
    {
        return mProvider;
    }

    public boolean isPersistent()
    {
        // Contact is always persistent
        return true;
    }

    public boolean isResolved()
    {
        return mResolved;
    }

    public void setResolved(boolean resolved)
    {
        mResolved = resolved;
    }

    public String getPersistentData()
    {
        return mPersistentData;
    }

    public String getStatusMessage()
    {
        // No status message so just return the empty string
        return "";
    }

    public String getDisplayName()
    {
        return mDisplayName;
    }

    /**
     * Set the details of a contact, typically called before trying to send the
     * contact to the server
     *
     * @param details The details to set.
     */
    void setDetails(ArrayList<GenericDetail> details)
    {
        mDetails = details;

        // We also need to update the persistent data:
        mPersistentData = toJsonString().toString();
    }
}
