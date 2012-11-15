/* (c) Copyright Metaswitch Networks 2011 */

package net.java.sip.communicator.impl.protocol.commportal.contacts;

import java.util.*;

import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;

import org.json.*;

/**
 * Parses a CommPortal contact address from the JSON returned by CommPortal
 */
class CPContactAddress
{
    /**
     * An enum representing the different types of address that CommPortal
     * supports
     */
    enum CPContactAddressType
    {
        HomeAddress(new CPContactHomeAddressFactory()),
        WorkAddress(new CPContactWorkAddressFactory());

        /**
         * The address factory for this address type
         */
        private final CPContactAddressFactory mAddressFactory;

        CPContactAddressType(CPContactAddressFactory addressFactory)
        {
            mAddressFactory = addressFactory;
        }

        /**
         * Get the address factory for this address type
         */
        CPContactAddressFactory getAddressFactory()
        {
            return mAddressFactory;
        }
    }

    /**
     * An enum of all the address types that CommPortal Supports
     */
    private enum CPContactAddressDetails
    {
        Street
        {
            @Override
            GenericDetail createDetail(String data,
                                       CPContactAddressFactory factory)
            {
                return factory.createStreetDetails(data);
            }
        },

        PostalCode
        {
            @Override
            GenericDetail createDetail(String data,
                                       CPContactAddressFactory factory)
            {
                return factory.createPostalCodeDetails(data);
            }
        },

        Region
        {
            @Override
            GenericDetail createDetail(String data,
                                       CPContactAddressFactory factory)
            {
                return factory.createRegionDetails(data);
            }
        },

        Locality
        {
            @Override
            GenericDetail createDetail(String data,
                                       CPContactAddressFactory factory)
            {
                return factory.createLocalityDetails(data);
            }
        },

        Country
        {
            @Override
            GenericDetail createDetail(String data,
                                       CPContactAddressFactory factory)
            {
                return factory.createCountryDetails(data);
            }
        };

        /**
         * Create a generic detail for this address type or return null if the
         * json data does not contain any data for this address type
         *
         * @param jsonData The json data got from CommPortal
         * @param factory The factory to create the address details
         * @return the detail or null if there is no data
         */
        GenericDetail maybeCreateDetail(JSONObject jsonData,
                                        CPContactAddressFactory factory)
        {
            String data = jsonData.optString(this.toString());
            return data == null ? null : createDetail(data, factory);
        }

        /**
         * Create a detail for this data type
         *
         * @param data The (non-null) data
         * @param factory The factory to create the address details
         * @return the detail object
         */
        abstract GenericDetail createDetail(String data,
                                            CPContactAddressFactory factory);
    }

    /** List of the details of this address object */
    private final ArrayList<GenericDetail> mDetails =
                                                 new ArrayList<GenericDetail>();

    /**
     * Creates and returns an instance of the user from the provided JSON data.
     *
     * @param data The JSONObject containing user data
     * @return A new CPContactAddress created from the JSON data.
     */
    CPContactAddress(JSONObject data, CPContactAddressType addressType)
    {
        CPContactAddressFactory factory = addressType.getAddressFactory();

        for (CPContactAddressDetails detail : CPContactAddressDetails.values())
        {
            mDetails.add(detail.maybeCreateDetail(data, factory));
        }
    }

    /**
     * @return the details of this address object
     */
    ArrayList<GenericDetail> getDetails()
    {
        return mDetails;
    }
}
