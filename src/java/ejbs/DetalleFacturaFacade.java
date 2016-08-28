/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejbs;

import Clases.DetalleFactura;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Ernesto
 */
@Stateless
public class DetalleFacturaFacade extends AbstractFacade<DetalleFactura> {

    @PersistenceContext(unitName = "webventapedido_v1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DetalleFacturaFacade() {
        super(DetalleFactura.class);
    }
    
}
