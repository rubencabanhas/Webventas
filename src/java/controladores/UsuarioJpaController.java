/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controladores;

import Clases.Usuario;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
public class UsuarioJpaController implements Serializable {

    public UsuarioJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Usuario usuario) throws PreexistingEntityException, RollbackFailureException, Exception {
        if (usuario.getVendedorList() == null) {
            usuario.setVendedorList(new ArrayList<Vendedor>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Vendedor> attachedVendedorList = new ArrayList<Vendedor>();
            for (Vendedor vendedorListVendedorToAttach : usuario.getVendedorList()) {
                vendedorListVendedorToAttach = em.getReference(vendedorListVendedorToAttach.getClass(), vendedorListVendedorToAttach.getIdVendedor());
                attachedVendedorList.add(vendedorListVendedorToAttach);
            }
            usuario.setVendedorList(attachedVendedorList);
            em.persist(usuario);
            for (Vendedor vendedorListVendedor : usuario.getVendedorList()) {
                Usuario oldIdUsuarioOfVendedorListVendedor = vendedorListVendedor.getIdUsuario();
                vendedorListVendedor.setIdUsuario(usuario);
                vendedorListVendedor = em.merge(vendedorListVendedor);
                if (oldIdUsuarioOfVendedorListVendedor != null) {
                    oldIdUsuarioOfVendedorListVendedor.getVendedorList().remove(vendedorListVendedor);
                    oldIdUsuarioOfVendedorListVendedor = em.merge(oldIdUsuarioOfVendedorListVendedor);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            if (findUsuario(usuario.getIdUsuario()) != null) {
                throw new PreexistingEntityException("Usuario " + usuario + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Usuario usuario) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Usuario persistentUsuario = em.find(Usuario.class, usuario.getIdUsuario());
            List<Vendedor> vendedorListOld = persistentUsuario.getVendedorList();
            List<Vendedor> vendedorListNew = usuario.getVendedorList();
            List<Vendedor> attachedVendedorListNew = new ArrayList<Vendedor>();
            for (Vendedor vendedorListNewVendedorToAttach : vendedorListNew) {
                vendedorListNewVendedorToAttach = em.getReference(vendedorListNewVendedorToAttach.getClass(), vendedorListNewVendedorToAttach.getIdVendedor());
                attachedVendedorListNew.add(vendedorListNewVendedorToAttach);
            }
            vendedorListNew = attachedVendedorListNew;
            usuario.setVendedorList(vendedorListNew);
            usuario = em.merge(usuario);
            for (Vendedor vendedorListOldVendedor : vendedorListOld) {
                if (!vendedorListNew.contains(vendedorListOldVendedor)) {
                    vendedorListOldVendedor.setIdUsuario(null);
                    vendedorListOldVendedor = em.merge(vendedorListOldVendedor);
                }
            }
            for (Vendedor vendedorListNewVendedor : vendedorListNew) {
                if (!vendedorListOld.contains(vendedorListNewVendedor)) {
                    Usuario oldIdUsuarioOfVendedorListNewVendedor = vendedorListNewVendedor.getIdUsuario();
                    vendedorListNewVendedor.setIdUsuario(usuario);
                    vendedorListNewVendedor = em.merge(vendedorListNewVendedor);
                    if (oldIdUsuarioOfVendedorListNewVendedor != null && !oldIdUsuarioOfVendedorListNewVendedor.equals(usuario)) {
                        oldIdUsuarioOfVendedorListNewVendedor.getVendedorList().remove(vendedorListNewVendedor);
                        oldIdUsuarioOfVendedorListNewVendedor = em.merge(oldIdUsuarioOfVendedorListNewVendedor);
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
                Integer id = usuario.getIdUsuario();
                if (findUsuario(id) == null) {
                    throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.");
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
            Usuario usuario;
            try {
                usuario = em.getReference(Usuario.class, id);
                usuario.getIdUsuario();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The usuario with id " + id + " no longer exists.", enfe);
            }
            List<Vendedor> vendedorList = usuario.getVendedorList();
            for (Vendedor vendedorListVendedor : vendedorList) {
                vendedorListVendedor.setIdUsuario(null);
                vendedorListVendedor = em.merge(vendedorListVendedor);
            }
            em.remove(usuario);
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

    public List<Usuario> findUsuarioEntities() {
        return findUsuarioEntities(true, -1, -1);
    }

    public List<Usuario> findUsuarioEntities(int maxResults, int firstResult) {
        return findUsuarioEntities(false, maxResults, firstResult);
    }

    private List<Usuario> findUsuarioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Usuario.class));
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

    public Usuario findUsuario(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public int getUsuarioCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Usuario> rt = cq.from(Usuario.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
