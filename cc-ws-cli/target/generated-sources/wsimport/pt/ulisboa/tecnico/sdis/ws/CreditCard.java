
package pt.ulisboa.tecnico.sdis.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.10
 * Generated source version: 2.2
 * 
 */
@WebService(name = "CreditCard", targetNamespace = "http://ws.sdis.tecnico.ulisboa.pt/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface CreditCard {


    /**
     * 
     * @param numberAsString
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "validateNumber", targetNamespace = "http://ws.sdis.tecnico.ulisboa.pt/", className = "pt.ulisboa.tecnico.sdis.ws.ValidateNumber")
    @ResponseWrapper(localName = "validateNumberResponse", targetNamespace = "http://ws.sdis.tecnico.ulisboa.pt/", className = "pt.ulisboa.tecnico.sdis.ws.ValidateNumberResponse")
    @Action(input = "http://ws.sdis.tecnico.ulisboa.pt/CreditCard/validateNumberRequest", output = "http://ws.sdis.tecnico.ulisboa.pt/CreditCard/validateNumberResponse")
    public boolean validateNumber(
        @WebParam(name = "numberAsString", targetNamespace = "")
        String numberAsString);

    /**
     * 
     * @param name
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "ping", targetNamespace = "http://ws.sdis.tecnico.ulisboa.pt/", className = "pt.ulisboa.tecnico.sdis.ws.Ping")
    @ResponseWrapper(localName = "pingResponse", targetNamespace = "http://ws.sdis.tecnico.ulisboa.pt/", className = "pt.ulisboa.tecnico.sdis.ws.PingResponse")
    @Action(input = "http://ws.sdis.tecnico.ulisboa.pt/CreditCard/pingRequest", output = "http://ws.sdis.tecnico.ulisboa.pt/CreditCard/pingResponse")
    public String ping(
        @WebParam(name = "name", targetNamespace = "")
        String name);

}