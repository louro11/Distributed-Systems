
package org.komparator.supplier.ws;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.10
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "BadText", targetNamespace = "http://ws.supplier.komparator.org/")
public class BadText_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private BadText faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public BadText_Exception(String message, BadText faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param cause
     * @param message
     */
    public BadText_Exception(String message, BadText faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.komparator.supplier.ws.BadText
     */
    public BadText getFaultInfo() {
        return faultInfo;
    }

}
