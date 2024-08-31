package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.dto.InvalidTokenDTO;
import biz.itehnika.homeaccrest.models.InvalidToken;
import biz.itehnika.homeaccrest.repos.InvalidTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvalidTokenService {
    private final InvalidTokenRepository invalidTokenRepository;

    
    @Transactional(readOnly = true)
    public boolean existsByToken(String token){
        return invalidTokenRepository.existsInvalidTokenByToken(token);
    }
    
    @Transactional
    public void addToBlackList(InvalidTokenDTO invalidTokenDTO){
        invalidTokenRepository.save(new InvalidToken(null, invalidTokenDTO.getToken(), invalidTokenDTO.getValidDateTime()));
    }
    
    @Transactional
    public void cleanTokensWithInvalidDate(){
        List<InvalidToken> invalidTokens = invalidTokenRepository.findAll();
        for (InvalidToken invalidToken : invalidTokens){
            LocalDateTime validDateTime = invalidToken.getValidDateTime();
            if (validDateTime.isAfter(LocalDateTime.now())){
                invalidTokenRepository.delete(invalidToken);
            }
        }
    }
    
}
