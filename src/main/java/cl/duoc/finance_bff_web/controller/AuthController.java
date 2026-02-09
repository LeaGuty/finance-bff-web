package cl.duoc.finance_bff_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.finance_bff_web.security.JwtUtil;

/**
 * Controlador de autenticacion para el BFF Web.
 *
 * Proporciona el endpoint publico de login donde los clientes web
 * envian sus credenciales y reciben un token JWT firmado.
 *
 * Endpoint:
 * - POST /auth/login - Autentica al usuario y retorna un token JWT
 *
 * Ejemplo de uso con curl:
 *   curl -k -X POST https://localhost:8081/auth/login \
 *        -H "Content-Type: application/json" \
 *        -d '{"username": "usuario_web", "password": "1234"}'
 *
 * Respuesta exitosa:
 *   { "token": "eyJhbGciOiJIUzUxMiJ9..." }
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Autentica al usuario y genera un token JWT.
     *
     * Flujo:
     * 1. Recibe credenciales (username/password) en formato JSON
     * 2. Valida contra el InMemoryUserDetailsManager definido en SecurityConfig
     * 3. Si es valido, extrae el rol del usuario autenticado
     * 4. Genera un token JWT firmado con HS512 (valido por 30 minutos)
     * 5. Retorna el token en la respuesta
     *
     * @param request Objeto con username y password del usuario
     * @return ResponseEntity con el token JWT o error 401 si las credenciales son invalidas
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Validar credenciales usando Spring Security AuthenticationManager
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Obtener el primer rol del usuario autenticado
            String role = auth.getAuthorities().iterator().next().getAuthority();

            // Generar token JWT firmado con el username y rol
            String token = jwtUtil.generateToken(request.getUsername(), role);

            return ResponseEntity.ok(new LoginResponse(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Credenciales inválidas");
        }
    }

    /**
     * DTO interno para recibir la solicitud de login.
     * Formato esperado: { "username": "...", "password": "..." }
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * DTO interno para la respuesta de login.
     * Formato de respuesta: { "token": "eyJhbG..." }
     */
    public static class LoginResponse {
        private String token;

        public LoginResponse(String token) { this.token = token; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
