package biz.itehnika.homeaccrest.services;

import biz.itehnika.homeaccrest.dto.InvalidTokenDTO;
import biz.itehnika.homeaccrest.models.InvalidToken;
import biz.itehnika.homeaccrest.repos.InvalidTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
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
    
    @Scheduled(cron = "0 0 * * * *")
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
