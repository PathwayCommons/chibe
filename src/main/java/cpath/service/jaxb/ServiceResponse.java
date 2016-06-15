package cpath.service.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

// not instantiable, basic cpath2 xml response type
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceResponse")
public abstract class ServiceResponse implements Serializable {
	@XmlTransient
	public abstract boolean isEmpty();
}
