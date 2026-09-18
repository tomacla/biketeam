package info.tomacla.biketeam.web.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.auth.BotProtectionService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.service.mail.MailSenderService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Inscription publique a une publication : formulaire anonyme qui envoie un mail de
 * confirmation, longtemps exploite par des robots. Il doit franchir la meme barriere anti-robot
 * que l'inscription au site, et n'etre accepte que si la publication ouvre les inscriptions.
 */
public class PublicationControllerTest {

    private PublicationService publicationService;
    private MailSenderService mailSenderService;
    private BotProtectionService botProtectionService;
    private RateLimitService rateLimitService;

    private Publication publication;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {

        publicationService = mock(PublicationService.class);
        mailSenderService = mock(MailSenderService.class);
        botProtectionService = mock(BotProtectionService.class);
        rateLimitService = mock(RateLimitService.class);

        PublicationController controller = new PublicationController();
        ReflectionTestUtils.setField(controller, "publicationService", publicationService);
        ReflectionTestUtils.setField(controller, "mailSenderService", mailSenderService);
        ReflectionTestUtils.setField(controller, "botProtectionService", botProtectionService);
        ReflectionTestUtils.setField(controller, "rateLimitService", rateLimitService);
        ReflectionTestUtils.setField(controller, "maxRegisterPerHour", 10);

        mockMvc = ControllerTestSupport.mockMvc(controller);

        Team team = new Team();
        team.setId("team");
        TeamService teamService = mock(TeamService.class);
        when(teamService.get("team")).thenReturn(Optional.of(team));
        ReflectionTestUtils.setField(controller, "teamService", teamService);

        publication = new Publication();
        ReflectionTestUtils.setField(publication, "id", "pub-1");
        publication.setPublishedStatus(PublishedStatus.PUBLISHED);
        publication.setAllowRegistration(true);
        when(publicationService.get("team", "pub-1")).thenReturn(Optional.of(publication));

        when(botProtectionService.check(any(), any(), any())).thenReturn(BotProtectionService.Verdict.HUMAN);
        when(rateLimitService.tryAcquire(any(), anyInt())).thenReturn(true);
        when(rateLimitService.clientKey(any(), anyString())).thenAnswer(i -> i.getArgument(1) + ":1.2.3.4");

    }

    private MockHttpServletRequestBuilder register() {
        return post("/team/publications/pub-1/register")
                .param("firstname", "Jean")
                .param("lastname", "Dupont")
                .param("email", "jean@example.com")
                .param("altcha", "payload")
                .param("website", "")
                .param("formStamp", "stamp");
    }

    private void verifyNothingRegistered() {
        verifyNoInteractions(mailSenderService);
        verify(publicationService, never()).save(any());
        assertTrue(publication.getRegistrations().isEmpty());
    }

    @Test
    public void testHumanRegistrationIsRecordedAndConfirmed() throws Exception {

        mockMvc.perform(register())
                .andExpect(redirectedUrl("/team/publications/pub-1"))
                .andExpect(flash().attributeExists("infos"));

        verify(botProtectionService).check("payload", "", "stamp");
        verify(rateLimitService).tryAcquire("publication-register:1.2.3.4", 10);
        verify(mailSenderService).sendDirectly(isNull(), eq(Set.of("jean@example.com")), eq("Confirmation requise"), anyString(), isNull());
        verify(publicationService).save(publication);
        assertEquals(1, publication.getRegistrations().size());

    }

    @Test
    public void testHoneypotFakesASuccessfulRegistration() throws Exception {

        when(botProtectionService.check(any(), any(), any())).thenReturn(BotProtectionService.Verdict.HONEYPOT);

        mockMvc.perform(register())
                .andExpect(redirectedUrl("/team/publications/pub-1"))
                .andExpect(flash().attributeExists("infos"))
                .andExpect(flash().attributeCount(1));

        verifyNothingRegistered();

    }

    @Test
    public void testFailedBotControlShowsAnError() throws Exception {

        for (BotProtectionService.Verdict verdict : List.of(BotProtectionService.Verdict.TOO_FAST,
                BotProtectionService.Verdict.CHALLENGE_FAILED)) {

            when(botProtectionService.check(any(), any(), any())).thenReturn(verdict);

            mockMvc.perform(register())
                    .andExpect(redirectedUrl("/team/publications/pub-1"))
                    .andExpect(flash().attribute("error", BotProtectionService.errorMessage(verdict)));

        }

        verifyNothingRegistered();
        verifyNoInteractions(rateLimitService);

    }

    @Test
    public void testRateLimitedRegistrationSendsNoMail() throws Exception {

        when(rateLimitService.tryAcquire(any(), anyInt())).thenReturn(false);

        mockMvc.perform(register())
                .andExpect(redirectedUrl("/team/publications/pub-1"))
                .andExpect(flash().attributeExists("error"));

        verifyNothingRegistered();

    }

    /**
     * Le formulaire n'est affiche que si la publication ouvre les inscriptions : un envoi direct
     * sur une publication fermee est ignore, avant meme le controle anti-robot.
     */
    @Test
    public void testClosedRegistrationIsIgnored() throws Exception {

        publication.setAllowRegistration(false);

        mockMvc.perform(register())
                .andExpect(redirectedUrl("/team/publications/pub-1"));

        verifyNothingRegistered();
        verifyNoInteractions(botProtectionService);

    }

    @Test
    public void testUnpublishedPublicationIsIgnored() throws Exception {

        publication.setPublishedStatus(PublishedStatus.UNPUBLISHED);

        mockMvc.perform(register())
                .andExpect(redirectedUrl("/team/publications/pub-1"));

        verifyNothingRegistered();

    }

}
