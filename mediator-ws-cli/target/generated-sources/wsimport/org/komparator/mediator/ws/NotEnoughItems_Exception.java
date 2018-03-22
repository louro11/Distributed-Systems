
package org.komparator.mediator.ws;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.10
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "NotEnoughItems", targetNamespace = "http://ws.mediator.komparator.org/")
public class NotEnoughItems_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private NotEnoughItems faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public NotEnoughItems_Exception(String message, NotEnoughItems faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param cause
     * @param message
     */
    public NotEnoughItems_Exception(String message, NotEnoughItems faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.komparator.mediator.ws.NotEnoughItems
     */
    public NotEnoughItems getFaultInfo() {
        return faultInfo;
    }

}