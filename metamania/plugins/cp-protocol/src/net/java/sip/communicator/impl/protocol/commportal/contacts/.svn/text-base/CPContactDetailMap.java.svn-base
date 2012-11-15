package net.java.sip.communicator.impl.protocol.commportal.contacts;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.EmailAddress1Detail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.EmailAddress2Detail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.HomePhoneDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.OtherPhoneDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.PreferredEmailDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.PreferredNumberDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.SMSDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.WorkTitleDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.AddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.CityDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FaxDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FirstNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.LastNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.MobilePhoneDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.NicknameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.PostalCodeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ProvinceDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkAddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkCityDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkOrganizationNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkPhoneDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkPostalCodeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkProvinceDetail;

/**
 * A class for converting GenericDetails into the CommPortal JSON API
 * representation
 */
public class CPContactDetailMap
{
    /**
     * A map from generic details to the name of that detail in the CommPortal
     * JSON API
     */
    private static final HashMap<Class<? extends GenericDetail>, String>
     DETAIL_CP_NAME_MAP = new HashMap<Class<? extends GenericDetail>, String>();
    static
    {
        // Name details
        DETAIL_CP_NAME_MAP.put(FirstNameDetail.class, "GivenName");
        DETAIL_CP_NAME_MAP.put(LastNameDetail.class,  "FamilyName");
        DETAIL_CP_NAME_MAP.put(NicknameDetail.class,  "Nickname");
        DETAIL_CP_NAME_MAP.put(WorkTitleDetail.class, "JobTitle");
        DETAIL_CP_NAME_MAP.put(WorkOrganizationNameDetail.class,"Organization");

        // Number details
        DETAIL_CP_NAME_MAP.put(HomePhoneDetail.class,   "HomePhone");
        DETAIL_CP_NAME_MAP.put(WorkPhoneDetail.class,   "WorkPhone");
        DETAIL_CP_NAME_MAP.put(MobilePhoneDetail.class, "CellPhone");
        DETAIL_CP_NAME_MAP.put(FaxDetail.class,         "Fax");
        DETAIL_CP_NAME_MAP.put(OtherPhoneDetail.class,  "OtherPhone");

        // Email and preferences
        DETAIL_CP_NAME_MAP.put(PreferredNumberDetail.class, "PreferredPhone");
        DETAIL_CP_NAME_MAP.put(EmailAddress1Detail.class,   "Email1");
        DETAIL_CP_NAME_MAP.put(EmailAddress2Detail.class,   "Email2");
        DETAIL_CP_NAME_MAP.put(PreferredEmailDetail.class,  "PreferredEmail");
        DETAIL_CP_NAME_MAP.put(SMSDetail.class,             "SMS");

        // Address details
        DETAIL_CP_NAME_MAP.put(AddressDetail.class,        "Street");
        DETAIL_CP_NAME_MAP.put(WorkAddressDetail.class,    "Street");
        DETAIL_CP_NAME_MAP.put(PostalCodeDetail.class,     "PostalCode");
        DETAIL_CP_NAME_MAP.put(WorkPostalCodeDetail.class, "PostalCode");
        DETAIL_CP_NAME_MAP.put(ProvinceDetail.class,       "Region");
        DETAIL_CP_NAME_MAP.put(WorkProvinceDetail.class,   "Region");
        DETAIL_CP_NAME_MAP.put(CityDetail.class,           "Locality");
        DETAIL_CP_NAME_MAP.put(WorkCityDetail.class,       "Locality");
        DETAIL_CP_NAME_MAP.put(PersonalContactDetails.CountryDetail.class,
                               "Country");
        DETAIL_CP_NAME_MAP.put(PersonalContactDetails.WorkCountryDetail.class,
                               "Country");
    }

    /**
     * Get the name that this detail type is represented by in the CommPortal
     * JSON API.
     * <p/>
     * Returns null if the detail type is not supported
     *
     * @param detail The detail to consider
     * @return The name of the corresponding field in the JSON API
     */
    public static String getCPNameForDetail(GenericDetail detail)
    {
        return DETAIL_CP_NAME_MAP.get(detail.getClass());
    }

    /**
     * Map between a detail type and the corresponding type of address.  Note
     * that if this map does not contain a particular detail it is because that
     * detail is not an address type
     */
    private static final HashMap<Class<? extends GenericDetail>, String>
        DETAIL_CP_ADDRESS_NAME_MAP = new HashMap<Class<? extends GenericDetail>,
                                                 String>();
    static
    {
        // Home address types
        DETAIL_CP_ADDRESS_NAME_MAP.put(AddressDetail.class,    "HomeAddress");
        DETAIL_CP_ADDRESS_NAME_MAP.put(PostalCodeDetail.class, "HomeAddress");
        DETAIL_CP_ADDRESS_NAME_MAP.put(ProvinceDetail.class,   "HomeAddress");
        DETAIL_CP_ADDRESS_NAME_MAP.put(CityDetail.class,       "HomeAddress");
        DETAIL_CP_ADDRESS_NAME_MAP.put(
                   PersonalContactDetails.CountryDetail.class, "HomeAddress");

        // Work address types:
        DETAIL_CP_ADDRESS_NAME_MAP.put(WorkAddressDetail.class,    "WorkAddress");
        DETAIL_CP_ADDRESS_NAME_MAP.put(WorkPostalCodeDetail.class, "WorkAddress");
        DETAIL_CP_ADDRESS_NAME_MAP.put(WorkProvinceDetail.class,   "WorkAddress");
        DETAIL_CP_ADDRESS_NAME_MAP.put(WorkCityDetail.class,       "WorkAddress");
        DETAIL_CP_ADDRESS_NAME_MAP.put(
                   PersonalContactDetails.WorkCountryDetail.class, "WorkAddress");
    }

    /**
     * Get the type of address of a generic detail and return the name of that
     * type in the CommPortal JSON API.  Returns null if this detail is not an
     * address.
     *
     * @param detail The detail to consider
     * @return The address type or null if this is not an address
     */
    public static String getCPAddressTypeForDetail(GenericDetail detail)
    {
        return DETAIL_CP_ADDRESS_NAME_MAP.get(detail.getClass());
    }
}
