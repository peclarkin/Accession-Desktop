package net.java.sip.communicator.impl.protocol.commportal.contacts;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;

public class CommPortalContactGroup implements ContactGroup
{

    /**
     * Determines whether this group has been resolved on the server.
     * Unresolved groups are groups that were available on previous runs and
     * that the meta contact list has stored. During all next runs, when
     * bootstrapping, the meta contact list would create these groups as
     * unresolved. Once a protocol provider implementation confirms that the
     * groups are still on the server, it would issue an event indicating that
     * the groups are now resolved.
     */
    private boolean mIsResolved = true;

    /** Protocol provider for which we are creating this group */
    private final ProtocolProviderService mProvider;

    /** Data handler which holds all the details of the contacts */
    private CommPortalContactDataHandler mDataHandler;

    /** The subgroups that this group has */
    private final ArrayList<ContactGroup> mSubGroups =
                                                  new ArrayList<ContactGroup>();

    public CommPortalContactGroup(ProtocolProviderService provider,
                                  CommPortalContactDataHandler dataHandler)
    {
        mProvider = provider;
        mDataHandler = dataHandler;
    }

    public Iterator<ContactGroup> subgroups()
    {
        return mSubGroups.iterator();
    }

    public int countSubgroups()
    {
        return mSubGroups.size();
    }

    public ContactGroup getGroup(int index)
    {
        // CommPortal does not support groups so return null.
        return null;
    }

    public ContactGroup getGroup(String groupName)
    {
        // CommPortal does not support groups so return null.
        return null;
    }

    public Iterator<Contact> contacts()
    {
        return mDataHandler.getAllContacts();
    }

    public int countContacts()
    {
        return mDataHandler.numberContacts();
    }

    public Contact getContact(String id)
    {
        return mDataHandler.findContactByID(id);
    }

    public boolean canContainSubgroups()
    {
        // CommPortal does not support subgroups
        return false;
    }

    public String getGroupName()
    {
        return "CommPortal";
    }

    public ProtocolProviderService getProtocolProvider()
    {
        return mProvider;
    }

    public ContactGroup getParentContactGroup()
    {
        // CommPortal does not support sub groups so there will only be one
        // CommPortal group. Therefore the parent group will always be null
        return null;
    }

    public boolean isPersistent()
    {
        return true;
    }

    public String getUID()
    {
        // We don't support subgroups thus we can use "CommPortal" as an
        // identifier
        return "CommPortal";
    }

    public boolean isResolved()
    {
        return mIsResolved;
    }

    public void setResolved(boolean resolved)
    {
        mIsResolved = resolved;
    }

    public String getPersistentData()
    {
        // No persistent data as we can construct the group from it's memebers
        return null;
    }

}
