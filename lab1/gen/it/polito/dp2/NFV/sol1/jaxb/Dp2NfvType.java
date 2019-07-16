//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.01.15 at 05:41:40 PM EET 
//


package it.polito.dp2.NFV.sol1.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dp2_nfvType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dp2_nfvType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="catalog" type="{http://NFV.dp2.polito.it}catalogType"/>
 *         &lt;element name="nf_fg" type="{http://NFV.dp2.polito.it}nf_fgType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="in" type="{http://NFV.dp2.polito.it}inType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dp2_nfvType", propOrder = {
    "catalog",
    "nfFg",
    "in"
})
public class Dp2NfvType {

    @XmlElement(required = true)
    protected CatalogType catalog;
    @XmlElement(name = "nf_fg")
    protected List<NfFgType> nfFg;
    @XmlElement(required = true)
    protected InType in;

    /**
     * Gets the value of the catalog property.
     * 
     * @return
     *     possible object is
     *     {@link CatalogType }
     *     
     */
    public CatalogType getCatalog() {
        return catalog;
    }

    /**
     * Sets the value of the catalog property.
     * 
     * @param value
     *     allowed object is
     *     {@link CatalogType }
     *     
     */
    public void setCatalog(CatalogType value) {
        this.catalog = value;
    }

    /**
     * Gets the value of the nfFg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nfFg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNfFg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NfFgType }
     * 
     * 
     */
    public List<NfFgType> getNfFg() {
        if (nfFg == null) {
            nfFg = new ArrayList<NfFgType>();
        }
        return this.nfFg;
    }

    /**
     * Gets the value of the in property.
     * 
     * @return
     *     possible object is
     *     {@link InType }
     *     
     */
    public InType getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     * 
     * @param value
     *     allowed object is
     *     {@link InType }
     *     
     */
    public void setIn(InType value) {
        this.in = value;
    }

}
