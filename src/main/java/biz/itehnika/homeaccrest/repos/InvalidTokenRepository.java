package biz.itehnika.homeaccrest.repos;

import biz.itehnika.homeaccrest.models.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken, Long> {
    
    boolean existsInvalidTokenByToken(String token);
    
}
