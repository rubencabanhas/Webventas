/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Ernesto
 */
@Entity
@Table(name = "zona")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Zona.findAll", query = "SELECT z FROM Zona z"),
    @NamedQuery(name = "Zona.findByIdZona", query = "SELECT z FROM Zona z WHERE z.idZona = :idZona"),
    @NamedQuery(name = "Zona.findByCiudad", query = "SELECT z FROM Zona z WHERE z.ciudad = :ciudad"),
    @NamedQuery(name = "Zona.findByDireccion", query = "SELECT z FROM Zona z WHERE z.direccion = :direccion"),
    @NamedQuery(name = "Zona.findByLatitdu", query = "SELECT z FROM Zona z WHERE z.latitdu = :latitdu"),
    @NamedQuery(name = "Zona.findByLongitud", query = "SELECT z FROM Zona z WHERE z.longitud = :longitud")})
public class Zona implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_zona")
    private Integer idZona;
    @Size(max = 50)
    @Column(name = "ciudad")
    private String ciudad;
    @Size(max = 100)
    @Column(name = "direccion")
    private String direccion;
    @Size(max = 20)
    @Column(name = "latitdu")
    private String latitdu;
    @Size(max = 20)
    @Column(name = "longitud")
    private String longitud;
    @OneToMany(mappedBy = "idZona")
    private List<Cliente> clienteList;
    @OneToMany(mappedBy = "idZona")
    private List<Pedido> pedidoList;

    public Zona() {
    }

    public Zona(Integer idZona) {
        this.idZona = idZona;
    }

    public Integer getIdZona() {
        return idZona;
    }

    public void setIdZona(Integer idZona) {
        this.idZona = idZona;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLatitdu() {
        return latitdu;
    }

    public void setLatitdu(String latitdu) {
        this.latitdu = latitdu;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    @XmlTransient
    public List<Cliente> getClienteList() {
        return clienteList;
    }

    public void setClienteList(List<Cliente> clienteList) {
        this.clienteList = clienteList;
    }

    @XmlTransient
    public List<Pedido> getPedidoList() {
        return pedidoList;
    }

    public void setPedidoList(List<Pedido> pedidoList) {
        this.pedidoList = pedidoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idZona != null ? idZona.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Zona)) {
            return false;
        }
        Zona other = (Zona) object;
        if ((this.idZona == null && other.idZona != null) || (this.idZona != null && !this.idZona.equals(other.idZona))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Clases.Zona[ idZona=" + idZona + " ]";
    }
    
}
