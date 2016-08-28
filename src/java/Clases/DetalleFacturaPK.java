/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Ernesto
 */
@Embeddable
public class DetalleFacturaPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "num_factura")
    private int numFactura;
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_pedido")
    private int idPedido;

    public DetalleFacturaPK() {
    }

    public DetalleFacturaPK(int numFactura, int idPedido) {
        this.numFactura = numFactura;
        this.idPedido = idPedido;
    }

    public int getNumFactura() {
        return numFactura;
    }

    public void setNumFactura(int numFactura) {
        this.numFactura = numFactura;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) numFactura;
        hash += (int) idPedido;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DetalleFacturaPK)) {
            return false;
        }
        DetalleFacturaPK other = (DetalleFacturaPK) object;
        if (this.numFactura != other.numFactura) {
            return false;
        }
        if (this.idPedido != other.idPedido) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Clases.DetalleFacturaPK[ numFactura=" + numFactura + ", idPedido=" + idPedido + " ]";
    }
    
}
