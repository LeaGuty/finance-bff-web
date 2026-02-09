package cl.duoc.finance_bff_web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import cl.duoc.finance_bff_web.security.JwtFilter;
import cl.duoc.finance_bff_web.security.JwtUtil;

/**
 * Configuracion de seguridad de Spring Security para el BFF Web.
 *
 * Define:
 * - Cadena de filtros de seguridad (SecurityFilterChain)
 * - Usuarios en memoria para autenticacion
 * - Integracion del filtro JWT personalizado
 * - Politica de sesiones stateless (sin estado)
 *
 * Reglas de acceso:
 * - POST /auth/login -> Publico (sin autenticacion)
 * - GET /bff/web/v1/** -> Requiere rol CLIENTE_WEB
 * - Cualquier otro endpoint -> Requiere autenticacion
 *
 * Arquitectura:
 * El JwtFilter se instancia como @Bean aqui (no como @Component)
 * para evitar dependencias circulares con UserDetailsService.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Crea el filtro JWT como bean de Spring.
     *
     * Se instancia manualmente para romper la dependencia circular:
     * JwtFilter necesita UserDetailsService, que se define en esta misma clase.
     * Al crear ambos como @Bean, Spring resuelve el orden correctamente.
     *
     * @param jwtUtil            Utilidad JWT para validar tokens
     * @param userDetailsService Servicio de usuarios en memoria
     * @return Instancia configurada de JwtFilter
     */
    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil, InMemoryUserDetailsManager userDetailsService) {
        return new JwtFilter(jwtUtil, userDetailsService);
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     *
     * - CSRF deshabilitado: No se necesita porque usamos tokens JWT (stateless)
     * - Sesiones STATELESS: Cada peticion se autentica por su token, no hay sesion
     * - JwtFilter se ejecuta antes de UsernamePasswordAuthenticationFilter
     *
     * @param http      Builder de configuracion HTTP de Spring Security
     * @param jwtFilter Filtro JWT inyectado como bean
     * @return SecurityFilterChain configurada
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/bff/web/v1/**").hasRole("CLIENTE_WEB")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura usuarios en memoria para autenticacion.
     *
     * Usuario de prueba:
     * - Username: usuario_web
     * - Password: 1234
     * - Rol: CLIENTE_WEB (permite acceso a /bff/web/v1/**)
     *
     * Nota: withDefaultPasswordEncoder() es solo para desarrollo/pruebas.
     * En produccion se debe usar BCryptPasswordEncoder con usuarios en base de datos.
     *
     * @return InMemoryUserDetailsManager con los usuarios configurados
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("usuario_web")
            .password("1234")
            .roles("CLIENTE_WEB")
            .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Expone el AuthenticationManager como bean para poder inyectarlo
     * en AuthController y realizar la autenticacion programatica en el login.
     *
     * @param config Configuracion de autenticacion de Spring
     * @return AuthenticationManager del contexto
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
