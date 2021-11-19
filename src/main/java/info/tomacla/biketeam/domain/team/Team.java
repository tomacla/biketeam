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

        setRoles(new HashSet<>());
        setCreatedAt(LocalDate.now(ZoneOffset.UTC));
        setVisibility(Visibility.PUBLIC);

        setDescription(new TeamDescription(this, defaultDescription));
        setConfiguration(new TeamConfiguration(this, timezone));
        setIntegration(new TeamIntegration(this));

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

    public ZoneId getZoneId() {
        return ZoneId.of(getConfiguration().getTimezone());
    }

    public boolean isAdmin(User user) {
        for (UserRole role : this.roles) {
            if (role.getUser().equals(user) && role.getRole().equals(Role.ADMIN)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMember(User user) {
        for (UserRole role : this.roles) {
            if (role.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    public void removeUser(User user) {
        this.roles.removeIf(role -> role.getUser().equals(user));
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
