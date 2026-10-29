package pe.edu.upeu.autoandes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.repository.RolRepository;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;

import java.util.HashSet;

/** Cuentas de demostración solicitadas por la guía, únicamente en el perfil local dev. */
@Configuration
@Profile("dev")
public class UsuariosPruebaConfig {
    private static final Logger log = LoggerFactory.getLogger(UsuariosPruebaConfig.class);

    @Bean
    CommandLineRunner crearUsuariosDePrueba(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            crearSiNoExiste(usuarioRepository, rolRepository, passwordEncoder,
                    "admin", "Administrador AutoAndes", "ADMIN", true);
            crearSiNoExiste(usuarioRepository, rolRepository, passwordEncoder,
                    "asesor", "Asesor AutoAndes", "ASESOR", true);
            crearSiNoExiste(usuarioRepository, rolRepository, passwordEncoder,
                    "jefe", "Jefe de Taller", "JEFE_TALLER", true);
            crearSiNoExiste(usuarioRepository, rolRepository, passwordEncoder,
                    "inactivo", "Usuario Inactivo", "ASESOR", false);
        };
    }

    private void crearSiNoExiste(UsuarioRepository usuarios, RolRepository roles,
                                 PasswordEncoder encoder, String username,
                                 String nombreCompleto, String nombreRol, boolean activo) {
        if (usuarios.existsByUsernameIgnoreCase(username)) {
            return;
        }
        Rol rol = roles.findByNombreIgnoreCase(nombreRol)
                .orElseThrow(() -> new IllegalStateException("No está sembrado el rol " + nombreRol));
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(encoder.encode("Auto2026*"));
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setActivo(activo);
        usuario.setRoles(new HashSet<>(java.util.Set.of(rol)));
        usuarios.save(usuario);
        log.info("Cuenta de demostración preparada: usuario={} activo={} rol={}", username, activo, nombreRol);
    }
}
