package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.reaction.Reaction;
import info.tomacla.biketeam.domain.reaction.ReactionHolder;
import info.tomacla.biketeam.domain.reaction.ReactionRepository;
import info.tomacla.biketeam.domain.team.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;

    public List<Reaction> listByTarget(ReactionHolder holder) {
        return reactionRepository.findAllByTargetIdAndType(holder.getId(), holder.getReactionType());
    }

    public Optional<Reaction> getReaction(String id) {
        return reactionRepository.findById(id);
    }

    public void save(Team team, ReactionHolder holder, Reaction reaction) {
        reactionRepository.save(reaction);
    }

    public void delete(String id) {
        getReaction(id).ifPresent(reactionRepository::delete);
    }

    public void deleteByUser(String userId) {
        reactionRepository.deleteByUserId(userId);
    }

    public void deleteByTarget(String targetId) {
        reactionRepository.deleteByTargetId(targetId);
    }

}
