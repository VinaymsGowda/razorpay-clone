package com.vinayms.razorpayclone.common.audit;

import com.vinayms.razorpayclone.merchant.security.MerchantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditAwareImpl implements AuditorAware<String> {

    private final MerchantContext merchantContext;

    @Override
    public Optional<String> getCurrentAuditor() {
        try {

        if(merchantContext.getKeyId() != null){
            return Optional.of(merchantContext.getKeyId());
        }

        if(merchantContext.getMerchantId() != null){
            return Optional.of(merchantContext.getMerchantId().toString());
        }
        return Optional.empty();
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
        }
            return Optional.of("SYSTEM");

    }



}
