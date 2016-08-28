
package controladores;

import Clases.Usuario;
import ejbs.UsuarioFacadeLocal;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("indexController")
@ViewScoped 
public class IndexController implements Serializable{
    
    @EJB
    private UsuarioFacadeLocal EJBUsuario;
    
    private Usuario user;
    
    @PostConstruct
    public void init(){
        user=new Usuario();
        
    }

    public Usuario getUsuario() {
        return user;
    }

    public void setUsuario(Usuario user) {
        this.user = user;
    }
    
    public String inciarSesion(){
        Usuario us;
        String redireccion=null;
        try{
            us=EJBUsuario.iniciarSesion(user);
            if (us!=null){
                redireccion="/protegido/principal";
            }else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,"Aviso","Credenciales Incorrectos"));
            }
        }catch(Exception e){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Aviso","Error"));
        }
        return redireccion;
    }
    
}
