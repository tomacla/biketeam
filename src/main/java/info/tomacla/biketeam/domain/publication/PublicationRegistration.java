package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.datatype.Strings;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "publication_registration")
public class PublicationRegistration {

    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "publication_id")
    private Publication publication;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_email_valid")
    private boolean userEmailValid;

    @Column(name = "user_email_code")
    private String userEmailCode = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = Strings.requireNonBlank(userName, "Name is null");
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = Strings.requireEmail(userEmail);
    }

    public boolean isUserEmailValid() {
        return userEmailValid;
    }

    public void setUserEmailValid(boolean userEmailValid) {
        this.userEmailValid = userEmailValid;
    }

    public String getUserEmailCode() {
        return userEmailCode;
    }

    public void setUserEmailCode(String userEmailCode) {
        this.userEmailCode = userEmailCode;
    }
}
