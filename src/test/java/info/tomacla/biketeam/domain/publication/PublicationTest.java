package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PublicationTest extends AbstractDBTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Test
    public void publicationRepositoryTU() {

        List<Publication> publications = publicationRepository.findAll(new SearchPublicationSpecification(
                null, Set.of("publicationtest-team"), null,PublishedStatus.PUBLISHED, null, ZonedDateTime.now()
        ));

        assertEquals(0, publications.size());

        createPublication(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED);
        createPublication(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.UNPUBLISHED);
        createPublication(ZonedDateTime.now().plus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED);

        publications = publicationRepository.findAll(new SearchPublicationSpecification(
                null, Set.of("publicationtest-team"), null,PublishedStatus.PUBLISHED, null, ZonedDateTime.now()
        ));

        assertEquals(1, publications.size());


    }

    private void createPublication(ZonedDateTime publishedAt, PublishedStatus status) {
        Publication publication = new Publication();
        publication.setTeamId("publicationtest-team");
        publication.setTitle("test-pub");
        publication.setContent("description");
        publication.setPublishedAt(publishedAt);
        publication.setPublishedStatus(status);
        publicationRepository.save(publication);
    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("publicationtest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }

}
