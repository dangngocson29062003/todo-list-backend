package com.example.weaver.services;

import com.example.weaver.exceptions.BadRequestException;
import com.example.weaver.exceptions.ForbiddenException;
import com.example.weaver.models.Project;
import com.example.weaver.models.InviteLink;
import com.example.weaver.repositories.InviteLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteLinkService {
    private final InviteLinkRepository repository;

    public InviteLink getInviteLink(Project project) {
        Optional<InviteLink> optional = repository.findByProject_Id(project.getId());
        if (optional.isPresent()) {
            InviteLink inviteLink = optional.get();
            if (inviteLink.getExpiresAt().isAfter(Instant.now())) {
                return inviteLink;
            }
        }
        return create(project);
    }

    public InviteLink create(Project project){
        String token = generateToken();
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);

        InviteLink inviteLink = InviteLink.builder()
                .project(project)
                .token(token)
                .expiresAt(expiresAt)
                .build();
        return repository.save(inviteLink);
    }

    public InviteLink validateInviteLink(String token){
        Optional<InviteLink> projectInviteLink=repository.findByToken(token);
        if(projectInviteLink.isPresent()){
            if(projectInviteLink.get().getExpiresAt().isBefore(Instant.now())){
                throw new ForbiddenException("Invite link expired");
            }
            return projectInviteLink.get();
        }else {
            throw new BadRequestException("Invalid invite link");
        }
    }


    public String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24]; // 24 bytes ≈ 32 char string
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public void deleteExpiredLinks() {
        List<Long> links=repository.findExpiredIds(Instant.now(), PageRequest.of(0,1000));
        if(!links.isEmpty()){
            repository.deleteByIds(links);
        }
    }
}
