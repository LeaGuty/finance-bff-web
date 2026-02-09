package cl.duoc.finance_bff_web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticacion JWT que intercepta cada peticion HTTP.
 *
 * Se ejecuta antes de UsernamePasswordAuthenticationFilter en la cadena
 * de filtros de Spring Security. Su funcion es validar el token JWT
 * presente en el header "Authorization" y establecer el contexto de
 * seguridad si el token es valido.
 *
 * Flujo del filtro:
 * 1. Extrae el header "Authorization: Bearer {token}"
 * 2. Decodifica el token y obtiene el username
 * 3. Carga los datos del usuario desde UserDetailsService (InMemory)
 * 4. Valida la firma e integridad del token con JwtUtil
 * 5. Si es valido, establece la autenticacion en el SecurityContext
 * 6. Continua con la cadena de filtros hacia el Controller
 *
 * Nota: Esta clase NO es un @Component. Se instancia como @Bean
 * en SecurityConfig para evitar dependencias circulares.
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param jwtUtil Utilidad para operaciones con tokens JWT
     * @param userDetailsService Servicio para cargar datos de usuario (roles, permisos)
     */
    public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Logica principal del filtro. Se ejecuta una vez por cada peticion HTTP.
     *
     * Si el token es valido, el request continua autenticado hacia el Controller.
     * Si no hay token o es invalido, el request continua sin autenticacion
     * (Spring Security se encargara de rechazarlo si el endpoint lo requiere).
     *
     * @param request  Peticion HTTP entrante
     * @param response Respuesta HTTP
     * @param chain    Cadena de filtros de Spring Security
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Paso 1: Extraer el token del header "Authorization: Bearer xxx"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                System.out.println("Error verificando token: " + e.getMessage());
            }
        }

        // Paso 2: Si se extrajo un username y no hay autenticacion previa en el contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar datos del usuario (roles, permisos) desde InMemoryUserDetailsManager
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Paso 3: Validar firma e integridad del token
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // Crear token de autenticacion de Spring Security con los roles del usuario
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Paso 4: Establecer la autenticacion en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con la cadena de filtros
        chain.doFilter(request, response);
    }
}
