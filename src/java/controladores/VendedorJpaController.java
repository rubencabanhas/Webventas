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
import Clases.Usuario;
import Clases.Pedido;
import Clases.Vendedor;
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
public class VendedorJpaController implements Serializable {

    public VendedorJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Vendedor vendedor) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (vendedor.getPedidoList() == null) {
            vendedor.setPedidoList(new ArrayList<Pedido>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Usuario idUsuario = vendedor.getIdUsuario();
            if (idUsuario != null) {
                idUsuario = em.getReference(idUsuario.getClass(), idUsuario.getIdUsuario());
                vendedor.setIdUsuario(idUsuario);
            }
            List<Pedido> attachedPedidoList = new ArrayList<Pedido>();
            for (Pedido pedidoListPedidoToAttach : vendedor.getPedidoList()) {
                pedidoListPedidoToAttach = em.getReference(pedidoListPedidoToAttach.getClass(), pedidoListPedidoToAttach.getIdPedido());
                attachedPedidoList.add(pedidoListPedidoToAttach);
            }
            vendedor.setPedidoList(attachedPedidoList);
            em.persist(vendedor);
            if (idUsuario != null) {
                idUsuario.getVendedorList().add(vendedor);
                idUsuario = em.merge(idUsuario);
            }
            for (Pedido pedidoListPedido : vendedor.getPedidoList()) {
                Vendedor oldIdVendedorOfPedidoListPedido = pedidoListPedido.getIdVendedor();
                pedidoListPedido.setIdVendedor(vendedor);
                pedidoListPedido = em.merge(pedidoListPedido);
                if (oldIdVendedorOfPedidoListPedido != null) {
                    oldIdVendedorOfPedidoListPedido.getPedidoList().remove(pedidoListPedido);
                    oldIdVendedorOfPedidoListPedido = em.merge(oldIdVendedorOfPedidoListPedido);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findVendedor(vendedor.getIdVendedor()) != null) {
                throw new PreexistingEntityException("Vendedor " + vendedor + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Vendedor vendedor) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Vendedor persistentVendedor = em.find(Vendedor.class, vendedor.getIdVendedor());
            Usuario idUsuarioOld = persistentVendedor.getIdUsuario();
            Usuario idUsuarioNew = vendedor.getIdUsuario();
            List<Pedido> pedidoListOld = persistentVendedor.getPedidoList();
            List<Pedido> pedidoListNew = vendedor.getPedidoList();
            if (idUsuarioNew != null) {
                idUsuarioNew = em.getReference(idUsuarioNew.getClass(), idUsuarioNew.getIdUsuario());
                vendedor.setIdUsuario(idUsuarioNew);
            }
            List<Pedido> attachedPedidoListNew = new ArrayList<Pedido>();
            for (Pedido pedidoListNewPedidoToAttach : pedidoListNew) {
                pedidoListNewPedidoToAttach = em.getReference(pedidoListNewPedidoToAttach.getClass(), pedidoListNewPedidoToAttach.getIdPedido());
                attachedPedidoListNew.add(pedidoListNewPedidoToAttach);
            }
            pedidoListNew = attachedPedidoListNew;
            vendedor.setPedidoList(pedidoListNew);
            vendedor = em.merge(vendedor);
            if (idUsuarioOld != null && !idUsuarioOld.equals(idUsuarioNew)) {
                idUsuarioOld.getVendedorList().remove(vendedor);
                idUsuarioOld = em.merge(idUsuarioOld);
            }
            if (idUsuarioNew != null && !idUsuarioNew.equals(idUsuarioOld)) {
                idUsuarioNew.getVendedorList().add(vendedor);
                idUsuarioNew = em.merge(idUsuarioNew);
            }
            for (Pedido pedidoListOldPedido : pedidoListOld) {
                if (!pedidoListNew.contains(pedidoListOldPedido)) {
                    pedidoListOldPedido.setIdVendedor(null);
                    pedidoListOldPedido = em.merge(pedidoListOldPedido);
                }
            }
            for (Pedido pedidoListNewPedido : pedidoListNew) {
                if (!pedidoListOld.contains(pedidoListNewPedido)) {
                    Vendedor oldIdVendedorOfPedidoListNewPedido = pedidoListNewPedido.getIdVendedor();
                    pedidoListNewPedido.setIdVendedor(vendedor);
                    pedidoListNewPedido = em.merge(pedidoListNewPedido);
                    if (oldIdVendedorOfPedidoListNewPedido != null && !oldIdVendedorOfPedidoListNewPedido.equals(vendedor)) {
                        oldIdVendedorOfPedidoListNewPedido.getPedidoList().remove(pedidoListNewPedido);
                        oldIdVendedorOfPedidoListNewPedido = em.merge(oldIdVendedorOfPedidoListNewPedido);
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
                Integer id = vendedor.getIdVendedor();
                if (findVendedor(id) == null) {
                    throw new NonexistentEntityException("The vendedor with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Vendedor vendedor;
            try {
                vendedor = em.getReference(Vendedor.class, id);
                vendedor.getIdVendedor();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The vendedor with id " + id + " no longer exists.", enfe);
            }
            Usuario idUsuario = vendedor.getIdUsuario();
            if (idUsuario != null) {
                idUsuario.getVendedorList().remove(vendedor);
                idUsuario = em.merge(idUsuario);
            }
            List<Pedido> pedidoList = vendedor.getPedidoList();
            for (Pedido pedidoListPedido : pedidoList) {
                pedidoListPedido.setIdVendedor(null);
                pedidoListPedido = em.merge(pedidoListPedido);
            }
            em.remove(vendedor);
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

    public List<Vendedor> findVendedorEntities() {
        return findVendedorEntities(true, -1, -1);
    }

    public List<Vendedor> findVendedorEntities(int maxResults, int firstResult) {
        return findVendedorEntities(false, maxResults, firstResult);
    }

    private List<Vendedor> findVendedorEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Vendedor.class));
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

    public Vendedor findVendedor(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Vendedor.class, id);
        } finally {
            em.close();
        }
    }

    public int getVendedorCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Vendedor> rt = cq.from(Vendedor.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
