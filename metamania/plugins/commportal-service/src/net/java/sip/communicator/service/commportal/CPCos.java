package net.java.sip.communicator.service.commportal;

/**
 * A service allowing interaction with the Class of Service
 */
public interface CPCos
{
    /**
     * @return the value of the "IchServiceLevel" value
     */
    public String getIchServiceLevel();

    /**
     * @return the value of the "IchAllowed" value
     */
    public boolean getIchAllowed();
}
