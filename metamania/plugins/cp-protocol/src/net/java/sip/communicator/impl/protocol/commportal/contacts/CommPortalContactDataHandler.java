package net.java.sip.communicator.impl.protocol.commportal.contacts;

import java.util.*;
import java.util.concurrent.*;

import net.java.sip.communicator.impl.protocol.commportal.*;
import net.java.sip.communicator.service.commportal.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

import org.json.*;

/**
 * A class for getting and parsing CommPortal Contacts
 * <p/>
 * implements the various operation sets required for contacts
 * <p/>
 * extends the Abstract Operation Set to make contacts easier
 */
public class CommPortalContactDataHandler
   extends AbstractOperationSetPersistentPresence<ProtocolProviderServiceCPImpl>
                        implements OperationSetServerStoredUpdatableContactInfo,
                                   OperationSetServerStoredContactInfo
{
    private static final Logger sLog =
                           Logger.getLogger(CommPortalContactDataHandler.class);

    /** The name of the service indication used to get contacts */
    private static final String SI_NAME = "Contacts";

    /** Service used to access CommPortal */
    private final CommPortalService mCommPortalService;

    /** A map from the UUID of a contact to the contact itself. */
    private ConcurrentHashMap<String, Contact> mContacts =
                                       new ConcurrentHashMap<String, Contact>();

    /** Callback for listening to contacts changes */
    private final CPDataRegistrationCallback mContactsListener =
                                                new CPDataRegistrationCallback()
    {
        public void onDataError(CPDataError error)
        {
            sLog.error("Data error listening for contacts " + error);
        }

        public void onDataChanged()
        {
            sLog.info("CommPortal contacts have changed");
            mCommPortalService.getServiceIndication(mContactGetter, null,false);
        }

        public String getSIName()
        {
            return SI_NAME;
        }
    };

    /** Callback for network errors while listening for contacts */
    private final CPOnNetworkErrorCallback mNetworkErrorCallback =
                                                  new CPOnNetworkErrorCallback()
    {
        public void onNetworkError(CPNetworkError error)
        {
            sLog.info("Network error listening for contacts");

            // Just request the contacts so that when the network returns we
            // remain up to date
            mCommPortalService.getServiceIndication(mContactGetter, null, true);
        }
    };

    /** Callback for getting the contacts */
    private final CPDataGetterCallback mContactGetter =
                                                      new CPDataGetterCallback()
    {
        public String getSIName()
        {
            return SI_NAME;
        }

        public DataFormat getDataFormat()
        {
            return DataFormat.DATA_JS;
        }

        public void onDataError(CPDataError error)
        {
            sLog.error("Data error getting contacts " + error);
        }

        public boolean onDataReceived(String data)
        {
            return CommPortalContactDataHandler.this.onDataReceived(data);
        }

    };

    /** True if we are registered for notifications on changes to contacts */
    private boolean mRegistered = false;

    /**
     * Constructor for the CommPortalContactDataHandler - a class which handles
     * getting contacts from CommPortal
     *
     * @param provider The provider that created us
     */
    public CommPortalContactDataHandler(ProtocolProviderServiceCPImpl provider)
    {
        super(provider);
        mCommPortalService = CommPortalProtocolActivator.getCommPortalService();
    }

    /**
     * Get the contacts and start polling for changes to the contacts
     */
    public void init()
    {
        sLog.debug("Initialising the CP data handler");

        // Get the contacts:
        mCommPortalService.getServiceIndication(mContactGetter, null, true);

        // Register for contacts changing:
        mCommPortalService.registerForNotifications(mContactsListener,
                                                    mNetworkErrorCallback);
        mRegistered = true;
    }

    /**
     * Close down the CommPortal Data handler
     */
    public void destroy()
    {
        sLog.info("Destroying the contacts data handler");

        if (mRegistered)
        {
            mRegistered = false;
            mCommPortalService.unregisterForNotifications(mContactsListener);
        }
    }

    /**
     * Handle data being received from CommPortal
     *
     * @param data the data that has been received
     * @return true if the data is valid
     */
    private boolean onDataReceived(String data)
    {
        if (sLog.isDebugEnabled())
            sLog.debug("Got contacts data");

        boolean dataValid;

        try
        {
            JSONArray jsonData = new JSONArray(data);
            final JSONArray dataArray = jsonData.getJSONObject(0)
                                                .getJSONObject("data")
                                                .getJSONArray("Contact");
            dataValid = true;

            // Run the contact adding and comparison in a new thread - otherwise
            // we block the CommPortal work thread for too long
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    compareNewAndExistingContacts(dataArray);
                }
            };

            new Thread(runnable).start();

        }
        catch (JSONException e)
        {
            sLog.error("Invalid JSON returned by server", e);
            dataValid = false;
        }

        return dataValid;
    }

    /**
     * Compare a JSON array of contacts received from CommPortal with the local
     * list of contacts and broadcast the details of any new, edited or deleted
     * contacts
     *
     * @param dataArray The array of contacts received from CommPortal
     */
    private void compareNewAndExistingContacts(JSONArray dataArray)
    {
        CommPortalContactGroup group = parentProvider.getGroup();

        // We store a list of new, changed and deleted contacts so that we
        // can tell the rest of the client that some contacts have changed
        // AFTER we have updated the cached list of contacts (otherwise we
        // might be asked for details of contact we don't yet know about).
        ArrayList<CPContact> newContacts     = new ArrayList<CPContact>();
        ArrayList<CPContact> deletedContacts = new ArrayList<CPContact>();

        // Now create a new map for the contacts - so we can simply replace
        // the existing map once we have completely parsed all the contacts
        ConcurrentHashMap<String, Contact> newContactsMap =
                           new ConcurrentHashMap<String, Contact>();

        int contacts = dataArray.length();
        for (int i = 0; i < contacts; i++)
        {
            // Parse the data into a contact object, then see if this
            // contact is a new contact or an existing one
            CPContact serverContact = new CPContact(dataArray.optJSONObject(i),
                                                    parentProvider,
                                                    group,
                                                    true);

            String uid = serverContact.getAddress();
            newContactsMap.put(uid, serverContact);
            Contact localContact = mContacts.get(uid);

            if (localContact == null)
            {
                // This is a new contact
                newContacts.add(serverContact);
            }
            else if (!localContact.equals(serverContact))
            {
                // Though there is an existing contact it is not the same
                // as the server contact - hence an edited contact. Treat
                // this as a deletion of the old contact, followed by the
                // creation of a new contact to force the UI to updates.
                deletedContacts.add((CPContact)localContact);
                newContacts.add(serverContact);
            }
        }

        // Finally, we need to go through the old list of contacts and check
        // to see if any are deleted
        for (String key : mContacts.keySet())
        {
            if (!newContactsMap.containsKey(key))
            {
                // The new contacts map does not contain the key for this
                // contact, so it must have been deleted
                deletedContacts.add((CPContact)mContacts.get(key));
            }
        }

        // Update the cached contact list
        synchronized (this)
        {
            mContacts = newContactsMap;
        }

        // And tell the rest of the client about the changed contacts:
        // DELETED - do this first as any edited contact will need to be
        // deleted before it is re-added
        for (CPContact contact : deletedContacts)
        {
            fireSubscriptionEvent(contact,
                                  group,
                                  SubscriptionEvent.SUBSCRIPTION_REMOVED);
        }

        // NEW
        for (CPContact contact : newContacts)
        {
            fireSubscriptionEvent(contact,
                                  group,
                                  SubscriptionEvent.SUBSCRIPTION_CREATED);
        }
    }

    public Iterator<Contact> getAllContacts()
    {
        return mContacts.values().iterator();
    }

    public int numberContacts()
    {
        return mContacts.size();
    }

    //-------------------------------------------------------------------------
    // METHODS FOR OperationSetServerStoredUpdatableContactInfo:
    //-------------------------------------------------------------------------

    public void setDetailsForContact(Contact contact,
                                     ArrayList<GenericDetail> details,
                                     final ContactUpdateResultListener listener)
    {
        sLog.info("User has changed a CommPortal contact");
        final CPContact cpContact = (CPContact)contact;
        final ArrayList<GenericDetail> oldDetails = cpContact.getDetails(null);
        cpContact.setDetails(details);

        CPDataSenderCallback sender = new CPDataSenderCallback()
        {
            public void onDataError(CPDataError error)
            {
                sLog.warn("Failed to change the contact data - bad data");

                // Tell the listener and revert the changes:
                listener.updateFailed(false);
                cpContact.setDetails(oldDetails);
            }

            public String getSIName()
            {
                return SI_NAME;
            }

            public DataFormat getDataFormat()
            {
                return DataFormat.DATA_JS;
            }

            public String getData()
            {
                // The data that we need to send is of the form:
                // {
                //     "Contact" :
                //     [
                //         { <Contact data> }
                //     ]
                // }
                JSONObject data = new JSONObject();

                try
                {
                    JSONArray contactArray = new JSONArray();
                    contactArray.put(cpContact.toJsonString());
                    data.put("Contact", contactArray);
                }
                catch (JSONException e)
                {
                    // This should never ever happen - only thrown if either
                    // arguments to "put" are null.
                    sLog.error("Unexpected error getting contact data ", e);
                }

                return data.toString();
            }

            public void onDataSent()
            {
                listener.updateSucceeded();
            }
        };

        CPOnNetworkErrorCallback networkError = new CPOnNetworkErrorCallback()
        {
            public void onNetworkError(CPNetworkError error)
            {
                sLog.warn("Failed to change the contact data - bad network");

                // Tell the listener and revert the changes:
                listener.updateFailed(true);
                cpContact.setDetails(oldDetails);
            }
        };

        mCommPortalService.postServiceIndication(sender, networkError, true);
    }

    //-------------------------------------------------------------------------
    // METHODS for OperationSetServerStoredContactInfo
    //-------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public <T extends GenericDetail> Iterator<T> getDetailsAndDescendants(
                                          Contact contact, Class<T> detailClass)
    {
        Iterator<GenericDetail> details = getDetails(contact, detailClass);
        List<T> result = new ArrayList<T>();

        while (details.hasNext())
        {
            GenericDetail detail = details.next();

            if (detail.getClass().equals(detailClass))
            {
                result.add((T)detail);
            }
        }

        return result.iterator();
    }

    public Iterator<GenericDetail> getDetails(Contact contact,
                                     Class<? extends GenericDetail> detailClass)
    {
        CPContact cpContact = (CPContact)contact;

        ArrayList<GenericDetail> details = cpContact == null ?
             new ArrayList<GenericDetail>() : cpContact.getDetails(detailClass);

        return details.iterator();
    }

    public Iterator<GenericDetail> getAllDetailsForContact(Contact contact)
    {
        return getDetails(contact, null);
    }

    public Iterator<GenericDetail> requestAllDetailsForContact(Contact contact,
        DetailsResponseListener listener)
    {
        // As the details are cached - request details is exactly the same as
        // get details
        return getDetails(contact, null);
    }

    //-------------------------------------------------------------------------
    // METHODS for AbstractOperationSetPersistentPresence
    //-------------------------------------------------------------------------

    public PresenceStatus getPresenceStatus()
    {
        return CommPortalPresenceStatus.NA;
    }

    public void publishPresenceStatus(PresenceStatus status,
                                      String statusMessage)
                                                throws IllegalArgumentException,
                                                       IllegalStateException,
                                                       OperationFailedException
    {
        // CommPortal does not support presence so nothing required
    }

    public Iterator<PresenceStatus> getSupportedStatusSet()
    {
        return CommPortalPresenceStatus.values();
    }

    public PresenceStatus queryContactStatus(String contactIdentifier)
                                                throws IllegalArgumentException,
                                                       IllegalStateException,
                                                       OperationFailedException
    {
        return CommPortalPresenceStatus.NA;
    }

    public Contact findContactByID(String contactID)
    {
        return mContacts.get(contactID);
    }

    public void setAuthorizationHandler(AuthorizationHandler handler)
    {
        // Nothing required - authorisation happens elsewhere
    }

    public String getCurrentStatusMessage()
    {
        // CommPortal does not support status so for now return the emtpy string
        return "";
    }

    public Contact createUnresolvedContact(String address,
                                           String persistentData)
    {
        return createUnresolvedContact(address,
                                       persistentData,
                                       parentProvider.getGroup());
    }

    public Contact createUnresolvedContact(String address,
                                           String persistentData,
                                           ContactGroup parentGroup)
    {
        // The persistent data is the JSON data received from the server.
        Contact contact = null;

        try
        {
            JSONObject data = new JSONObject(persistentData);
            contact = new CPContact(data, parentProvider, parentGroup, false);
            mContacts.put(contact.getAddress(), contact);
        }
        catch (JSONException e)
        {
            sLog.error("Error parsing contact data", e);
        }

        return contact;
    }

    public void subscribe(String contactIdentifier)
    {
        subscribe(parentProvider.getGroup(), contactIdentifier);
    }

    public void subscribe(ContactGroup parent, String address)
    {
        sLog.info("Creating a contact from identifier " + address);

        // We don't actually send the contact to the server until edit is called
        // later. This is because we don't currently have enough info to send it
        // We still add it to the list of contacts however.
        // Note that there is a slight hole which could lead to a NPE: the newly
        // added contact may be deleted by a sync with the server if the sync
        // happens before the contact is sent.  However, this is a very small
        // hole so don't believe we will actually hit it in practice.
        CPContact contact = new CPContact(parent, address, parentProvider);
        mContacts.put(address, contact);

        fireSubscriptionEvent(contact,
                              parent,
                              SubscriptionEvent.SUBSCRIPTION_CREATED);
    }

    public void unsubscribe(final Contact contact)
    {
        // Despite the name, this is called when deleting a contact
        sLog.info("Deleting contact with name " + contact.getDisplayName());

        CPDataSenderCallback callback = new CPDataSenderCallback()
        {

            public void onDataError(CPDataError error)
            {
                sLog.error("Error sending contact deletion");
            }

            public String getSIName()
            {
                return SI_NAME;
            }

            public DataFormat getDataFormat()
            {
                return DataFormat.DATA_JS;
            }

            public String getData()
            {
                return "{\"_Action\":\"delete\",\"Contact\":[{\"UID\":\"" +
                                        ((CPContact)contact).getUID() + "\"}]}";
            }

            public void onDataSent()
            {
                // Don't need to do anything
            }
        };

        mCommPortalService.postServiceIndication(callback, null, true);
    }

    public void createServerStoredContactGroup(ContactGroup parent,
                                               String groupName)
        throws OperationFailedException
    {
        // Do nothing - we don't care about groups (at the moment)
    }

    public void removeServerStoredContactGroup(ContactGroup group)
    {
        // Do nothing - we don't care about groups (at the moment)
    }

    public void renameServerStoredContactGroup(ContactGroup group,
                                               String newName)
    {
        // Do nothing - we don't care about groups (at the moment)
    }

    public void moveContactToGroup(Contact contactToMove,ContactGroup newParent)
    {
        // Do nothing - we don't care about groups (at the moment)
    }

    public ContactGroup getServerStoredContactListRoot()
    {
        return parentProvider.getGroup();
    }

    public ContactGroup createUnresolvedContactGroup(String groupUID,
                                                     String persistentData,
                                                     ContactGroup parentGroup)
    {
        // Not required at the moment
        return null;
    }

    @Override
    public boolean isAddressDisplayable()
    {
        // CommPortal contact addresses are server generated UIDs, thus not
        // suitable for display
        return false;
    }
}
