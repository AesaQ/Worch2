package com.worch.repository;

import com.worch.model.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    @Query("""
    select v.option.id, count(v)
    from Vote v
    where v.option.id in :optionIds
    group by v.option.id """)
    List<Object[]> countVotesPerOption(@Param("optionIds") List<UUID> optionIds);

    @Query("""
        select v.option.id
        from Vote v
        where v.choice.id = :choiceId
          and v.user.id = :userId
    """)
    Optional<UUID> findVotedOptionId(@Param("choiceId") UUID choiceId,
                                     @Param("userId") UUID userId);
}
