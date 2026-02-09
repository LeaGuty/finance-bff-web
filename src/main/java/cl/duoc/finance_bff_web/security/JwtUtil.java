package cl.duoc.finance_bff_web.security;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad para la generacion y validacion de tokens JWT.
 *
 * Gestiona todo el ciclo de vida del token:
 * - Generacion: Crea tokens firmados con HS512 que incluyen username y rol
 * - Validacion: Verifica firma, integridad y expiracion del token
 * - Extraccion: Permite obtener claims individuales (username, rol, expiracion)
 *
 * Configuracion:
 * - La clave secreta se carga desde el archivo .env (variable JWT_SECRET)
 * - El token tiene una validez de 30 minutos
 * - Algoritmo de firma: HMAC-SHA512 (HS512)
 *
 * Seguridad:
 * - La clave secreta debe estar codificada en Base64 en el archivo .env
 * - Si no se encuentra el .env, se usa una clave por defecto (solo para desarrollo)
 */
@Component
public class JwtUtil {

    /** Carga de variables de entorno desde el archivo .env en la raiz del proyecto */
    private final Dotenv dotenv = Dotenv.configure().directory("./").ignoreIfMissing().load();

    /** Clave secreta en formato String, leida desde la variable de entorno JWT_SECRET */
    private final String SECRET_KEY_STRING = dotenv.get("JWT_SECRET",
        "ZXN0YV9lc191bmFfY2xhdmVfbXV5X3NlZ3VyYV95X2xhcmdhX3BhcmFfY3VtcGxpcl9jb25fbG9zX3JlcXVpc2l0b3NfZGVfSFM1MTJfYmZmXzIwMjZfZHVvY19jaGlsZV9wYXJhX2VsX2V4YW1lbg==");

    /** Clave criptografica derivada de SECRET_KEY_STRING para firmar/verificar tokens */
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY_STRING));

    /** Tiempo de expiracion del token: 30 minutos (en milisegundos) */
    private final long EXPIRATION_TIME = 1000 * 60 * 30;

    /**
     * Genera un token JWT firmado con el username y rol del usuario.
     *
     * @param username Nombre de usuario que sera el subject del token
     * @param role     Rol del usuario (ej: "ROLE_CLIENTE_WEB"), se incluye como claim "role"
     * @return Token JWT firmado como String
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    /**
     * Construye y firma el token JWT con los claims proporcionados.
     *
     * @param claims  Mapa de claims personalizados (ej: role)
     * @param subject Username del usuario (subject del JWT)
     * @return Token JWT firmado
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Valida que el token pertenezca al usuario indicado y no haya expirado.
     *
     * @param token    Token JWT a validar
     * @param username Username esperado
     * @return true si el token es valido y pertenece al usuario
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Extrae el username (subject) del token JWT.
     *
     * @param token Token JWT
     * @return Username contenido en el token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el rol del usuario desde los claims del token.
     *
     * @param token Token JWT
     * @return Rol del usuario (ej: "ROLE_CLIENTE_WEB")
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extrae la fecha de expiracion del token.
     *
     * @param token Token JWT
     * @return Fecha de expiracion
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim especifico del token usando una funcion de resolucion.
     *
     * @param token          Token JWT
     * @param claimsResolver Funcion que extrae el claim deseado
     * @param <T>            Tipo del claim
     * @return Valor del claim extraido
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Decodifica y verifica el token, retornando todos sus claims.
     * Lanza excepcion si la firma es invalida o el token esta malformado.
     *
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
    }

    /**
     * Verifica si el token ha expirado comparando con la fecha actual.
     *
     * @param token Token JWT
     * @return true si el token ya expiro
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
