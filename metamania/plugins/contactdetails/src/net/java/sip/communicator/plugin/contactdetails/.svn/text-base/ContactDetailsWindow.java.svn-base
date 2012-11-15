package net.java.sip.communicator.plugin.contactdetails;

import java.awt.*;
import java.awt.Container;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.OperationSetServerStoredUpdatableContactInfo.ContactUpdateResultListener;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.CountryDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.EmailAddress1Detail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.EmailAddress2Detail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.HomePhoneDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.IMDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.OtherPhoneDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.SMSDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.WorkCountryDetail;
import net.java.sip.communicator.service.protocol.PersonalContactDetails.WorkTitleDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.AddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.CityDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FaxDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.FirstNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.GenericDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.LastNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.MobilePhoneDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.NameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.NicknameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.PostalCodeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.ProvinceDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.StringDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkAddressDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkCityDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkOrganizationNameDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkPhoneDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkPostalCodeDetail;
import net.java.sip.communicator.service.protocol.ServerStoredDetails.WorkProvinceDetail;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

import org.apache.commons.lang3.*;

/**
 * Window for viewing contact details.  This contains a panel suitable for a
 * 'personal contact', IM 'buddy', or linked personal contact and buddy.
 *
 * Currently this only supports CommPortal contacts and Jabber contacts, with
 * up to one of each merged in a metacontact.  This will need to be changed
 * if/when we start supporting Outlook / Address Book contacts or other
 * chat etc protocols.
 */
