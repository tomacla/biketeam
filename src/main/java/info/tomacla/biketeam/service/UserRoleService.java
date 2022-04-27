package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.domain.userrole.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserRoleService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    public Optional<UserRole> get(Team team, User user) {
        return userRoleRepository.findById(UserRole.getId(team, user));
    }

    @Transactional
    public void save(UserRole userRole) {
        userRoleRepository.save(userRole);
    }

    @Transactional
    public void delete(Team team, User user) {
        get(team, user).ifPresent(userRole -> {
            team.removeUser(user);
            user.removeTeam(team);
            userRoleRepository.delete(userRole);
        });
    }

    public void deleteByTeam(String teamId) {
        userRoleRepository.findByTeam_Id(teamId).forEach(ur -> delete(ur.getTeam(), ur.getUser()));
    }

    public void deleteByUser(String userId) {
        userRoleRepository.deleteByUserId(userId);
    }

}
