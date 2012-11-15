package net.java.sip.communicator.impl.protocol.commportal.contacts;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;

/**
 * Factory for creating the work address details
 */
class CPContactWorkAddressFactory extends CPContactAddressFactory
{
    @Override
    GenericDetail createStreetDetails(String data)
    {
        return new ServerStoredDetails.WorkAddressDetail(data);
    }

    @Override
    GenericDetail createPostalCodeDetails(String data)
    {
        return new ServerStoredDetails.WorkPostalCodeDetail(data);
    }

    @Override
    GenericDetail createRegionDetails(String data)
    {
        return new ServerStoredDetails.WorkProvinceDetail(data);
    }

    @Override
    GenericDetail createLocalityDetails(String data)
    {
        return new ServerStoredDetails.WorkCityDetail(data);
    }

    @Override
    GenericDetail createCountryDetails(String data)
    {
        return new PersonalContactDetails.WorkCountryDetail(data);
    }
}
