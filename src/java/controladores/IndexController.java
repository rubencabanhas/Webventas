
package controladores;

import Clases.Usuario;
import ejbs.UsuarioFacadeLocal;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("indexController")
@RequestScoped
public class IndexController implements Serializable{
    private String nombre;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
    private String clave;
    
    @EJB
    private UsuarioFacadeLocal EJBUsuario;
    
    private Usuario user;
    
    @PostConstruct
    public void init(){
        user=new Usuario();
        
    }

    public Usuario getUser() {
        return user;
    }

    public void setUser(Usuario user) {
        this.user = user;
    }

    
    
    public String inciarSesion(){
        Usuario us;
        String redireccion=null;
        try{
            user.setUsuario(nombre);
            user.setClave(clave);
            us=EJBUsuario.iniciarSesion(user);
            if (us!=null){
                redireccion="protegido/principal";
            }else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Aviso","Credenciales Incorrectos"));
            }
        }catch(Exception e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Aviso","Error"));
        }
        return redireccion;
    }
    
}
