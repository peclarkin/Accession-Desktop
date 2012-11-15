package net.java.sip.communicator.impl.protocol.commportal.contacts;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;

/**
 * Factory for creating the fields for a home address
 */
class CPContactHomeAddressFactory extends CPContactAddressFactory
{
    @Override
    GenericDetail createStreetDetails(String data)
    {
        return new ServerStoredDetails.AddressDetail(data);
    }

    @Override
    GenericDetail createPostalCodeDetails(String data)
    {
        return new ServerStoredDetails.PostalCodeDetail(data);
    }

    @Override
    GenericDetail createRegionDetails(String data)
    {
        return new ServerStoredDetails.ProvinceDetail(data);
    }

    @Override
    GenericDetail createLocalityDetails(String data)
    {
        return new ServerStoredDetails.CityDetail(data);
    }

    @Override
    GenericDetail createCountryDetails(String data)
    {
        return new PersonalContactDetails.CountryDetail(data);
    }
}
