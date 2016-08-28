/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Clases.Cliente;
import Clases.Producto;
import Clases.Vendedor;
import Clases.Zona;
import Clases.DetalleFactura;
import Clases.Pedido;
import controladores.exceptions.IllegalOrphanException;
import controladores.exceptions.NonexistentEntityException;
import controladores.exceptions.PreexistingEntityException;
import controladores.exceptions.RollbackFailureException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ernesto
 */
public class PedidoJpaController implements Serializable {

    public PedidoJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Pedido pedido) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (pedido.getDetalleFacturaList() == null) {
            pedido.setDetalleFacturaList(new ArrayList<DetalleFactura>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cliente idCliente = pedido.getIdCliente();
            if (idCliente != null) {
                idCliente = em.getReference(idCliente.getClass(), idCliente.getIdCliente());
                pedido.setIdCliente(idCliente);
            }
            Producto idProducto = pedido.getIdProducto();
            if (idProducto != null) {
                idProducto = em.getReference(idProducto.getClass(), idProducto.getIdProducto());
                pedido.setIdProducto(idProducto);
            }
            Vendedor idVendedor = pedido.getIdVendedor();
            if (idVendedor != null) {
                idVendedor = em.getReference(idVendedor.getClass(), idVendedor.getIdVendedor());
                pedido.setIdVendedor(idVendedor);
            }
            Zona idZona = pedido.getIdZona();
            if (idZona != null) {
                idZona = em.getReference(idZona.getClass(), idZona.getIdZona());
                pedido.setIdZona(idZona);
            }
            List<DetalleFactura> attachedDetalleFacturaList = new ArrayList<DetalleFactura>();
            for (DetalleFactura detalleFacturaListDetalleFacturaToAttach : pedido.getDetalleFacturaList()) {
                detalleFacturaListDetalleFacturaToAttach = em.getReference(detalleFacturaListDetalleFacturaToAttach.getClass(), detalleFacturaListDetalleFacturaToAttach.getDetalleFacturaPK());
                attachedDetalleFacturaList.add(detalleFacturaListDetalleFacturaToAttach);
            }
            pedido.setDetalleFacturaList(attachedDetalleFacturaList);
            em.persist(pedido);
            if (idCliente != null) {
                idCliente.getPedidoList().add(pedido);
                idCliente = em.merge(idCliente);
            }
            if (idProducto != null) {
                idProducto.getPedidoList().add(pedido);
                idProducto = em.merge(idProducto);
            }
            if (idVendedor != null) {
                idVendedor.getPedidoList().add(pedido);
                idVendedor = em.merge(idVendedor);
            }
            if (idZona != null) {
                idZona.getPedidoList().add(pedido);
                idZona = em.merge(idZona);
            }
            for (DetalleFactura detalleFacturaListDetalleFactura : pedido.getDetalleFacturaList()) {
                Pedido oldPedidoOfDetalleFacturaListDetalleFactura = detalleFacturaListDetalleFactura.getPedido();
                detalleFacturaListDetalleFactura.setPedido(pedido);
                detalleFacturaListDetalleFactura = em.merge(detalleFacturaListDetalleFactura);
                if (oldPedidoOfDetalleFacturaListDetalleFactura != null) {
                    oldPedidoOfDetalleFacturaListDetalleFactura.getDetalleFacturaList().remove(detalleFacturaListDetalleFactura);
                    oldPedidoOfDetalleFacturaListDetalleFactura = em.merge(oldPedidoOfDetalleFacturaListDetalleFactura);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findPedido(pedido.getIdPedido()) != null) {
                throw new PreexistingEntityException("Pedido " + pedido + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Pedido pedido) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pedido persistentPedido = em.find(Pedido.class, pedido.getIdPedido());
            Cliente idClienteOld = persistentPedido.getIdCliente();
            Cliente idClienteNew = pedido.getIdCliente();
            Producto idProductoOld = persistentPedido.getIdProducto();
            Producto idProductoNew = pedido.getIdProducto();
            Vendedor idVendedorOld = persistentPedido.getIdVendedor();
            Vendedor idVendedorNew = pedido.getIdVendedor();
            Zona idZonaOld = persistentPedido.getIdZona();
            Zona idZonaNew = pedido.getIdZona();
            List<DetalleFactura> detalleFacturaListOld = persistentPedido.getDetalleFacturaList();
            List<DetalleFactura> detalleFacturaListNew = pedido.getDetalleFacturaList();
            List<String> illegalOrphanMessages = null;
            for (DetalleFactura detalleFacturaListOldDetalleFactura : detalleFacturaListOld) {
                if (!detalleFacturaListNew.contains(detalleFacturaListOldDetalleFactura)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain DetalleFactura " + detalleFacturaListOldDetalleFactura + " since its pedido field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (idClienteNew != null) {
                idClienteNew = em.getReference(idClienteNew.getClass(), idClienteNew.getIdCliente());
                pedido.setIdCliente(idClienteNew);
            }
            if (idProductoNew != null) {
                idProductoNew = em.getReference(idProductoNew.getClass(), idProductoNew.getIdProducto());
                pedido.setIdProducto(idProductoNew);
            }
            if (idVendedorNew != null) {
                idVendedorNew = em.getReference(idVendedorNew.getClass(), idVendedorNew.getIdVendedor());
                pedido.setIdVendedor(idVendedorNew);
            }
            if (idZonaNew != null) {
                idZonaNew = em.getReference(idZonaNew.getClass(), idZonaNew.getIdZona());
                pedido.setIdZona(idZonaNew);
            }
            List<DetalleFactura> attachedDetalleFacturaListNew = new ArrayList<DetalleFactura>();
            for (DetalleFactura detalleFacturaListNewDetalleFacturaToAttach : detalleFacturaListNew) {
                detalleFacturaListNewDetalleFacturaToAttach = em.getReference(detalleFacturaListNewDetalleFacturaToAttach.getClass(), detalleFacturaListNewDetalleFacturaToAttach.getDetalleFacturaPK());
                attachedDetalleFacturaListNew.add(detalleFacturaListNewDetalleFacturaToAttach);
            }
            detalleFacturaListNew = attachedDetalleFacturaListNew;
            pedido.setDetalleFacturaList(detalleFacturaListNew);
            pedido = em.merge(pedido);
            if (idClienteOld != null && !idClienteOld.equals(idClienteNew)) {
                idClienteOld.getPedidoList().remove(pedido);
                idClienteOld = em.merge(idClienteOld);
            }
            if (idClienteNew != null && !idClienteNew.equals(idClienteOld)) {
                idClienteNew.getPedidoList().add(pedido);
                idClienteNew = em.merge(idClienteNew);
            }
            if (idProductoOld != null && !idProductoOld.equals(idProductoNew)) {
                idProductoOld.getPedidoList().remove(pedido);
                idProductoOld = em.merge(idProductoOld);
            }
            if (idProductoNew != null && !idProductoNew.equals(idProductoOld)) {
                idProductoNew.getPedidoList().add(pedido);
                idProductoNew = em.merge(idProductoNew);
            }
            if (idVendedorOld != null && !idVendedorOld.equals(idVendedorNew)) {
                idVendedorOld.getPedidoList().remove(pedido);
                idVendedorOld = em.merge(idVendedorOld);
            }
            if (idVendedorNew != null && !idVendedorNew.equals(idVendedorOld)) {
                idVendedorNew.getPedidoList().add(pedido);
                idVendedorNew = em.merge(idVendedorNew);
            }
            if (idZonaOld != null && !idZonaOld.equals(idZonaNew)) {
                idZonaOld.getPedidoList().remove(pedido);
                idZonaOld = em.merge(idZonaOld);
            }
            if (idZonaNew != null && !idZonaNew.equals(idZonaOld)) {
                idZonaNew.getPedidoList().add(pedido);
                idZonaNew = em.merge(idZonaNew);
            }
            for (DetalleFactura detalleFacturaListNewDetalleFactura : detalleFacturaListNew) {
                if (!detalleFacturaListOld.contains(detalleFacturaListNewDetalleFactura)) {
                    Pedido oldPedidoOfDetalleFacturaListNewDetalleFactura = detalleFacturaListNewDetalleFactura.getPedido();
                    detalleFacturaListNewDetalleFactura.setPedido(pedido);
                    detalleFacturaListNewDetalleFactura = em.merge(detalleFacturaListNewDetalleFactura);
                    if (oldPedidoOfDetalleFacturaListNewDetalleFactura != null && !oldPedidoOfDetalleFacturaListNewDetalleFactura.equals(pedido)) {
                        oldPedidoOfDetalleFacturaListNewDetalleFactura.getDetalleFacturaList().remove(detalleFacturaListNewDetalleFactura);
                        oldPedidoOfDetalleFacturaListNewDetalleFactura = em.merge(oldPedidoOfDetalleFacturaListNewDetalleFactura);
                    }
                }
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
                Integer id = pedido.getIdPedido();
                if (findPedido(id) == null) {
                    throw new NonexistentEntityException("The pedido with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Pedido pedido;
            try {
                pedido = em.getReference(Pedido.class, id);
                pedido.getIdPedido();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pedido with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<DetalleFactura> detalleFacturaListOrphanCheck = pedido.getDetalleFacturaList();
            for (DetalleFactura detalleFacturaListOrphanCheckDetalleFactura : detalleFacturaListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Pedido (" + pedido + ") cannot be destroyed since the DetalleFactura " + detalleFacturaListOrphanCheckDetalleFactura + " in its detalleFacturaList field has a non-nullable pedido field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Cliente idCliente = pedido.getIdCliente();
            if (idCliente != null) {
                idCliente.getPedidoList().remove(pedido);
                idCliente = em.merge(idCliente);
            }
            Producto idProducto = pedido.getIdProducto();
            if (idProducto != null) {
                idProducto.getPedidoList().remove(pedido);
                idProducto = em.merge(idProducto);
            }
            Vendedor idVendedor = pedido.getIdVendedor();
            if (idVendedor != null) {
                idVendedor.getPedidoList().remove(pedido);
                idVendedor = em.merge(idVendedor);
            }
            Zona idZona = pedido.getIdZona();
            if (idZona != null) {
                idZona.getPedidoList().remove(pedido);
                idZona = em.merge(idZona);
            }
            em.remove(pedido);
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

    public List<Pedido> findPedidoEntities() {
        return findPedidoEntities(true, -1, -1);
    }

    public List<Pedido> findPedidoEntities(int maxResults, int firstResult) {
        return findPedidoEntities(false, maxResults, firstResult);
    }

    private List<Pedido> findPedidoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Pedido.class));
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

    public Pedido findPedido(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pedido.class, id);
        } finally {
            em.close();
        }
    }

    public int getPedidoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Pedido> rt = cq.from(Pedido.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
