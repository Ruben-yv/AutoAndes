package pe.edu.upeu.autoandes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import pe.edu.upeu.autoandes.enums.Permiso;
import pe.edu.upeu.autoandes.security.JwtAuthenticationFilter;
import pe.edu.upeu.autoandes.security.JwtService;
import pe.edu.upeu.autoandes.security.RestAccessDeniedHandler;
import pe.edu.upeu.autoandes.security.RestAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            CorsConfigurationSource corsConfigurationSource,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
                .authenticationManager(authenticationManager)
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/swagger-ui.html",
                                "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**")
                        .hasAuthority(Permiso.VEHICULO_LEER.name())
                        .requestMatchers("/api/v1/clientes/**").denyAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/servicios/**")
                        .hasAuthority(Permiso.SERVICIO_LEER.name())
                        .requestMatchers("/api/v1/servicios/**")
                        .hasAuthority(Permiso.SERVICIO_ESCRIBIR.name())

                        .requestMatchers(HttpMethod.GET, "/api/v1/vehiculos/**")
                        .hasAuthority(Permiso.VEHICULO_LEER.name())
                        .requestMatchers("/api/v1/vehiculos/**")
                        .hasAuthority(Permiso.VEHICULO_ESCRIBIR.name())

                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/**")
                        .hasAuthority(Permiso.ORDEN_LEER.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes")
                        .hasAuthority(Permiso.ORDEN_REGISTRAR.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/ordenes/*/anular")
                        .hasAuthority(Permiso.ORDEN_ANULAR.name())
                        .requestMatchers("/api/v1/ordenes/**").denyAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/**")
                        .hasAuthority(Permiso.REPORTE_VER.name())
                        .requestMatchers("/api/v1/reportes/**").denyAll()

                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
