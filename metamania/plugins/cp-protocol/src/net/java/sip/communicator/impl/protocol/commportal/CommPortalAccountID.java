package net.java.sip.communicator.impl.protocol.commportal;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;

/**
 * A simple Account ID for use by the CommPortal protocol
 */
class CommPortalAccountID extends AccountID
{

    protected CommPortalAccountID(String userID,
                                  Map<String, String> accountProperties,
                                  String serviceName)
    {
        super(userID,
              accountProperties,
              ProtocolProviderFactoryCPImpl.PROTOCOL_NAME,
              serviceName);
    }

}
