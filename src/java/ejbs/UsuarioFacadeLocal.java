/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ejbs;

import Clases.Usuario;
import javax.ejb.Local;

/**
 *
 * @author Ernesto
 */
@Local
public interface UsuarioFacadeLocal {
    Usuario iniciarSesion(Usuario us);
}
