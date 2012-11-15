package net.java.sip.communicator.impl.protocol.commportal.contacts;

import net.java.sip.communicator.impl.protocol.commportal.contacts.CPContactAddress.*;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;

/**
 * Factory class for creating the "GenericDetail"s required for a contact
 * address
 */
abstract class CPContactAddressFactory
{
    /**
     * Get the factory for a particular address type
     *
     * @param type The address type we are interested in
     * @return The corresponding factory
     */
    static CPContactAddressFactory getInstance(CPContactAddressType type)
    {
        return type.getAddressFactory();
    }

    /**
     * @param data The street details
     * @return A GenericDetail object containing the street data
     */
    abstract GenericDetail createStreetDetails(String data);

    /**
     * @param data The postal code details
     * @return A GenericDetail object containing the postal code data
     */
    abstract GenericDetail createPostalCodeDetails(String data);

    /**
     * @param data The region details
     * @return A GenericDetail object containing the region data
     */
    abstract GenericDetail createRegionDetails(String data);

    /**
     * @param data The locality details
     * @return A GenericDetail object containing the locality data
     */
    abstract GenericDetail createLocalityDetails(String data);

    /**
     * @param data The country details
     * @return A GenericDetail object containing the country data
     */
    abstract GenericDetail createCountryDetails(String data);
}
