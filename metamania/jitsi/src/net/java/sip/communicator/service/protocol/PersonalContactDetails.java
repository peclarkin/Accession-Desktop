package net.java.sip.communicator.service.protocol;

import net.java.sip.communicator.service.protocol.ServerStoredDetails.EmailAddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.PhoneNumberDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.StringDetail;

/**
 * Class containing all the data types that personal contacts support but that
 * are not covered by one of the ServerStoredDetails
 */
public class PersonalContactDetails
{
    /**
     * Class for displaying the "Other" phone number field of a contact
     */
    public static class OtherPhoneDetail extends PhoneNumberDetail
    {
        public OtherPhoneDetail(String otherNumber)
        {
            super(otherNumber);
        }
    }

    /**
     * Class for displaying the "Home" phone number field of a contact
     */
    public static class HomePhoneDetail extends PhoneNumberDetail
    {
        public HomePhoneDetail(String homeNumber)
        {
            super(homeNumber);
        }
    }

    /**
     * Class for displaying the job title field of a contact
     */
    public static class WorkTitleDetail extends StringDetail
    {
        public WorkTitleDetail(String jobTitle)
        {
            super("Job Title", jobTitle);
        }
    }

    /**
     * Class for displaying the "SMS" field of a contact
     */
    public static class SMSDetail extends PhoneNumberDetail
    {
        public SMSDetail(String smsNumber)
        {
            super(smsNumber);
        }
    }

    /**
     * Class for displaying the "Preferred Email" field of a contact, where
     * the value specifies which detail has the address e.g. "Work", not
     * "john@software.com".
     */
    public static class PreferredEmailDetail extends StringDetail
    {
        public PreferredEmailDetail(String preferredEmail)
        {
            super("Preferred Email", preferredEmail);
        }
    }

    /**
     * Class for displaying the "Preferred Number" field of a contact, where
     * the value specifies which detail has the address e.g. "Home", not
     * "01234567890".
     */
    public static class PreferredNumberDetail extends StringDetail
    {
        public PreferredNumberDetail(String preferredPhone)
        {
            super("Preferred Phone Number", preferredPhone);
        }
    }

    /**
     * Class for the email address 1 field of a Contact
     */
    public static class EmailAddress1Detail extends EmailAddressDetail
    {
        public EmailAddress1Detail(String value)
        {
            super("Email address", value);
        }
    }

    /**
     * Class for the email address 2 field of a Contact
     */
    public static class EmailAddress2Detail extends EmailAddressDetail
    {
        public EmailAddress2Detail(String value)
        {
            super("Email address", value);
        }
    }

    /**
     * Class for the country detail of the contact.
     * <P/>
     * Used when the locale is not known
     */
    public static class CountryDetail extends GenericDetail
    {
        public CountryDetail(String value)
        {
            super("Country detail", value);
        }
    }

    /**
     * Class for work the country detail of the contact.
     * <P/>
     * Used when the locale is not known
     */
    public static class WorkCountryDetail extends GenericDetail
    {
        public WorkCountryDetail(String value)
        {
            super("Work country detail", value);
        }
    }

    public static class IMDetail extends GenericDetail
    {
        public IMDetail(String value)
        {
            super("IMDetail", value);
        }
    }
}
