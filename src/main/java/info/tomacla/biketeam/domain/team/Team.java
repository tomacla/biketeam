package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.Country;
import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.domain.user.Role;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRole;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "team")
public class Team {

    @Id
    private String id;
    private String name;
    private String city;
    @Enumerated(EnumType.STRING)
    private Country country;
    @Column(name = "created_at")
    private LocalDate createdAt;
    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private TeamDescription description;
    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private TeamConfiguration configuration;
    @OneToOne(mappedBy = "team", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private TeamIntegration integration;
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<UserRole> roles;
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    public Team() {
    }

    public Team(String id,
                String name,
                String city,
                Country country,
                String timezone,
                String defaultDescription) {

        setId(id.toLowerCase());
        setName(name);
        setCity(city);
        setCountry(country);
        this.roles = new HashSet<>();
        this.createdAt = LocalDate.now(ZoneOffset.UTC);
        this.visibility = Visibility.PUBLIC;

        TeamDescription description = new TeamDescription(this);
        description.setDescription(defaultDescription);
        setDescription(description);

        TeamConfiguration configuration = new TeamConfiguration(this);
        configuration.setTimezone(timezone);
        setConfiguration(configuration);

        TeamIntegration teamIntegration = new TeamIntegration(this);
        setIntegration(teamIntegration);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = Strings.requireNonBlank(city, "city is null");
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = Objects.requireNonNull(country, "country is null");
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public TeamDescription getDescription() {
        return description;
    }

    public void setDescription(TeamDescription description) {
        this.description = Objects.requireNonNull(description);
    }

    public TeamConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TeamConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    public TeamIntegration getIntegration() {
        return integration;
    }

    public void setIntegration(TeamIntegration integration) {
        this.integration = Objects.requireNonNull(integration);
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = Objects.requireNonNull(visibility);
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = Objects.requireNonNullElse(roles, new HashSet<>());
    }

    public List<UserRole> getSortedRoles() {
        return roles.stream()
                .sorted(Comparator.comparing(UserRole::getUserId))
                .collect(Collectors.toList());
    }

    public void clearRoles() {
        this.roles.forEach(UserRole::removeTeam); // needed for hibernate
        this.roles.clear();
    }

    public void addRole(User user, Role role) {
        if (isMember(user.getId())) {
            this.roles.stream().filter(r -> r.getUserId().equals(user.getId())).findFirst().ifPresent(r -> r.setRole(role));
        } else {
            this.roles.add(new UserRole(this, user.getId(), role));
        }
    }

    public void removeRole(User user) {
        this.roles.stream().filter(role -> role.getUserId().equals(user.getId())).forEach(UserRole::removeTeam);
        this.roles.removeIf(role -> role.getUserId().equals(user.getId()));
    }

    public boolean isAdmin(String userId) {
        for (UserRole role : this.roles) {
            if (role.getUserId().equals(userId) && role.getRole().equals(Role.ADMIN)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMember(String userId) {
        for (UserRole role : this.roles) {
            if (role.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public ZoneId getZoneId() {
        return ZoneId.of(getConfiguration().getTimezone());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id.equals(team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
