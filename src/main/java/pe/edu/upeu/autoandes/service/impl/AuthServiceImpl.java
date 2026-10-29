package pe.edu.upeu.autoandes.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.auth.LoginRequestDTO;
import pe.edu.upeu.autoandes.dto.auth.LoginResponseDTO;
import pe.edu.upeu.autoandes.dto.auth.UsuarioActualResponseDTO;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.exception.RecursoNoEncontradoException;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;
import pe.edu.upeu.autoandes.security.JwtService;
import pe.edu.upeu.autoandes.security.PermisosPorRol;
import pe.edu.upeu.autoandes.service.service.AuthService;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService,
                           UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        String username = request.getUsername().trim();
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, request.getPassword()));

        Usuario usuario = buscarUsuario(authentication.getName());
        List<String> roles = nombresRoles(usuario);
        List<String> permisos = PermisosPorRol.permisos(usuario.getRoles());
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        String token = jwtService.generateToken(usuario.getUsername(), authorities, roles, permisos);

        log.info("Inicio de sesión correcto: usuario={} roles={}", usuario.getUsername(), roles);
        return new LoginResponseDTO(token, "Bearer", jwtService.expirationSeconds(),
                usuario.getUsername(), usuario.getNombreCompleto(), roles, permisos);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioActualResponseDTO usuarioActual(String username) {
        Usuario usuario = buscarUsuario(username);
        return new UsuarioActualResponseDTO(usuario.getUsername(), usuario.getNombreCompleto(),
                nombresRoles(usuario), PermisosPorRol.permisos(usuario.getRoles()));
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private List<String> nombresRoles(Usuario usuario) {
        return usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .sorted()
                .toList();
    }
}
