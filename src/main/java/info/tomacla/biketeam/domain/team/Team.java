package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.data.Country;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "team")
public class Team {

    @Id
    private String id = UUID.randomUUID().toString();
    private String name;
    private String city;
    @Enumerated(EnumType.STRING)
    private Country country = Country.FR;
    @Column(name = "created_at")
    private LocalDate createdAt = LocalDate.now(ZoneOffset.UTC);
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
    private Set<UserRole> roles = new HashSet<>();
    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    public Team() {
        setConfiguration(new TeamConfiguration());
        setIntegration(new TeamIntegration());
        setDescription(new TeamDescription());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Strings.requireNonBlank(id, "id is null").toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Strings.requireNonBlank(name, "name is null");
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
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public TeamDescription getDescription() {
        return description;
    }

    public void setDescription(TeamDescription description) {
        this.description = Objects.requireNonNull(description);
        this.description.setTeam(this);
    }

    public TeamConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TeamConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
        this.configuration.setTeam(this);
    }

    public TeamIntegration getIntegration() {
        return integration;
    }

    public void setIntegration(TeamIntegration integration) {
        this.integration = Objects.requireNonNull(integration);
        this.integration.setTeam(this);
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = Objects.requireNonNullElse(visibility, Visibility.PUBLIC);
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = Objects.requireNonNullElse(roles, new HashSet<>());
    }

    public ZoneId getZoneId() {
        return ZoneId.of(getConfiguration().getTimezone());
    }

    public boolean isAdmin(User user) {
        if (user.isAdmin()) {
            return true;
        }
        return this.roles.stream().anyMatch(role -> role.getUser().equals(user) && role.getRole().equals(Role.ADMIN));
    }

    public boolean isMember(User user) {
        return this.roles.stream().anyMatch(role -> role.getUser().equals(user));
    }

    public void removeUser(User user) {
        this.roles.remove(user);
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