public class ContactDetailsWindow extends SIPCommFrame
    implements ActionListener
{
    private static final long serialVersionUID = 1L;

    // String resources.
    private static final String DISPLAY_NAME_LABEL           = Resources.getString("plugin.contactdetails.editlabel.DISPLAY_NAME");
    private static final String NICKNAME_LABEL               = Resources.getString("plugin.contactdetails.editlabel.NICKNAME");
    private static final String JOB_TITLE_LABEL              = Resources.getString("plugin.contactdetails.editlabel.JOB_TITLE");
    private static final String ORG_LABEL                    = Resources.getString("plugin.contactdetails.editlabel.ORGANISATION");
    private static final String ADDRESS_LABEL                = Resources.getString("plugin.contactdetails.editlabel.ADDRESS");
    private static final String PHONE_NUMBER_LABEL           = Resources.getString("plugin.contactdetails.editlabel.PHONE_NUMBER");
    private static final String WORK_DETAIL_LABEL            = Resources.getString("plugin.contactdetails.editlabeldetail.WORK");
    private static final String HOME_DETAIL_LABEL            = Resources.getString("plugin.contactdetails.editlabeldetail.HOME");
    private static final String MOBILE_DETAIL_LABEL          = Resources.getString("plugin.contactdetails.editlabeldetail.MOBILE");
    private static final String OTHER_DETAIL_LABEL           = Resources.getString("plugin.contactdetails.editlabeldetail.OTHER");
    private static final String FAX_DETAIL_LABEL             = Resources.getString("plugin.contactdetails.editlabeldetail.FAX");
    private static final String SMS_DETAIL_LABEL             = Resources.getString("plugin.contactdetails.editlabeldetail.SMS");
    private static final String FIRST_NAME_GUIDANCE          = Resources.getString("plugin.contactdetails.guidance.FIRST_NAME");
    private static final String LAST_NAME_GUIDANCE           = Resources.getString("plugin.contactdetails.guidance.LAST_NAME");
    private static final String NICKNAME_GUIDANCE            = Resources.getString("plugin.contactdetails.guidance.NICKNAME");
    private static final String JOB_TITLE_GUIDANCE           = Resources.getString("plugin.contactdetails.guidance.JOB_TITLE");
    private static final String ORG_GUIDANCE                 = Resources.getString("plugin.contactdetails.guidance.ORGANISATION");
    private static final String PHONE_NUMBER_GUIDANCE        = Resources.getString("plugin.contactdetails.guidance.PHONE_NUMBER");
    private static final String EMAIL_GUIDANCE               = Resources.getString("plugin.contactdetails.guidance.EMAIL");
    private static final String IM_GUIDANCE                  = Resources.getString("plugin.contactdetails.guidance.IM");
    private static final String WORK_PHONE_READ_ONLY_LABEL   = Resources.getString("plugin.contactdetails.readonlylabel.PHONE_WORK");
    private static final String MOBILE_PHONE_READ_ONLY_LABEL = Resources.getString("plugin.contactdetails.readonlylabel.PHONE_MOBILE");
    private static final String HOME_PHONE_READ_ONLY_LABEL   = Resources.getString("plugin.contactdetails.readonlylabel.PHONE_HOME");
    private static final String OTHER_PHONE_READ_ONLY_LABEL  = Resources.getString("plugin.contactdetails.readonlylabel.PHONE_OTHER");
    private static final String SMS_PHONE_READ_ONLY_LABEL    = Resources.getString("plugin.contactdetails.readonlylabel.PHONE_SMS");
    private static final String FAX_PHONE_READ_ONLY_LABEL    = Resources.getString("plugin.contactdetails.readonlylabel.PHONE_FAX");
    private static final String HOME_ADDRESS_READ_ONLY_LABEL = Resources.getString("plugin.contactdetails.readonlylabel.ADDRESS_HOME");
    private static final String EMAIL_LABEL                  = Resources.getString("plugin.contactdetails.EMAIL");
    private static final String IM_LABEL                     = Resources.getString("plugin.contactdetails.IM");
    private static final String IM_PROVIDER_NONE             = Resources.getString("plugin.contactdetails.IM_PROVIDER_NONE");
    private static final String EDIT_WINDOW_TITLE            = Resources.getString("plugin.contactdetails.EDIT_WINDOW_TITLE");
    private static final String VIEW_WINDOW_TITLE            = Resources.getString("plugin.contactdetails.VIEW_WINDOW_TITLE");

    // String resource keys.  They are not loaded as resources here because we
    // will require the string and mnemonic for each.
    private static final String OK_KEY     = "service.gui.OK";
    private static final String EDIT_KEY   = "service.gui.EDIT";
    private static final String SAVE_KEY   = "service.gui.SAVE";
    private static final String CANCEL_KEY = "service.gui.CANCEL";

    // Image resource keys.  Given we may not require all of the images and
    // images use considerably more memory than string, for efficiency they
    // are not loaded here.
    private static final String DEFAULT_PHOTO_KEY      = "service.gui.DEFAULT_USER_PHOTO";
    private static final String VIDEO_BTN_KEY          = "service.gui.buttons.CALL_VIDEO_BUTTON_SMALL";
    private static final String VIDEO_BTN_ROLLOVER_KEY = "service.gui.buttons.CALL_VIDEO_BUTTON_SMALL_ROLLOVER";
    private static final String VIDEO_BTN_PRESSED_KEY  = "service.gui.buttons.CALL_VIDEO_BUTTON_SMALL_PRESSED";
    private static final String CLEAR_BTN_KEY          = "service.gui.buttons.CALL_VIDEO_BUTTON_SMALL";
    private static final String CLEAR_BTN_ROLLOVER_KEY = "service.gui.buttons.CALL_VIDEO_BUTTON_SMALL_ROLLOVER";
    private static final String CLEAR_BTN_PRESSED_KEY  = "service.gui.buttons.CALL_VIDEO_BUTTON_SMALL_PRESSED";

    // Array of guidance text strings for address fields.
    private static final String[] ADDRESS_GUIDANCE_TEXT = new String[]{
       Resources.getString("plugin.contactdetails.guidance.ADDRESS_STREET"),
       Resources.getString("plugin.contactdetails.guidance.ADDRESS_CITY"),
       Resources.getString("plugin.contactdetails.guidance.ADDRESS_REGION"),
       Resources.getString("plugin.contactdetails.guidance.ADDRESS_POST_CODE"),
       Resources.getString("plugin.contactdetails.guidance.ADDRESS_COUNTRY")};

    /**
     * Identifiers for the buttons controlling the window e.g. 'Cancel'
     */
    private enum controlButton
    {
        OK, EDIT, SAVE, CANCEL;

        private boolean equals(JButton button)
        {
            boolean isEqual = false;

            if (button != null)
            {
                isEqual = toString().equalsIgnoreCase(button.getName());
            }

            return isEqual;
        }
    }

    /**
     * Width of this window, in pixels
     */
    private static final int WINDOW_WIDTH = 400;

    /**
     * Size of padding in this window, in pixels
     */
    private static final int PADDING = 7;

    /**
     * Width of the 'OK', 'Save' etc buttons
     */
    private static final int BUTTON_WIDTH_PIXELS = 60;

    /**
     * The maximum width we want phone number buttons to be, in pixels.
     */
    private static final int MAX_PHONE_BTN_WIDTH = 120;

    /**
     * Value for R, G and B to produce a mid-grey
     */
    private static final int MID_GREY = 75;

    /**
     * Default inset padding, in pixels.
     */
    private static final int DEFAULT_INSET = 5;

    /**
     * GridBagConstraint identifier value for the name row this pane
     */
    private static final int NAME_ROW_INDEX = 0;

    /**
     * GridBagConstraint identifier value for the row that main details start
     * on, under the name.
     */
    private static final int MAIN_DETAILS_ROW_INDEX = 10;

    /**
     * GridBagConstraint identifier value for the row that the work address
     * starts on.
     */
    private static final int WORK_ADDRESS_ROW_INDEX = 20;

    /**
     * GridBagConstraint identifier value for the row that the home address
     * starts on.
     */
    private static final int HOME_ADDRESS_ROW_INDEX = 30;

    /**
     * GridBagConstraint identifier value for the row that phone numbers start
     * on.
     */
    private static final int PHONE_NUMBER_ROW_INDEX = 40;

    /**
     * GridBagConstraint identifier value for the row for the first email
     * address.
     */
    private static final int EMAIL1_ROW_INDEX = 50;

    /**
     * GridBagConstraint identifier value for the row for the second email
     * address.
     */
    private static final int EMAIL2_ROW_INDEX = 60;

    /**
     * GridBagConstraint identifier value for the row for the IM address.
     */
    private static final int IM_ROW_INDEX = 70;

    /**
     * GridBagConstraint identifier value for the left column of this pane
     */
    private static final int LEFT_COLUMN = 0;

    /**
     * GridBagConstraint identifier value for the centre column of this pane
     * when in read-only mode.
     */
    private static final int READ_ONLY_CENTRE_COLUMN = 1;

    /**
     * GridBagConstraint identifier value for the right column of this pane
     * when in read-only mode.
     */
    private static final int READ_ONLY_RIGHT_COLUMN = 2;

    /**
     * GridBagConstraint identifier value for the dropdowns column of this
     * pane when in edit mode
     */
    private static final int DROPDOWN_COLUMN = 1;

    /**
     * GridBagConstraint identifier value for the column to the right of
     * dropdowns when in edit mode
     */
    private static final int POST_DROPDOWN_COLUMN = 2;

    /**
     * GridBagConstraint identifier value for the 'last name' field column of
     * this pane when in edit mode
     */
    private static final int LAST_NAME_COLUMN = 3;

    /**
     * GridBagConstraint identifier value for the 'clear' button column of
     * this pane when in edit mode
     */
    private static final int DELETE_BUTTON_COLUMN = 4;

    // Width and height of the contact photo, in pixels.
    private static final int PHOTO_WIDTH = 75;
    private static final int PHOTO_HEIGHT = 75;

    /**
     * Whether the 'Edit' window should be closed when a button is pressed -
     * we return to a 'View' window if we came from one originally.
     */
    private final boolean fReturnToViewPane;

    /**
     * The main content pane for this window
     */
    private final Container mContentPane = getContentPane();

    /**
     * The panel showing contact details
     */
    private final TransparentPanel mDetailsPanel = new TransparentPanel();

    /**
     * The panel with the OK/Cancel and Save/Edit buttons
     */
    private final TransparentPanel mButtonsPanel = new TransparentPanel();

    /**
     * Personal contact displayed in this panel (null for a Jabber-only
     * contact)
     */
    private Contact mPersonalContact;

    /**
     * IM buddy displayed in this panel (null for a SIP-only contact)
     */
    private Contact mIMBuddy;

    /**
     * Operation set for read access to server stored details
     */
    private OperationSetServerStoredContactInfo mContactInfoOpSet = null;

    /**
     * Operation set for write access to server stored details
     */
    private OperationSetServerStoredUpdatableContactInfo
                                             mUpdatableContactInfoOpSet = null;

    /**
     * Whether this window is in read-only mode (i.e. 'View', not 'Edit' mode)
     */
    private boolean mReadOnly;

    // Text fields used in 'Edit' mode
    private SIPCommTextField mFirstName;
    private SIPCommTextField mLastName;
    private SIPCommTextField mNickname;
    private SIPCommTextField mJobTitle;
    private SIPCommTextField mOrganisation;
    private SIPCommTextField mWorkPhone;
    private SIPCommTextField mMobilePhone;
    private SIPCommTextField mHomePhone;
    private SIPCommTextField mOtherPhone;
    private SIPCommTextField mFaxPhone;
    private SIPCommTextField mSMSPhone;
    private SIPCommTextField mEmail1;
    private SIPCommTextField mEmail2;
    private SIPCommTextField mIMAddress;
    private SIPCommTextField[] mWorkAddressLines;
    private SIPCommTextField[] mHomeAddressLines;

    /**
     * Create a 'View contact' window, fill with contact details, and show it
     * @param metaContact   The contact detailed in this window
     * @param readOnly   Whether this window is in read-only mode (i.e.
     * 'View', not 'Edit' mode)
     */
    public ContactDetailsWindow(MetaContact metaContact,
                                boolean readOnly)
    {
        super();
        mReadOnly = readOnly;

        // If this window is first made as a 'View' window, we should always
        // return to the 'View' window after editing the contact
        fReturnToViewPane = mReadOnly;

        // Identify the 'personal contact' and 'IM buddy' from the metacontact.
        // We are only supporting SIP telephony accounts (with CommPortal /
        // Outlook contacts) and Jabber IM accounts.
        mPersonalContact = getSubcontact(metaContact, "CommPortal");
        mIMBuddy         = getSubcontact(metaContact, ProtocolNames.JABBER);

        // Get the operation sets for reading and writing contact information
        // for this personal contact
        if (mPersonalContact != null)
        {
            ProtocolProviderService provider =
                                        mPersonalContact.getProtocolProvider();
            mContactInfoOpSet = provider.getOperationSet(
                                    OperationSetServerStoredContactInfo.class);
            mUpdatableContactInfoOpSet = provider.getOperationSet(
                           OperationSetServerStoredUpdatableContactInfo.class);
        }

        // Initialise the window with all the contact details and buttons.
        initWindow();

        // Display the window
        setResizable(false);
        setVisible(true);
        pack();
    }

    /**
     * Sets up the window with contact details and the buttons to go in the
     * footer.
     */
    private void initWindow()
    {
        // Set the window title based on the window mode.
        String resourceKey = mReadOnly ? VIEW_WINDOW_TITLE : EDIT_WINDOW_TITLE;
        setTitle(resourceKey);

        // Set up the details and buttons panels
        initDetailsPanel();
        initButtonsPanel();

        // Lay the window out again.
        mContentPane.removeAll();
        mContentPane.setLayout(new BoxLayout(mContentPane, BoxLayout.Y_AXIS));

        mContentPane.add(mDetailsPanel);
        mContentPane.add(new JSeparator());
        mContentPane.add(mButtonsPanel);

        // Lay the window out.  It is important to set preferred size to null
        // before setting the width, as this way the preferred height will be
        // correctly determined.
        pack();
        setPreferredSize(null);
        setPreferredSize(new Dimension(WINDOW_WIDTH,
                                       getPreferredSize().height));
    }

    /**
     * Set the layout and add components to the contact details panel.
     */
    private void initDetailsPanel()
    {
        // A gridbag layout is used for adding UI elements in a grid, where
        // some can take multiple rows / columns.
        GridBagLayout layout = new GridBagLayout();

        // Set a minimum width for the left-most column, so that it doesn't
        // change width depending on which fields are visible.
        layout.columnWidths = new int[]{100};
        mDetailsPanel.setLayout(layout);

        // We want the padding from the window edge to be consistent with
        // other components in the window, so add a border to the layout,
        // remembering that every component within the layout will have insets.
        int borderWidth = PADDING - DEFAULT_INSET;
        mDetailsPanel.setBorder(BorderFactory.createEmptyBorder(borderWidth,
                                                                borderWidth,
                                                                borderWidth,
                                                                borderWidth));

        // Load and add each field to the panel
        addFields();

        // Redraw the panel's components
        mDetailsPanel.revalidate();
        mDetailsPanel.repaint();
    }

    /**
     * Initialise the panel containing the OK/Cancel and Save/Edit buttons.
     */
    private void initButtonsPanel()
    {
        mButtonsPanel.removeAll();
        mButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton closeButton;
        JButton changeStateButton;

        if (mReadOnly)
        {
            // Read only mode:  Add an 'OK' and an 'Edit' button
            closeButton       = createButton(OK_KEY,   controlButton.OK);
            changeStateButton = createButton(EDIT_KEY, controlButton.EDIT);
        }
        else
        {
            // Edit mode:  Add a 'Cancel' and a 'Save' button
            closeButton       = createButton(CANCEL_KEY, controlButton.CANCEL);
            changeStateButton = createButton(SAVE_KEY,   controlButton.SAVE);
        }

        mButtonsPanel.add(closeButton);
        mButtonsPanel.add(changeStateButton);
    }

    /**
     * Add all the fields we wish to be displayed in this panel
     */
    private void addFields()
    {
        mDetailsPanel.removeAll();

        addName();
        addMainDetails();
        addPhoto();
        addAddressDetails();
        addPhoneNumbers();
        addEmailDetails();
        addIMDetail();
    }

    /**
     * Add the display name
     */
    private void addName()
    {
        if (mReadOnly)
        {
            // Create a label for the name, with larger font than the rest of
            // the panel.
            JLabel name = new JLabel(getNameDetail());
            name.setFont(name.getFont().deriveFont(Font.BOLD).deriveFont(16F));
            name.setForeground(Color.DARK_GRAY);

            // Add the name to the panel.
            GridBagConstraints constraints = getPanelGridBagConstraints(
                                                               NAME_ROW_INDEX);
            constraints.gridx = READ_ONLY_CENTRE_COLUMN;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.weightx = 1;
            constraints.gridwidth = 2;
            mDetailsPanel.add(name, constraints);
        }
        else
        {
            // Create the field's label.
            JLabel label = new JLabel(DISPLAY_NAME_LABEL);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setForeground(Color.DARK_GRAY);

            // Add the label to the panel.
            GridBagConstraints constraints = getPanelGridBagConstraints(
                                                               NAME_ROW_INDEX);
            constraints.gridx = LEFT_COLUMN;
            constraints.anchor = GridBagConstraints.EAST;
            mDetailsPanel.add(label, constraints);

            // Create a text field for the first name.
            mFirstName = new SIPCommTextField(
                                                     FIRST_NAME_GUIDANCE);
            mFirstName.setText(getDetail(FirstNameDetail.class));

            // Add the field to the panel.
            constraints = getPanelGridBagConstraints(NAME_ROW_INDEX);
            constraints.gridx = DROPDOWN_COLUMN;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridwidth = 2;
            constraints.weightx = 0.5;
            mDetailsPanel.add(mFirstName, constraints);

            // Create a text field for the last name.
            mLastName = new SIPCommTextField(LAST_NAME_GUIDANCE);
            mLastName.setText(getDetail(LastNameDetail.class));

            // Add the field to the panel.
            constraints = getPanelGridBagConstraints(NAME_ROW_INDEX);
            constraints.gridx = LAST_NAME_COLUMN;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 0.5;
            mDetailsPanel.add(mLastName, constraints);

            // Mysteriously, we have to do this to make sure both name text
            // fields are the same size (which they should be just with the
            // constraints applied).
            mFirstName.setColumns(10);
            mLastName.setColumns(10);

            // Create a button to clear the text fields.
            SIPCommTextField[] nameTextFields = {mFirstName,
                                                 mLastName};

            // Add the button to the panel.
            constraints = getPanelGridBagConstraints(NAME_ROW_INDEX);
            constraints.gridx = DELETE_BUTTON_COLUMN;
            constraints.insets = new Insets(0, DEFAULT_INSET, 0, DEFAULT_INSET);
            mDetailsPanel.add(createClearButton(nameTextFields), constraints);
        }
    }

    /**
     * Add the details to go next to the contact photo.  This includes
     * nickname, job title, organisation, and (when in read-only mode) work
     * address.
     */
    private void addMainDetails()
    {
        String nickname     = getDetail(NicknameDetail.class);
        String jobTitle     = getDetail(WorkTitleDetail.class);
        String organisation = getDetail(WorkOrganizationNameDetail.class);

        if (mReadOnly)
        {
            @SuppressWarnings("unchecked")
            Class<? extends StringDetail>[] workAddressFields =
                                        new Class[]{WorkAddressDetail.class,
                                                    WorkCityDetail.class,
                                                    WorkProvinceDetail.class,
                                                    WorkPostalCodeDetail.class,
                                                    WorkCountryDetail.class};

            // This is the desired order of other 'main' details - missing
            // details don't change the order of other numbers
            addReadOnlyMainDetail(nickname);
            addReadOnlyMainDetail(jobTitle);
            addReadOnlyMainDetail(organisation);
            addReadOnlyMainDetail(getCompoundDetail(workAddressFields, ", "));
        }
        else
        {
            // Add the 'main' details on sequential rows.
            int row = MAIN_DETAILS_ROW_INDEX;
            mNickname     = addEditableMainDetail(nickname,
                                                  NICKNAME_LABEL,
                                                  NICKNAME_GUIDANCE,
                                                  row++);
            mJobTitle     = addEditableMainDetail(jobTitle,
                                                  JOB_TITLE_LABEL,
                                                  JOB_TITLE_GUIDANCE,
                                                  row++);
            mOrganisation = addEditableMainDetail(organisation,
                                                  ORG_LABEL,
                                                  ORG_GUIDANCE,
                                                  row++);
        }
    }

    /**
     * Add a 'main' detail to the panel, in read-only mode.  'Main' refers to
     * the details that appear under the name and to the right of the contact
     * photo.
     *
     * @param detail   The string value of the contact's detail
     */
    private void addReadOnlyMainDetail(String detail)
    {
        if (StringUtils.isNotBlank(detail))
        {
            // Create a label for the detail.
            JLabel valueLabel = new JLabel(detail);
            valueLabel.setForeground(new Color(MID_GREY, MID_GREY, MID_GREY));

            // Add the label to the panel
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(0,
                                            DEFAULT_INSET,
                                            0,
                                            DEFAULT_INSET);
            constraints.gridx = READ_ONLY_CENTRE_COLUMN;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.weightx = 1;
            constraints.gridwidth = 2;
            mDetailsPanel.add(valueLabel, constraints);
        }
    }

    /**
     * Add a 'main' detail to the panel, in edit mode.  'Main' refers to the
     * the details that appear under name but before addresses (e.g. phone,
     * postal, email)
     *
     * @param detail   The string value of the contact's detail
     * @param labelText   The text for the detail's description label
     * @param guidanceText   The text to guide the user, appearing in the text
     * field when it's otherwise blank
     * @param row   This field's row number in the panel's layout
     */
    private SIPCommTextField addEditableMainDetail(String detail,
                                                   String labelText,
                                                   String guidanceText,
                                                   int row)
    {
        // Create a description label.
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(Color.DARK_GRAY);

        // Add the label to the panel.
        GridBagConstraints constraints = getPanelGridBagConstraints(row);
        constraints.gridx = LEFT_COLUMN;
        constraints.anchor = GridBagConstraints.EAST;
        mDetailsPanel.add(label, constraints);

        // Create a textbox for the detail.
        final SIPCommTextField fTextField = new SIPCommTextField(guidanceText);
        fTextField.setText(detail);

        // Add the textbox to the panel.
        constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DROPDOWN_COLUMN;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        mDetailsPanel.add(fTextField, constraints);

        // Add a 'clear' button for the text field.
        constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DELETE_BUTTON_COLUMN;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, DEFAULT_INSET, 0, DEFAULT_INSET);
        mDetailsPanel.add(createClearButton(fTextField), constraints);

        return fTextField;
    }

    /**
     * Add the contact's image
     */
    private void addPhoto()
    {
        if (mReadOnly)
        {
            // Only a Jabber contact can have a photo.
            byte[] imageBytes = null;

            if (mIMBuddy != null)
            {
                imageBytes = mIMBuddy.getImage();
            }

            // Scale and crop the corners of the image.
            ImageIcon scaledImage;

            if ((imageBytes != null) && (imageBytes.length > 0))
            {
                scaledImage = ImageUtils.getScaledRoundedIcon(imageBytes,
                                                              PHOTO_WIDTH,
                                                              PHOTO_HEIGHT);
            }
            else
            {
                // Use a default image if there is no contact photo.
                scaledImage = ImageUtils.getScaledRoundedIcon(
                                         Resources.getImage(DEFAULT_PHOTO_KEY),
                                                            PHOTO_WIDTH,
                                                            PHOTO_HEIGHT);
            }

            JLabel photo = new JLabel(scaledImage);
            photo.setPreferredSize(new Dimension(PHOTO_WIDTH, PHOTO_HEIGHT));

            // Set the photo:
            // To go in the top left cell of the grid
            // To float to the top right of that cell
            // To occupy (but not expand to fill) all the rows down to the
            // addresses
            GridBagConstraints constraints = getPanelGridBagConstraints(
                                                               NAME_ROW_INDEX);
            constraints.gridx = LEFT_COLUMN;
            constraints.anchor = GridBagConstraints.NORTHEAST;
            constraints.gridheight = WORK_ADDRESS_ROW_INDEX;
            mDetailsPanel.add(photo, constraints);
        }
    }

    /**
     * Add the contact's postal addresses.
     */
    private void addAddressDetails()
    {
        if (mReadOnly)
        {
            // In read-only mode, we only add the home address here (the work
            // address is included in 'main' details).
            @SuppressWarnings("unchecked")
            Class<? extends StringDetail>[] homeAddressFields =
                                            new Class[]{AddressDetail.class,
                                                        CityDetail.class,
                                                        ProvinceDetail.class,
                                                        PostalCodeDetail.class,
                                                        CountryDetail.class};

            // Build a string of the contact's whole home address (i.e.
            // Street, City, County etc)
            String compoundHomeAddress = getCompoundDetail(homeAddressFields,
                                                           ", ");

            if (StringUtils.isNotBlank(compoundHomeAddress))
            {
                addAddressLabel(HOME_ADDRESS_READ_ONLY_LABEL,
                                HOME_ADDRESS_ROW_INDEX);

                // Create a label object for the address.
                JLabel value = new JLabel(compoundHomeAddress);
                value.setForeground(new Color(MID_GREY, MID_GREY, MID_GREY));

                // Add the address to the panel.
                GridBagConstraints constraints = getPanelGridBagConstraints(
                                                       HOME_ADDRESS_ROW_INDEX);
                constraints.gridx = READ_ONLY_CENTRE_COLUMN;
                constraints.anchor = GridBagConstraints.WEST;
                constraints.weightx = 1;
                constraints.gridwidth = 2;
                mDetailsPanel.add(value, constraints);
            }
        }
        else
        {
            addAddressLabel(ADDRESS_LABEL, WORK_ADDRESS_ROW_INDEX);

            String[] workAddressLines = {getDetail(WorkAddressDetail.class),
                                         getDetail(WorkCityDetail.class),
                                         getDetail(WorkProvinceDetail.class),
                                         getDetail(WorkPostalCodeDetail.class),
                                         getDetail(WorkCountryDetail.class)};
            String[] homeAddressLines = {getDetail(AddressDetail.class),
                                         getDetail(CityDetail.class),
                                         getDetail(ProvinceDetail.class),
                                         getDetail(PostalCodeDetail.class),
                                         getDetail(CountryDetail.class)};

            // Add the work address on the starting address row.
            mWorkAddressLines = addEditableAddressDetails(
                                                       workAddressLines,
                                                       WORK_DETAIL_LABEL,
                                                       WORK_ADDRESS_ROW_INDEX);

            // Add the home address 10 lines lower, leaving space for all the
            // address fields in the work address.
            mHomeAddressLines = addEditableAddressDetails(
                                                       homeAddressLines,
                                                       HOME_DETAIL_LABEL,
                                                       HOME_ADDRESS_ROW_INDEX);
        }
    }

    /**
     * Add a description label for addresses.
     *
     * @param labelText   The text for the detail's description label
     * @param row   This field's row number in the panel's layout
     */
    private void addAddressLabel(String labelText, int row)
    {
        // Create a description label.
        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(Color.DARK_GRAY);

        // Add the label to the first address line.
        GridBagConstraints constraints = getPanelGridBagConstraints(row);
        constraints.gridx = LEFT_COLUMN;
        constraints.anchor = GridBagConstraints.EAST;
        mDetailsPanel.add(label, constraints);
    }

    /**
     * Add all the contact's work OR home address details (i.e. the street,
     * city, region etc for one address) to the panel, in edit mode.  The text
     * fields are laid out on sequential rows and can all be cleared with a
     * single 'clear' button.
     *
     * @param addressDetails   A string array of the address lines
     * @param labelText   The text for the detail's description label
     * @param row   This field's row number in the panel's layout
     */
    private SIPCommTextField[] addEditableAddressDetails(
                                                       String[] addressDetails,
                                                       String labelText,
                                                       int row)
    {
        // Create description label for the type of address.
        JLabel dropdown = new JLabel(labelText);
        dropdown.setFont(dropdown.getFont().deriveFont(Font.BOLD));
        dropdown.setForeground(Color.DARK_GRAY);

        // Add the label to the panel.  It lies on the same row as the first
        // line of the address.
        GridBagConstraints constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DROPDOWN_COLUMN;
        constraints.anchor = GridBagConstraints.WEST;
        mDetailsPanel.add(dropdown, constraints);

        // Create and add the 'clear' button (this is done before adding the
        // address lines so that the row is correct).
        SIPCommTextField[] addressTextFields =
                                   new SIPCommTextField[addressDetails.length];
        constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DELETE_BUTTON_COLUMN;
        constraints.insets = new Insets(0, DEFAULT_INSET, 0, DEFAULT_INSET);
        mDetailsPanel.add(createClearButton(addressTextFields), constraints);

        for (int ii = 0; ii < addressDetails.length; ii++)
        {
            // Create a textbox for the address line, and add it to the array
            // to be cleared by the 'clear' button.
            SIPCommTextField textField = new SIPCommTextField(
                                                    ADDRESS_GUIDANCE_TEXT[ii]);
            textField.setText(addressDetails[ii]);
            addressTextFields[ii] = textField;

            // Add the text box to the panel.
            constraints = getPanelGridBagConstraints(row);
            constraints.gridx = POST_DROPDOWN_COLUMN;
            constraints.gridwidth = 2;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            mDetailsPanel.add(textField, constraints);

            row++;
        }

        return addressTextFields;
    }

    /**
     * Add the contact's phone numbers.
     */
    private void addPhoneNumbers()
    {
        int row = PHONE_NUMBER_ROW_INDEX;

        String work   = getDetail(WorkPhoneDetail.class);
        String mobile = getDetail(MobilePhoneDetail.class);
        String home   = getDetail(HomePhoneDetail.class);
        String other  = getDetail(OtherPhoneDetail.class);
        String fax    = getDetail(FaxDetail.class);
        String sms    = getDetail(SMSDetail.class);

        if (mReadOnly)
        {
            // This is the desired order of phone numbers.  For any number
            // that is blank, the row will take zero space.
            addReadOnlyPhoneNumber(work,
                                   WORK_PHONE_READ_ONLY_LABEL,
                                   true,
                                   row++);
            addReadOnlyPhoneNumber(mobile,
                                   MOBILE_PHONE_READ_ONLY_LABEL,
                                   true,
                                   row++);
            addReadOnlyPhoneNumber(home,
                                   HOME_PHONE_READ_ONLY_LABEL,
                                   true,
                                   row++);
            addReadOnlyPhoneNumber(other,
                                   OTHER_PHONE_READ_ONLY_LABEL,
                                   true,
                                   row++);
            addReadOnlyPhoneNumber(fax,
                                   FAX_PHONE_READ_ONLY_LABEL,
                                   false,
                                   row++);
            addReadOnlyPhoneNumber(sms,
                                   SMS_PHONE_READ_ONLY_LABEL,
                                   false,
                                   row++);
        }
        else
        {
            // Create a description label for phone numbers
            JLabel label = new JLabel(PHONE_NUMBER_LABEL);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setForeground(Color.DARK_GRAY);

            // Add the label on the same row as the first phone number
            // textfield.
            GridBagConstraints constraints = getPanelGridBagConstraints(row);
            constraints.gridx = LEFT_COLUMN;
            constraints.anchor = GridBagConstraints.EAST;
            mDetailsPanel.add(label, constraints);

            // Add textfields for all the phone number.
            mWorkPhone   = addEditablePhoneNumber(work,
                                                  WORK_DETAIL_LABEL,
                                                  row++);
            mMobilePhone = addEditablePhoneNumber(mobile,
                                                  MOBILE_DETAIL_LABEL,
                                                  row++);
            mHomePhone   = addEditablePhoneNumber(home,
                                                  HOME_DETAIL_LABEL,
                                                  row++);
            mOtherPhone  = addEditablePhoneNumber(other,
                                                  OTHER_DETAIL_LABEL,
                                                  row++);
            mFaxPhone    = addEditablePhoneNumber(fax,
                                                  FAX_DETAIL_LABEL,
                                                  row++);
            mSMSPhone    = addEditablePhoneNumber(sms,
                                                  SMS_DETAIL_LABEL,
                                                  row++);
        }
    }

    /**
     * Add a phone number to the panel, in read-only mode.  The phone number
     * is clickable to start a call, and there is also a video call button for
     * numbers that can be video called (i.e. not fax or SMS numbers)
     *
     * @param detail   The string value of the contact's detail
     * @param labelText   The text for the detail's description label
     * @param guidanceText   The text to guide the user, appearing in the text
     * field when it's otherwise blank
     * @param row   This field's row number in the panel's layout
     */
    private void addReadOnlyPhoneNumber(String value,
                                        String labelText,
                                        boolean videoPossible,
                                        int row)
    {
        if (StringUtils.isNotBlank(value))
        {
            // Make the phone number final so that the button's action
            // listener can reference it.
            final String fNumber = value;

            // Create a description label.
            JLabel fieldName = new JLabel(labelText);
            fieldName.setFont(fieldName.getFont().deriveFont(Font.BOLD));
            fieldName.setForeground(Color.DARK_GRAY);

            // Add the label to the panel.
            GridBagConstraints constraints = getPanelGridBagConstraints(row);
            constraints.gridx = LEFT_COLUMN;
            constraints.anchor = GridBagConstraints.EAST;
            mDetailsPanel.add(fieldName, constraints);

            // Create a button for the phone number.
            JButton phoneBtn = new JButton(fNumber);
            phoneBtn.setFont(phoneBtn.getFont().deriveFont(Font.BOLD));
            phoneBtn.setForeground(Color.DARK_GRAY);

            // Make sure that no button is wider than our chosen maximum width
            // for phone buttons.
            int width = Math.min(phoneBtn.getPreferredSize().width,
                                 MAX_PHONE_BTN_WIDTH);
            int height = phoneBtn.getPreferredSize().height;
            phoneBtn.setPreferredSize(new Dimension(width, height));

            // Add a listener to start an audio call when the button is
            // clicked.
            phoneBtn.addActionListener(new ActionListener()
            {
                @SuppressWarnings("unused")
                public void actionPerformed(ActionEvent event)
                {
                    // Audio calling could be implemented in the same way as
                    // chat (see addReadOnlyIM) but video calling cannot,
                    // so we will use the CreateCallThread for now.
                    CreateCallThread.createCall(fNumber, false);
                }
            });

            // By setting the fill to horizontal, we can make sure that
            // when they are added, all phone number buttons are the same
            // width as the widest button.
            constraints = getPanelGridBagConstraints(row);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridx = READ_ONLY_CENTRE_COLUMN;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = new Insets(0, DEFAULT_INSET, 0, DEFAULT_INSET);
            mDetailsPanel.add(phoneBtn, constraints);

            if (videoPossible)
            {
                // Create a video call button.
                Image backgroundImg = Resources.getImage(VIDEO_BTN_KEY);
                Image rolloverImg = Resources.getImage(VIDEO_BTN_ROLLOVER_KEY);
                Image pressedImg = Resources.getImage(VIDEO_BTN_PRESSED_KEY);
                SIPCommButton videoBtn = new SIPCommButton(backgroundImg,
                                                           rolloverImg,
                                                           pressedImg,
                                                           null,
                                                           null,
                                                           null);
                videoBtn.setFont(videoBtn.getFont().deriveFont(Font.BOLD));
                videoBtn.setForeground(Color.DARK_GRAY);

                // Add a listener to start a video call when the button is
                // clicked.
                videoBtn.addActionListener(new ActionListener()
                {
                    @SuppressWarnings("unused")
                    public void actionPerformed(ActionEvent e)
                    {
                        // Video calling cannot be implemented in the same way
                        // as chat (see addReadOnlyIM) so we will use the
                        // CreateCallThread.
                        CreateCallThread.createCall(fNumber, true);
                    }
                });

                // Add the button to right of the audio call button.
                constraints = getPanelGridBagConstraints(row);
                constraints.gridx = READ_ONLY_RIGHT_COLUMN;
                constraints.fill = GridBagConstraints.NONE;
                constraints.anchor = GridBagConstraints.WEST;
                constraints.insets = new Insets(0, DEFAULT_INSET, 0, DEFAULT_INSET);
                mDetailsPanel.add(videoBtn, constraints);
            }
        }
    }

    /**
     * Add a phone number to the panel, in edit mode.
     *
     * @param detail   The string value of the contact's detail
     * @param labelText   The text for the detail's description label
     * @param row   This field's row number in the panel's layout
     */
    private SIPCommTextField addEditablePhoneNumber(String detail, String labelText, int row)
    {
        // Create a description label for the type of phone number.
        JLabel dropdown = new JLabel(labelText);
        dropdown.setFont(dropdown.getFont().deriveFont(Font.BOLD));
        dropdown.setForeground(Color.DARK_GRAY);

        // Add the label to the panel.
        GridBagConstraints constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DROPDOWN_COLUMN;
        constraints.anchor = GridBagConstraints.WEST;
        mDetailsPanel.add(dropdown, constraints);

        // Create a textfield for the detail.
        SIPCommTextField textField = new SIPCommTextField(
                                                        PHONE_NUMBER_GUIDANCE);
        textField.setText(detail);

        // Add the textfield to the panel.
        constraints = getPanelGridBagConstraints(row);
        constraints.gridx = POST_DROPDOWN_COLUMN;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        mDetailsPanel.add(textField, constraints);

        // Add a 'clear' button for the text field.
        constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DELETE_BUTTON_COLUMN;
        constraints.insets = new Insets(0, DEFAULT_INSET, 0, DEFAULT_INSET);
        mDetailsPanel.add(createClearButton(textField), constraints);

        return textField;
    }

    /**
     * Add the contact's email addresses.
     */
    private void addEmailDetails()
    {
        String email1 = getDetail(EmailAddress1Detail.class);
        String email2 = getDetail(EmailAddress2Detail.class);

        if (mReadOnly)
        {
            // Add a description label for the first non-blank email.
            if (StringUtils.isNotBlank(email1))
            {
                addEmailLabel(EMAIL1_ROW_INDEX);
            }
            else if (StringUtils.isNotBlank(email2))
            {
                addEmailLabel(EMAIL2_ROW_INDEX);
            }

            // Add the email fields.
            addReadOnlyEmailDetail(email1, EMAIL1_ROW_INDEX);
            addReadOnlyEmailDetail(email2, EMAIL2_ROW_INDEX);

        }
        else
        {
            // Add a label and the email fields.
            addEmailLabel(EMAIL1_ROW_INDEX);
            mEmail1 = addEditableEmailDetail(email1, EMAIL1_ROW_INDEX);
            mEmail2 = addEditableEmailDetail(email2, EMAIL2_ROW_INDEX);
        }
    }

    /**
     * Add a description label for email addresses.
     *
     * @param row   This field's row number in the panel's layout
     */
    private void addEmailLabel(int row)
    {
        // Create a description label for email addresses.
        GridBagConstraints constraints = getPanelGridBagConstraints(row);
        constraints.gridx = LEFT_COLUMN;
        constraints.anchor = GridBagConstraints.EAST;

        // Add the label to the panel.
        JLabel label = new JLabel(EMAIL_LABEL);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(Color.DARK_GRAY);
        mDetailsPanel.add(label, constraints);
    }

    /**
     * Add an email address to the panel, in edit mode.
     *
     * @param detail   The string value of the contact's detail
     * @param row   This field's row number in the panel's layout
     */
    private SIPCommTextField addEditableEmailDetail(String address, int row)
    {
        // Create a textfield for the address.
        SIPCommTextField textField = new SIPCommTextField(EMAIL_GUIDANCE);
        textField.setText(address);

        // Add the textfield to the panel.
        GridBagConstraints constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DROPDOWN_COLUMN;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        mDetailsPanel.add(textField, constraints);

        // Add a 'clear' button for the text field.
        constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DELETE_BUTTON_COLUMN;
        constraints.insets = new Insets(0, DEFAULT_INSET, 0, DEFAULT_INSET);
        mDetailsPanel.add(createClearButton(textField), constraints);

        return textField;
    }

    /**
     * Add an email address to the panel, in read-only mode.
     *
     * @param address   The string value of the contact's email address
     * @param row   This field's row number in the panel's layout
     */
    private void addReadOnlyEmailDetail(String address, int row)
    {
        if (StringUtils.isNotBlank(address))
        {
            // Create a label object for the email address.
            JLabel value = new JLabel(address);
            value.setForeground(new Color(MID_GREY, MID_GREY, MID_GREY));

            // Sdd the email address text to the panel.
            GridBagConstraints constraints = getPanelGridBagConstraints(row);
            constraints.anchor = GridBagConstraints.WEST;
            constraints.gridx = READ_ONLY_CENTRE_COLUMN;
            constraints.weightx = 1;
            constraints.gridwidth = 2;
            mDetailsPanel.add(value, constraints);
        }
    }

    /**
     * Add the contact's IM address.
     */
    private void addIMDetail()
    {
        if (mReadOnly)
        {
            addReadOnlyIM(IM_ROW_INDEX);
        }
        else
        {
            mIMAddress = addEditableIM(IM_ROW_INDEX);
        }
    }

    /**
     * Add a description label for the IM address.
     *
     * @param row   This field's row number in the panel's layout
     */
    private void addIMLabel(int row)
    {
        // Create a description label for the IM address.
        JLabel label = new JLabel(IM_LABEL);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(Color.DARK_GRAY);

        // Add the label to the panel.
        GridBagConstraints constraints = getPanelGridBagConstraints(row);
        constraints.gridx = LEFT_COLUMN;
        constraints.anchor = GridBagConstraints.EAST;
        mDetailsPanel.add(label, constraints);
    }

    /**
     * Add an email address to the panel, in edit mode.
     *
     * @param row   This field's row number in the panel's layout
     */
    private SIPCommTextField addEditableIM(int row)
    {
        // Add a description label for the IM address.
        addIMLabel(row);

        // Create a label for the IM protocol provider.
        JLabel dropdown = new JLabel(getIMProvider());
        dropdown.setFont(dropdown.getFont().deriveFont(Font.BOLD));
        dropdown.setForeground(Color.DARK_GRAY);

        // Add the label to the panel.
        GridBagConstraints constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DROPDOWN_COLUMN;
        constraints.anchor = GridBagConstraints.WEST;
        mDetailsPanel.add(dropdown, constraints);

        // Create a textfield for the IM address.
        SIPCommTextField textField = new SIPCommTextField(IM_GUIDANCE);
        textField.setText(getIMDetail());

        // Add the textfield to the panel.
        constraints = getPanelGridBagConstraints(row);
        constraints.gridx = POST_DROPDOWN_COLUMN;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        mDetailsPanel.add(textField, constraints);

        // Add a 'clear' button for the text field.
        constraints = getPanelGridBagConstraints(row);
        constraints.gridx = DELETE_BUTTON_COLUMN;
        constraints.insets = new Insets(0, DEFAULT_INSET, 0, DEFAULT_INSET);
        mDetailsPanel.add(createClearButton(textField), constraints);

        return textField;
    }

    /**
     * Add an email address to the panel, in read-only mode.  The address
     * is clickable to start a chat session.
     *
     * @param row   This field's row number in the panel's layout
     */
    private void addReadOnlyIM(int row)
    {
        // Make the address final so that the button's action listener can
        // reference it.
        final String fAddress = getIMDetail();

        if (StringUtils.isNotBlank(fAddress))
        {
            // Add a description label for the IM address.
            addIMLabel(row);

            // Create a button for the IM address.
            JButton button = new JButton(fAddress);
            button.setFont(button.getFont().deriveFont(Font.BOLD));
            button.setForeground(Color.DARK_GRAY);

            // Set the button to start a chat session with the contact when
            // clicked.
            button.addActionListener(new ActionListener()
            {
                @SuppressWarnings("unused")
                public void actionPerformed(ActionEvent e)
                {
                    // This is the best-exposed way to start a chat session,
                    // but doesn't allow us to specify which chat protocol
                    // provider to use.  The first protocol provider found
                    // that has a contact with this address is used, which
                    // means that if the user has added the same contact IM
                    // address on two Jabber accounts, the wrong one may be
                    // used.  TBD whether this is something that needs
                    // changing.
                    UIService uiService = UtilActivator.getUIService();

                    if (uiService != null)
                    {
                        uiService.startChat(new String[]{fAddress});
                    }
                }
            });

            // Add the button to the panel.
            GridBagConstraints constraints = getPanelGridBagConstraints(row);
            constraints.gridx = READ_ONLY_CENTRE_COLUMN;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.gridwidth = 2;
            constraints.weightx = 1;
            mDetailsPanel.add(button, constraints);
        }
    }

    /**
     * Create a button suitable for the window footer (e.g. the 'OK' button).
     *
     * @param resourceKey   The resource key for the button's text
     * @param name   The unique identifier for this button
     * @return   A JButton with text and a margin, with this class as a
     * listener
     */
    private JButton createButton(String resourceKey, controlButton id)
    {
        JButton button = new JButton(Resources.getString(resourceKey));
        button.setName(id.toString());
        button.addActionListener(this);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH_PIXELS,
                                              button.getPreferredSize().height));

        // The margin must be added after the preferred size is set as we
        // don't want the margin to have an effect on preferred height.
        button.setMargin(new Insets(PADDING, PADDING, PADDING, PADDING));

        // setMnemonic(char c) claims to be obsolete but is not deprecated,
        // and it is not worth writing a utility to convert from the char to
        // an int (in the arbitrary KeyEvent mapping).
        button.setMnemonic(Resources.getMnemonic(resourceKey));

        return button;
    }

    public void actionPerformed(ActionEvent event)
    {
        // Get the unique identifier from the button leading to this event.
        JButton button = (JButton)event.getSource();

        if (controlButton.OK.equals(button))
        {
            // 'OK' button on the 'View' window: close the window.
            dispose();
        }
        else if (controlButton.EDIT.equals(button))
        {
            // 'Edit' button on the 'View' window: switch to edit mode
            switchState();
        }
        else if (controlButton.CANCEL.equals(button))
        {
            // 'Cancel' button on the 'Edit' window
            if (fReturnToViewPane)
            {
                // The window started as a 'View' window - return to view mode
                switchState();
            }
            else
            {
                // The window was called from the right-click-menu - close the
                // window.
                dispose();
            }
        }
        else if (controlButton.SAVE.equals(button))
        {
            // 'Save' button on the 'Edit' window:  Save the details
            save();
        }
    }

    /**
     * Switch the state of this window between 'View' and 'Edit'
     */
    private void switchState()
    {
        mReadOnly = !mReadOnly;
        initWindow();
        pack();
    }

    /**
     * Get the display name of the contact
     *
     * @return  The contact's display name
     */
    private String getNameDetail()
    {
        String name = null;

        // The personal contact's name is preferred over the IM buddy's.
        if (mPersonalContact != null)
        {
            @SuppressWarnings("unchecked")
            Class<? extends NameDetail>[] nameFields =
                      new Class[]{FirstNameDetail.class, LastNameDetail.class};
            name = getCompoundDetail(nameFields, " ");
        }
        else if (mIMBuddy != null)
        {
            name = mIMBuddy.getDisplayName();
        }

        return name;
    }

    /**
     * Get the IM address for the contact
     *
     * @return   The IM address of the contact
     */
    private String getIMDetail()
    {
        // Get the address of the Jabber contact if there is one, else get it
        // from the IM address field of the personal contact.
        return (mIMBuddy == null) ? getDetail(IMDetail.class) :
                                    mIMBuddy.getAddress();
    }

    /**
     * Get the IM protocol provider name for the contact
     *
     * @return   The IM protocol provider name, or "None" if there is no
     * associated IM protocol
     */
    private String getIMProvider()
    {
        return (mIMBuddy == null) ? IM_PROVIDER_NONE :
                                    mIMBuddy.getProtocolProvider().toString();
    }

    /**
     * Create a contact detail string, which is made up of the values from
     * multiple contact details fields joined by a separator string <br>
     * e.g. <tt>getCompoundDetail({street, town, country}, ", ")<br>
     * => "100 Church Street, Enfield, UK"</tt><br>
     * <br>
     * New line characters (Windows and Unix) are replaced with ", "
     *
     * @param fields      The array of fields to get contact details from
     * @param separator   The string used to join values
     * @return   A string of the combined values
     */
    private String getCompoundDetail(Class<? extends GenericDetail>[] fields,
                                       String separator)
    {
        // Add the value for each field to a list
        StringBuilder builder = new StringBuilder();

        for (Class<? extends GenericDetail> detailsClass : fields)
        {
            String detail = getDetail(detailsClass);

            // Only add the value if there are non-whitespace characters
            if (StringUtils.isNotBlank(detail))
            {
                if (builder.length() > 0)
                {
                    builder.append(separator);
                }
                // There may be new line characters in some fields - replace
                // them with commas
                builder.append(detail.replace("\r\n", ", ")
                                     .replace("\n", ", "));
            }
        }

        return builder.toString();
    }

    /**
     * Gets a contact detail string from the specified contact field
     *
     * @param field   The field to get the contact detail from
     * @return   The value from the contact field
     */
    private String getDetail(Class<? extends GenericDetail> field)
    {
        String value = "";

        if (mPersonalContact != null)
        {
            Iterator<GenericDetail> details = mContactInfoOpSet.getDetails(
                                                              mPersonalContact,
                                                              field);

            // Only use the first value found.
            if (details.hasNext())
            {
                value += details.next().getDetailValue();
            }
        }

        return value;
    }

    /**
     * Get a set of GridBagConstraints with appropriate defaults for this panel
     */
    private GridBagConstraints getPanelGridBagConstraints(int rowNumber)
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = rowNumber;
        constraints.insets = new Insets(DEFAULT_INSET,
                                        DEFAULT_INSET,
                                        DEFAULT_INSET,
                                        DEFAULT_INSET);

        return constraints;
    }

    /**
     * Get the contact of a specific protocol provider type from a metacontact
     *
     * @param metaContact   The currently selected metacontact
     * @param protocolName   The name of the protocol type e.g. "Jabber"
     * @return
     */
    private Contact getSubcontact(MetaContact metaContact, String protocolName)
    {
        Contact contact = null;
        Iterator<Contact> subContacts = metaContact.getContacts();

        // Loop through the subcontacts and extract the first one for the
        // specified protocol
        while (subContacts.hasNext())
        {
            Contact subContact = subContacts.next();

            if (subContact.getProtocolProvider()
                          .getProtocolName()
                          .equalsIgnoreCase(protocolName))
            {
                // Found a contact - stop searching
                contact = subContact;
                break;
            }
        }

        return contact;
    }

    /**
     * Saves the contact to all relevant protocol providers
     */
    private void save()
    {
        // TODO Grey out window, show spinner, decide which contacts need
        // updating...
        new Thread()
        {
            @Override
            public void run()
            {
                mUpdatableContactInfoOpSet.setDetailsForContact(
                                                    mPersonalContact,
                                                    getChangedDetails(),
                                                    new SaveDetailsListener());
            }
        }.run();
    }

    /**
     *
     * @return
     */
    private ArrayList<GenericDetail> getChangedDetails()
    {
        // TODO Only add details that have changed
        ArrayList<GenericDetail> newDetails = new ArrayList<GenericDetail>();

        newDetails.add(new FirstNameDetail(mFirstName.getText()));
        newDetails.add(new LastNameDetail(mLastName.getText()));
        newDetails.add(new WorkAddressDetail(mWorkAddressLines[0].getText()));
        newDetails.add(new WorkCityDetail(mWorkAddressLines[1].getText()));
        newDetails.add(new WorkProvinceDetail(mWorkAddressLines[2].getText()));
        newDetails.add(new WorkPostalCodeDetail(mWorkAddressLines[3].getText()));
        newDetails.add(new WorkCountryDetail(mWorkAddressLines[4].getText()));
        newDetails.add(new AddressDetail(mHomeAddressLines[0].getText()));
        newDetails.add(new CityDetail(mHomeAddressLines[1].getText()));
        newDetails.add(new ProvinceDetail(mHomeAddressLines[2].getText()));
        newDetails.add(new PostalCodeDetail(mHomeAddressLines[3].getText()));
        newDetails.add(new CountryDetail(mHomeAddressLines[4].getText()));
        newDetails.add(new NicknameDetail(mNickname.getText()));
        newDetails.add(new WorkTitleDetail(mJobTitle.getText()));
        newDetails.add(new WorkOrganizationNameDetail(mOrganisation.getText()));
        newDetails.add(new WorkPhoneDetail(mWorkPhone.getText()));
        newDetails.add(new MobilePhoneDetail(mMobilePhone.getText()));
        newDetails.add(new HomePhoneDetail(mHomePhone.getText()));
        newDetails.add(new OtherPhoneDetail(mOtherPhone.getText()));
        newDetails.add(new FaxDetail(mFaxPhone.getText()));
        newDetails.add(new SMSDetail(mSMSPhone.getText()));
        newDetails.add(new EmailAddress1Detail(mEmail1.getText()));
        newDetails.add(new EmailAddress2Detail(mEmail2.getText()));

        return newDetails;
    }

    /**
     * Create a 'clear' button which will clear the text of the specified text
     * field when it is clicked.
     *
     * @param textField   The text field that this button should clear
     * @return a button to clear the specified text field
     */
    private SIPCommButton createClearButton(SIPCommTextField textField)
    {
        return createClearButton(new SIPCommTextField[]{textField});
    }

    /**
     * Create a 'clear' button which will clear the text of all the specified
     * text fields when it is clicked.
     *
     * @param textFields   An array of text fields that this button should
     * clear
     * @return a button to clear the specified text fields
     */
    private SIPCommButton createClearButton(SIPCommTextField[] textFields)
    {
        // Create a 'clear' button.
        Image backgroundImg = Resources.getImage(CLEAR_BTN_KEY);
        Image rolloverImg   = Resources.getImage(CLEAR_BTN_ROLLOVER_KEY);
        Image pressedImg    = Resources.getImage(CLEAR_BTN_PRESSED_KEY);
        SIPCommButton clearBtn = new SIPCommButton(backgroundImg,
                                                   rolloverImg,
                                                   pressedImg,
                                                   null,
                                                   null,
                                                   null);

        // Make the array of text fields final so that the action listener can
        // reference it.
        final SIPCommTextField[] fTextFields = textFields;

        // Set the behaviour of the button to clear every text field.
        clearBtn.addActionListener(new ActionListener()
        {
            @SuppressWarnings("unused")
            public void actionPerformed(ActionEvent e)
            {
                for (SIPCommTextField field : fTextFields)
                {
                    field.setText("");
                }
            }
        });

        return clearBtn;
    }

    /**
     * Listener which can be passed to an
     * OperationSetServerStoredUpdatableContactInfo and will be called when
     * the server responds with a success/failure message after trying to
     * update a contact.
     */
    private class SaveDetailsListener implements ContactUpdateResultListener
    {
        public void updateFailed(boolean badNetwork)
        {
            // TODO do somthing on failed update
        }

        public void updateSucceeded()
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (fReturnToViewPane)
                    {
                        // The window started as a 'View' window - return to
                        // view mode
                        switchState();
                    }
                    else
                    {
                        // The window was called from the right-click-menu -
                        // close the window.
                        dispose();
                    }
                }
            });
        }
    }
}