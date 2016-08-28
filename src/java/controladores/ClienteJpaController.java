/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import Clases.Cliente;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Clases.Zona;
import Clases.Pedido;
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
public class ClienteJpaController implements Serializable {

    public ClienteJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Cliente cliente) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (cliente.getPedidoList() == null) {
            cliente.setPedidoList(new ArrayList<Pedido>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Zona idZona = cliente.getIdZona();
            if (idZona != null) {
                idZona = em.getReference(idZona.getClass(), idZona.getIdZona());
                cliente.setIdZona(idZona);
            }
            List<Pedido> attachedPedidoList = new ArrayList<Pedido>();
            for (Pedido pedidoListPedidoToAttach : cliente.getPedidoList()) {
                pedidoListPedidoToAttach = em.getReference(pedidoListPedidoToAttach.getClass(), pedidoListPedidoToAttach.getIdPedido());
                attachedPedidoList.add(pedidoListPedidoToAttach);
            }
            cliente.setPedidoList(attachedPedidoList);
            em.persist(cliente);
            if (idZona != null) {
                idZona.getClienteList().add(cliente);
                idZona = em.merge(idZona);
            }
            for (Pedido pedidoListPedido : cliente.getPedidoList()) {
                Cliente oldIdClienteOfPedidoListPedido = pedidoListPedido.getIdCliente();
                pedidoListPedido.setIdCliente(cliente);
                pedidoListPedido = em.merge(pedidoListPedido);
                if (oldIdClienteOfPedidoListPedido != null) {
                    oldIdClienteOfPedidoListPedido.getPedidoList().remove(pedidoListPedido);
                    oldIdClienteOfPedidoListPedido = em.merge(oldIdClienteOfPedidoListPedido);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findCliente(cliente.getIdCliente()) != null) {
                throw new PreexistingEntityException("Cliente " + cliente + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Cliente cliente) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Cliente persistentCliente = em.find(Cliente.class, cliente.getIdCliente());
            Zona idZonaOld = persistentCliente.getIdZona();
            Zona idZonaNew = cliente.getIdZona();
            List<Pedido> pedidoListOld = persistentCliente.getPedidoList();
            List<Pedido> pedidoListNew = cliente.getPedidoList();
            if (idZonaNew != null) {
                idZonaNew = em.getReference(idZonaNew.getClass(), idZonaNew.getIdZona());
                cliente.setIdZona(idZonaNew);
            }
            List<Pedido> attachedPedidoListNew = new ArrayList<Pedido>();
            for (Pedido pedidoListNewPedidoToAttach : pedidoListNew) {
                pedidoListNewPedidoToAttach = em.getReference(pedidoListNewPedidoToAttach.getClass(), pedidoListNewPedidoToAttach.getIdPedido());
                attachedPedidoListNew.add(pedidoListNewPedidoToAttach);
            }
            pedidoListNew = attachedPedidoListNew;
            cliente.setPedidoList(pedidoListNew);
            cliente = em.merge(cliente);
            if (idZonaOld != null && !idZonaOld.equals(idZonaNew)) {
                idZonaOld.getClienteList().remove(cliente);
                idZonaOld = em.merge(idZonaOld);
            }
            if (idZonaNew != null && !idZonaNew.equals(idZonaOld)) {
                idZonaNew.getClienteList().add(cliente);
                idZonaNew = em.merge(idZonaNew);
            }
            for (Pedido pedidoListOldPedido : pedidoListOld) {
                if (!pedidoListNew.contains(pedidoListOldPedido)) {
                    pedidoListOldPedido.setIdCliente(null);
                    pedidoListOldPedido = em.merge(pedidoListOldPedido);
                }
            }
            for (Pedido pedidoListNewPedido : pedidoListNew) {
                if (!pedidoListOld.contains(pedidoListNewPedido)) {
                    Cliente oldIdClienteOfPedidoListNewPedido = pedidoListNewPedido.getIdCliente();
                    pedidoListNewPedido.setIdCliente(cliente);
                    pedidoListNewPedido = em.merge(pedidoListNewPedido);
                    if (oldIdClienteOfPedidoListNewPedido != null && !oldIdClienteOfPedidoListNewPedido.equals(cliente)) {
                        oldIdClienteOfPedidoListNewPedido.getPedidoList().remove(pedidoListNewPedido);
                        oldIdClienteOfPedidoListNewPedido = em.merge(oldIdClienteOfPedidoListNewPedido);
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
                Integer id = cliente.getIdCliente();
                if (findCliente(id) == null) {
                    throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.");
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
            Cliente cliente;
            try {
                cliente = em.getReference(Cliente.class, id);
                cliente.getIdCliente();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.", enfe);
            }
            Zona idZona = cliente.getIdZona();
            if (idZona != null) {
                idZona.getClienteList().remove(cliente);
                idZona = em.merge(idZona);
            }
            List<Pedido> pedidoList = cliente.getPedidoList();
            for (Pedido pedidoListPedido : pedidoList) {
                pedidoListPedido.setIdCliente(null);
                pedidoListPedido = em.merge(pedidoListPedido);
            }
            em.remove(cliente);
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

    public List<Cliente> findClienteEntities() {
        return findClienteEntities(true, -1, -1);
    }

    public List<Cliente> findClienteEntities(int maxResults, int firstResult) {
        return findClienteEntities(false, maxResults, firstResult);
    }

    private List<Cliente> findClienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Cliente.class));
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

    public Cliente findCliente(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }

    public int getClienteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Cliente> rt = cq.from(Cliente.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
