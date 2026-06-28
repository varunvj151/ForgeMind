package com.forgemind.modules.team.repository;

import com.forgemind.modules.team.entity.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    
    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, Long userId);
    
    List<TeamMember> findAllByTeamId(UUID teamId);
    
    Page<TeamMember> findAllByTeamId(UUID teamId, Pageable pageable);
    
    Page<TeamMember> findAllByUserId(Long userId, Pageable pageable);
    
    boolean existsByTeamIdAndUserId(UUID teamId, Long userId);
}
