package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.common.Timezone;
import info.tomacla.biketeam.domain.user.UserRole;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private String country;
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
    @OneToMany(mappedBy = "id.teamId", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<UserRole> roles;

    protected Team() {

    }

    public Team(String id, String name, String city, String country,
                String defaultDescription, Set<UserRole> roles) {
        setId(id);
        setName(name);
        setCity(city);
        setCountry(country);
        this.roles = Objects.requireNonNullElse(roles, new HashSet<>());
        this.createdAt = LocalDate.now();

        TeamDescription description = new TeamDescription();
        description.setTeam(this);
        description.setDescription(defaultDescription);
        setDescription(description);

        TeamConfiguration configuration = new TeamConfiguration();
        configuration.setTeam(this);
        configuration.setTimezone(Timezone.DEFAULT_TIMEZONE);
        configuration.setDefaultSearchTags(new ArrayList<>());
        configuration.setFeedVisible(true);
        configuration.setRidesVisible(true);
        configuration.setDefaultPage(Page.FEED);
        setConfiguration(configuration);

        TeamIntegration teamIntegration = new TeamIntegration();
        teamIntegration.setTeam(this);
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
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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
        this.description = description;
    }

    public TeamConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TeamConfiguration configuration) {
        this.configuration = configuration;
    }

    public TeamIntegration getIntegration() {
        return integration;
    }

    public void setIntegration(TeamIntegration integration) {
        this.integration = integration;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

}
