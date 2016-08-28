/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import Clases.DetalleFactura;
import Clases.DetalleFacturaPK;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Clases.Factura;
import Clases.Pedido;
import controladores.exceptions.NonexistentEntityException;
import controladores.exceptions.PreexistingEntityException;
import controladores.exceptions.RollbackFailureException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ernesto
 */
public class DetalleFacturaJpaController implements Serializable {

    public DetalleFacturaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(DetalleFactura detalleFactura) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (detalleFactura.getDetalleFacturaPK() == null) {
            detalleFactura.setDetalleFacturaPK(new DetalleFacturaPK());
        }
        detalleFactura.getDetalleFacturaPK().setIdPedido(detalleFactura.getPedido().getIdPedido());
        detalleFactura.getDetalleFacturaPK().setNumFactura(detalleFactura.getFactura().getNumFactura());
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Factura factura = detalleFactura.getFactura();
            if (factura != null) {
                factura = em.getReference(factura.getClass(), factura.getNumFactura());
                detalleFactura.setFactura(factura);
            }
            Pedido pedido = detalleFactura.getPedido();
            if (pedido != null) {
                pedido = em.getReference(pedido.getClass(), pedido.getIdPedido());
                detalleFactura.setPedido(pedido);
            }
            em.persist(detalleFactura);
            if (factura != null) {
                factura.getDetalleFacturaList().add(detalleFactura);
                factura = em.merge(factura);
            }
            if (pedido != null) {
                pedido.getDetalleFacturaList().add(detalleFactura);
                pedido = em.merge(pedido);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findDetalleFactura(detalleFactura.getDetalleFacturaPK()) != null) {
                throw new PreexistingEntityException("DetalleFactura " + detalleFactura + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(DetalleFactura detalleFactura) throws NonexistentEntityException, RollbackFailureException, Exception {
        detalleFactura.getDetalleFacturaPK().setIdPedido(detalleFactura.getPedido().getIdPedido());
        detalleFactura.getDetalleFacturaPK().setNumFactura(detalleFactura.getFactura().getNumFactura());
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            DetalleFactura persistentDetalleFactura = em.find(DetalleFactura.class, detalleFactura.getDetalleFacturaPK());
            Factura facturaOld = persistentDetalleFactura.getFactura();
            Factura facturaNew = detalleFactura.getFactura();
            Pedido pedidoOld = persistentDetalleFactura.getPedido();
            Pedido pedidoNew = detalleFactura.getPedido();
            if (facturaNew != null) {
                facturaNew = em.getReference(facturaNew.getClass(), facturaNew.getNumFactura());
                detalleFactura.setFactura(facturaNew);
            }
            if (pedidoNew != null) {
                pedidoNew = em.getReference(pedidoNew.getClass(), pedidoNew.getIdPedido());
                detalleFactura.setPedido(pedidoNew);
            }
            detalleFactura = em.merge(detalleFactura);
            if (facturaOld != null && !facturaOld.equals(facturaNew)) {
                facturaOld.getDetalleFacturaList().remove(detalleFactura);
                facturaOld = em.merge(facturaOld);
            }
            if (facturaNew != null && !facturaNew.equals(facturaOld)) {
                facturaNew.getDetalleFacturaList().add(detalleFactura);
                facturaNew = em.merge(facturaNew);
            }
            if (pedidoOld != null && !pedidoOld.equals(pedidoNew)) {
                pedidoOld.getDetalleFacturaList().remove(detalleFactura);
                pedidoOld = em.merge(pedidoOld);
            }
            if (pedidoNew != null && !pedidoNew.equals(pedidoOld)) {
                pedidoNew.getDetalleFacturaList().add(detalleFactura);
                pedidoNew = em.merge(pedidoNew);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                DetalleFacturaPK id = detalleFactura.getDetalleFacturaPK();
                if (findDetalleFactura(id) == null) {
                    throw new NonexistentEntityException("The detalleFactura with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(DetalleFacturaPK id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            DetalleFactura detalleFactura;
            try {
                detalleFactura = em.getReference(DetalleFactura.class, id);
                detalleFactura.getDetalleFacturaPK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The detalleFactura with id " + id + " no longer exists.", enfe);
            }
            Factura factura = detalleFactura.getFactura();
            if (factura != null) {
                factura.getDetalleFacturaList().remove(detalleFactura);
                factura = em.merge(factura);
            }
            Pedido pedido = detalleFactura.getPedido();
            if (pedido != null) {
                pedido.getDetalleFacturaList().remove(detalleFactura);
                pedido = em.merge(pedido);
            }
            em.remove(detalleFactura);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<DetalleFactura> findDetalleFacturaEntities() {
        return findDetalleFacturaEntities(true, -1, -1);
    }

    public List<DetalleFactura> findDetalleFacturaEntities(int maxResults, int firstResult) {
        return findDetalleFacturaEntities(false, maxResults, firstResult);
    }

    private List<DetalleFactura> findDetalleFacturaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(DetalleFactura.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public DetalleFactura findDetalleFactura(DetalleFacturaPK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(DetalleFactura.class, id);
        } finally {
            em.close();
        }
    }

    public int getDetalleFacturaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<DetalleFactura> rt = cq.from(DetalleFactura.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
