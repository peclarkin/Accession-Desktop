/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.demuxcontactsource;

import java.util.*;
import java.util.regex.*;

import net.java.sip.communicator.service.contactsource.*;
import net.java.sip.communicator.service.protocol.*;

/**
 * The <tt>DemuxContactSource</tt> is a contact source that takes as parameter
 * another <tt>ContactSourceService</tt> and provides a demultiplexed result.
 * Every contact detail like telephone number or protocol contact address is
 * represented as a single entry in the query result.
 *
 * @author Yana Stamcheva
 */
public class DemuxContactSource
    implements ProtocolAwareContactSourceService
{
    /**
     * The underlying contact source service.
     */
    private final ContactSourceService contactSource;

    /**
     * The preferred protocol provider for this contact source.
     */
    private Map<Class<? extends OperationSet>, ProtocolProviderService>
        preferredProtocolProviders;

    /**
     * Create an instance of <tt>DemuxContactSource</tt> by specifying the
     * underlying <tt>ContactSourceService</tt> to be demuxed.
     *
     * @param contactSource the underlying <tt>ContactSourceService</tt> to be
     * demuxed
     */
    public DemuxContactSource(ContactSourceService contactSource)
    {
        this.contactSource = contactSource;
    }

    /**
     * Sets the preferred protocol provider for this contact source.
     *
     * @param protocolProvider the <tt>ProtocolProviderService</tt> to set
     */
    public void setPreferredProtocolProvider(
                                    Class<? extends OperationSet> opSetClass,
                                    ProtocolProviderService protocolProvider)
    {
        if (preferredProtocolProviders == null)
            preferredProtocolProviders
                = new HashMap<  Class<? extends OperationSet>,
                                ProtocolProviderService>();

        preferredProtocolProviders.put(opSetClass, protocolProvider);
    }

    /**
     * Returns the type of the underlying contact source.
     *
     * @return the type of this contact source
     */
    public int getType()
    {
        return contactSource.getType();
    }

    /**
     * Returns a user-friendly string that identifies the underlying contact
     * source.
     * 
     * @return the display name of this contact source
     */
    public String getDisplayName()
    {
        return contactSource.getDisplayName();
    }

    /**
     * Queries this search source for the given <tt>queryString</tt>.
     *
     * @param queryString the string to search for
     * @return the created query
     */
    public ContactQuery queryContactSource(String queryString)
    {
        if (contactSource instanceof ExtendedContactSourceService)
            return new DemuxContactQuery(
                ((ExtendedContactSourceService) contactSource)
                    .queryContactSource(Pattern.compile(
                        Pattern.quote(queryString),
                        Pattern.MULTILINE
                            | Pattern.CASE_INSENSITIVE
                            | Pattern.UNICODE_CASE)));
        else
            return new DemuxContactQuery(
                contactSource.queryContactSource(queryString));
    }

    /**
     * Queries this search source for the given <tt>queryString</tt>.
     *
     * @param queryString the string to search for
     * @param contactCount the maximum count of result contacts
     * @return the created query
     */
    public ContactQuery queryContactSource(String queryString, int contactCount)
    {
        return new DemuxContactQuery(
            contactSource.queryContactSource(queryString, contactCount));
    }

    /**
     * Returns the index of the contact source in the result list.
     *
     * @return the index of the contact source in the result list
     */
    public int getIndex()
    {
        return contactSource.getIndex();
    }

    /**
     * The <tt>DemuxContactQuery</tt> takes a <tt>ContactQuery</tt> as a
     * parameter and provides a demultiplexed result. Every contact detail like
     * telephone number or protocol contact address is represented as a
     * separate entry in the query result.
     */
    private class DemuxContactQuery
        extends AbstractContactQuery<ContactSourceService>
        implements ContactQueryListener
    {
        /**
         * The underlying query.
         */
        private final ContactQuery sourceQuery;

        /**
         * Creates an instance of <tt>DemuxContactQuery</tt>.
         *
         * @param sourceQuery the source <tt>ContactQuery</tt>
         */
        public DemuxContactQuery(ContactQuery sourceQuery)
        {
            super(DemuxContactSource.this);

            this.sourceQuery = sourceQuery;

            sourceQuery.addContactQueryListener(this);
        }

        /**
         * Gets the <tt>ContactSourceService</tt> which is performing this
         * <tt>ContactQuery</tt>.
         *
         * @return the <tt>ContactSourceService</tt> which is performing this
         * <tt>ContactQuery</tt>
         * @see ContactQuery#getContactSource()
         */
        public ContactSourceService getContactSource()
        {
            return DemuxContactSource.this;
        }

        /**
         * Returns the query string, this query was created for.
         *
         * @return the query string, this query was created for
         */
        public String getQueryString()
        {
            return sourceQuery.getQueryString();
        }

        /**
         * Returns the list of <tt>SourceContact</tt>s returned by this query.
         *
         * @return the list of <tt>SourceContact</tt>s returned by this query
         */
        public List<SourceContact> getQueryResults()
        {
            List<SourceContact> sourceContacts = sourceQuery.getQueryResults();

            if (sourceContacts == null)
                return null;

            List<SourceContact> newSourceContacts
                = new ArrayList<SourceContact>();

            Iterator<SourceContact> contactIter = sourceContacts.iterator();
            while (contactIter.hasNext())
            {
                SourceContact sourceContact = contactIter.next();

                Iterator<ContactDetail> detailsIter
                    = sourceContact.getContactDetails().iterator();

                while (detailsIter.hasNext())
                {
                    ContactDetail detail = detailsIter.next();

                    if (preferredProtocolProviders == null
                        || isPreferredContactDetail(detail))
                    {
                        newSourceContacts.add(
                            createSourceContact(sourceContact,
                                                detail));
                    }
                }
            }

            return newSourceContacts;
        }

        public void cancel()
        {
            sourceQuery.cancel();
        }

        /**
         * Returns the status of this query. One of the static constants
         * QUERY_XXXX defined in this class.
         *
         * @return the status of this query
         */
        public int getStatus()
        {
            return sourceQuery.getStatus();
        }

        /**
         * Indicates that a new contact has been received for a search.
         *
         * @param event the <tt>ContactQueryEvent</tt> containing information
         * about the received <tt>SourceContact</tt>
         */
        public void contactReceived(ContactReceivedEvent event)
        {
            SourceContact sourceContact = event.getContact();

            Iterator<ContactDetail> detailsIter
                = sourceContact.getContactDetails().iterator();

            while (detailsIter.hasNext())
            {
                ContactDetail detail = detailsIter.next();

                if (preferredProtocolProviders == null
                    || isPreferredContactDetail(detail))
                {
                    fireContactReceived(
                        createSourceContact(sourceContact, detail));
                }
            }
        }

        /**
         * Creates a single contact from the given <tt>sourceContact</tt> and
         * <tt>contactDetail</tt>.
         *
         * @param sourceContact the source contact
         * @param contactDetail the source contact detail
         * @return the created contact
         */
        private SourceContact createSourceContact(  SourceContact sourceContact,
                                                    ContactDetail contactDetail)
        {
            List<ContactDetail> contactDetails = new ArrayList<ContactDetail>();

            contactDetails.add(contactDetail);

            GenericSourceContact genericContact
                = new GenericSourceContact( DemuxContactSource.this,
                                            sourceContact.getDisplayName(),
                                            contactDetails);

            genericContact.setDisplayDetails(contactDetail.getContactAddress());

            return genericContact;
        }

        /**
         * Indicates that the status of a search has been changed.
         *
         * @param event the <tt>ContactQueryStatusEvent</tt> containing
         * information about the status change
         */
        public void queryStatusChanged(ContactQueryStatusEvent event)
        {
            fireQueryStatusChanged(event.getEventType());
        }

        public void contactRemoved(ContactRemovedEvent event) {}

        public void contactChanged(ContactChangedEvent event) {}
    }

    /**
     * Indicates if the given contact detail has a pair of OperationSet and
     * ProtocolProviderService that matches the preferred pairs indicated for
     * this contact source.
     *
     * @param c the <tt>ContactDetail</tt> to check
     * @return <tt>true</tt> if the given <tt>ContactDetail</tt> contains one
     * of the preferred pairs or has no preferred pairs,
     * <tt>false</tt> - otherwise.
     */
    private boolean isPreferredContactDetail(ContactDetail c)
    {
        Iterator<Class<? extends OperationSet>> preferredProviderOpSets
            = preferredProtocolProviders.keySet().iterator();

        while (preferredProviderOpSets.hasNext())
        {
            Class<? extends OperationSet> opSetClass
                = preferredProviderOpSets.next();

            ProtocolProviderService preferredProvider
                = c.getPreferredProtocolProvider(opSetClass);

            if (preferredProvider == null
                || preferredProvider.equals(
                    preferredProtocolProviders.get(opSetClass)))
            {
                return true;
            }
        }

        return false;
    }
}