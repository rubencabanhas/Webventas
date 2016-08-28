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
import java.util.ArrayList;
import java.util.List;
import Clases.Pedido;
import Clases.Zona;
import controladores.exceptions.NonexistentEntityException;
import controladores.exceptions.PreexistingEntityException;
import controladores.exceptions.RollbackFailureException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

/**
 *
 * @author Ernesto
 */
public class ZonaJpaController implements Serializable {

    public ZonaJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Zona zona) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (zona.getClienteList() == null) {
            zona.setClienteList(new ArrayList<Cliente>());
        }
        if (zona.getPedidoList() == null) {
            zona.setPedidoList(new ArrayList<Pedido>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Cliente> attachedClienteList = new ArrayList<Cliente>();
            for (Cliente clienteListClienteToAttach : zona.getClienteList()) {
                clienteListClienteToAttach = em.getReference(clienteListClienteToAttach.getClass(), clienteListClienteToAttach.getIdCliente());
                attachedClienteList.add(clienteListClienteToAttach);
            }
            zona.setClienteList(attachedClienteList);
            List<Pedido> attachedPedidoList = new ArrayList<Pedido>();
            for (Pedido pedidoListPedidoToAttach : zona.getPedidoList()) {
                pedidoListPedidoToAttach = em.getReference(pedidoListPedidoToAttach.getClass(), pedidoListPedidoToAttach.getIdPedido());
                attachedPedidoList.add(pedidoListPedidoToAttach);
            }
            zona.setPedidoList(attachedPedidoList);
            em.persist(zona);
            for (Cliente clienteListCliente : zona.getClienteList()) {
                Zona oldIdZonaOfClienteListCliente = clienteListCliente.getIdZona();
                clienteListCliente.setIdZona(zona);
                clienteListCliente = em.merge(clienteListCliente);
                if (oldIdZonaOfClienteListCliente != null) {
                    oldIdZonaOfClienteListCliente.getClienteList().remove(clienteListCliente);
                    oldIdZonaOfClienteListCliente = em.merge(oldIdZonaOfClienteListCliente);
                }
            }
            for (Pedido pedidoListPedido : zona.getPedidoList()) {
                Zona oldIdZonaOfPedidoListPedido = pedidoListPedido.getIdZona();
                pedidoListPedido.setIdZona(zona);
                pedidoListPedido = em.merge(pedidoListPedido);
                if (oldIdZonaOfPedidoListPedido != null) {
                    oldIdZonaOfPedidoListPedido.getPedidoList().remove(pedidoListPedido);
                    oldIdZonaOfPedidoListPedido = em.merge(oldIdZonaOfPedidoListPedido);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findZona(zona.getIdZona()) != null) {
                throw new PreexistingEntityException("Zona " + zona + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Zona zona) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Zona persistentZona = em.find(Zona.class, zona.getIdZona());
            List<Cliente> clienteListOld = persistentZona.getClienteList();
            List<Cliente> clienteListNew = zona.getClienteList();
            List<Pedido> pedidoListOld = persistentZona.getPedidoList();
            List<Pedido> pedidoListNew = zona.getPedidoList();
            List<Cliente> attachedClienteListNew = new ArrayList<Cliente>();
            for (Cliente clienteListNewClienteToAttach : clienteListNew) {
                clienteListNewClienteToAttach = em.getReference(clienteListNewClienteToAttach.getClass(), clienteListNewClienteToAttach.getIdCliente());
                attachedClienteListNew.add(clienteListNewClienteToAttach);
            }
            clienteListNew = attachedClienteListNew;
            zona.setClienteList(clienteListNew);
            List<Pedido> attachedPedidoListNew = new ArrayList<Pedido>();
            for (Pedido pedidoListNewPedidoToAttach : pedidoListNew) {
                pedidoListNewPedidoToAttach = em.getReference(pedidoListNewPedidoToAttach.getClass(), pedidoListNewPedidoToAttach.getIdPedido());
                attachedPedidoListNew.add(pedidoListNewPedidoToAttach);
            }
            pedidoListNew = attachedPedidoListNew;
            zona.setPedidoList(pedidoListNew);
            zona = em.merge(zona);
            for (Cliente clienteListOldCliente : clienteListOld) {
                if (!clienteListNew.contains(clienteListOldCliente)) {
                    clienteListOldCliente.setIdZona(null);
                    clienteListOldCliente = em.merge(clienteListOldCliente);
                }
            }
            for (Cliente clienteListNewCliente : clienteListNew) {
                if (!clienteListOld.contains(clienteListNewCliente)) {
                    Zona oldIdZonaOfClienteListNewCliente = clienteListNewCliente.getIdZona();
                    clienteListNewCliente.setIdZona(zona);
                    clienteListNewCliente = em.merge(clienteListNewCliente);
                    if (oldIdZonaOfClienteListNewCliente != null && !oldIdZonaOfClienteListNewCliente.equals(zona)) {
                        oldIdZonaOfClienteListNewCliente.getClienteList().remove(clienteListNewCliente);
                        oldIdZonaOfClienteListNewCliente = em.merge(oldIdZonaOfClienteListNewCliente);
                    }
                }
            }
            for (Pedido pedidoListOldPedido : pedidoListOld) {
                if (!pedidoListNew.contains(pedidoListOldPedido)) {
                    pedidoListOldPedido.setIdZona(null);
                    pedidoListOldPedido = em.merge(pedidoListOldPedido);
                }
            }
            for (Pedido pedidoListNewPedido : pedidoListNew) {
                if (!pedidoListOld.contains(pedidoListNewPedido)) {
                    Zona oldIdZonaOfPedidoListNewPedido = pedidoListNewPedido.getIdZona();
                    pedidoListNewPedido.setIdZona(zona);
                    pedidoListNewPedido = em.merge(pedidoListNewPedido);
                    if (oldIdZonaOfPedidoListNewPedido != null && !oldIdZonaOfPedidoListNewPedido.equals(zona)) {
                        oldIdZonaOfPedidoListNewPedido.getPedidoList().remove(pedidoListNewPedido);
                        oldIdZonaOfPedidoListNewPedido = em.merge(oldIdZonaOfPedidoListNewPedido);
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
                Integer id = zona.getIdZona();
                if (findZona(id) == null) {
                    throw new NonexistentEntityException("The zona with id " + id + " no longer exists.");
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
            Zona zona;
            try {
                zona = em.getReference(Zona.class, id);
                zona.getIdZona();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The zona with id " + id + " no longer exists.", enfe);
            }
            List<Cliente> clienteList = zona.getClienteList();
            for (Cliente clienteListCliente : clienteList) {
                clienteListCliente.setIdZona(null);
                clienteListCliente = em.merge(clienteListCliente);
            }
            List<Pedido> pedidoList = zona.getPedidoList();
            for (Pedido pedidoListPedido : pedidoList) {
                pedidoListPedido.setIdZona(null);
                pedidoListPedido = em.merge(pedidoListPedido);
            }
            em.remove(zona);
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

    public List<Zona> findZonaEntities() {
        return findZonaEntities(true, -1, -1);
    }

    public List<Zona> findZonaEntities(int maxResults, int firstResult) {
        return findZonaEntities(false, maxResults, firstResult);
    }

    private List<Zona> findZonaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Zona.class));
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

    public Zona findZona(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Zona.class, id);
        } finally {
            em.close();
        }
    }

    public int getZonaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Zona> rt = cq.from(Zona.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
