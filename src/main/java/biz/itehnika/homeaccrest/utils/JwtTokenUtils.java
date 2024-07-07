package biz.itehnika.homeaccrest.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtils {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.lifetime}")
    private Duration jwtLifetime;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> rolesList = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        claims.put("roles", rolesList);
        
        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + jwtLifetime.toMillis());
        return Jwts.builder()
            .claims(claims)
            .subject(userDetails.getUsername())
            .issuedAt(issuedDate)
            .expiration(expiredDate)
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()) , Jwts.SIG.HS256)
            .compact();
    }
    
    public String getUsername(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }
    
    public List<?> getRoles(String token) {
        return getAllClaimsFromToken(token).get("roles", List.class);
    }
    
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
