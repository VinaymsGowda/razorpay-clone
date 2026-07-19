package com.vinayms.razorpayclone.vault.service.impl;

import com.vinayms.razorpayclone.common.entity.Money;
import com.vinayms.razorpayclone.common.enums.CardBrand;

import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import com.vinayms.razorpayclone.common.exceptions.ResourceNotFoundException;
import com.vinayms.razorpayclone.common.util.RandomizerUtil;
import com.vinayms.razorpayclone.payment.processor.PaymentProcessor;
import com.vinayms.razorpayclone.payment.processor.PaymentProcessorFactory;
import com.vinayms.razorpayclone.payment.processor.dto.request.PaymentProcessorRequest;
import com.vinayms.razorpayclone.payment.processor.dto.response.PaymentProcessorResponse;
import com.vinayms.razorpayclone.vault.config.VaultEncryption;
import com.vinayms.razorpayclone.vault.dto.request.TokenizeReq;
import com.vinayms.razorpayclone.vault.dto.response.TokenizeResp;
import com.vinayms.razorpayclone.vault.entity.CardToken;
import com.vinayms.razorpayclone.vault.entity.VaultCard;
import com.vinayms.razorpayclone.vault.repository.CardTokenRepository;
import com.vinayms.razorpayclone.vault.repository.VaultCardRepository;
import com.vinayms.razorpayclone.vault.service.VaultService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.KeyGeneratorSpi;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultServiceImpl implements VaultService {

    private final VaultEncryption vaultEncryption;
    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final PaymentProcessorFactory paymentProcessorFactory;

    @Override
    public TokenizeResp tokenize(TokenizeReq req, UUID merchantId) {
        String lastFour=req.pan().substring(req.pan().length()-4);
        String bin=req.pan().substring(0,6);

        CardBrand cardBrand=detectBrand(req.pan());

        byte[] dek= KeyGenerators.secureRandom(32).generateKey();

        byte[] encryptedPan= VaultEncryption.
                panEncryptor(dek).
                encrypt(req.pan().getBytes(StandardCharsets.UTF_8));

        byte[] encryptedDek=vaultEncryption.dekEncryptor()
                .encrypt(dek);

        VaultCard card=VaultCard.builder()
                .bin(bin)
                .brand(cardBrand)
                .encryptedDek(encryptedDek)
                .encryptedPan(encryptedPan)
                .lastFourDigits(lastFour)
                .expMonth(req.expiry().getExpiryMonth())
                .expYear(req.expiry().getExpiryYear())
                .cardHolderName(req.cardHolderName())
                .build();

        String token= "token_"+RandomizerUtil.randomBase64(32);

        vaultCardRepository.save(card);

        CardToken cardToken=CardToken.builder()
                .token(token)
                .vaultCard(card)
                .customerId(req.customerId())
                .merchantId(merchantId)
                .build();

        cardTokenRepository.save(cardToken);
        return new TokenizeResp(
                token,
                req.expiry(),
                lastFour
        );


    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId,String token, Money amount, Map<String, Object> methodDetails
    ,PaymentMethod paymentMethod) {
        try {



        CardToken cardToken= cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"))
                ;

        VaultCard vaultCard=cardToken.getVaultCard();

        byte[] panBytes=null;

        byte[] dek=vaultEncryption.dekEncryptor()
                .decrypt(vaultCard.getEncryptedDek());
        panBytes=VaultEncryption.panEncryptor(dek)
                .decrypt(vaultCard.getEncryptedPan());

        String pan=new String(panBytes, StandardCharsets.UTF_8);

        String expiry=vaultCard.getExpMonth()+"/"+vaultCard.getExpYear();

        PaymentProcessorRequest processorRequest=PaymentProcessorRequest.card(
                paymentId,
                pan,
                expiry,
                amount,
                methodDetails
        );
        PaymentProcessor paymentProcessor=paymentProcessorFactory.getPaymentProcessor(
                paymentMethod
        );

        PaymentProcessorResponse paymentProcessorResponse=paymentProcessor.processPayment(
                processorRequest
        );
        log.info("Vault charge registered for token {} ", token);
        panBytes=null;

        return paymentProcessorResponse;
        } catch (RuntimeException e) {
            log.error("Error processing PaymentProcessorRequest", e);
            return new PaymentProcessorResponse.Failed(
                    "Vault charge failed for token "+token,
                    e.getMessage()
            );
        }

    }

    private CardBrand detectBrand(String pan) {

        if(pan.startsWith("4")) {
            return CardBrand.VISA;
        } else if(pan.startsWith("5") || pan.startsWith("2")) {
            return CardBrand.MASTERCARD;
        } else if(pan.startsWith("34") || pan.startsWith("37")) {
            return CardBrand.AMEX;
        } else {
            return CardBrand.RUPAY;
        }
    }
}
