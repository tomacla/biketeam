package info.tomacla.biketeam.security.passkey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Reponse de {@code navigator.credentials.create()}, aplatie par le script client.
 * Les champs binaires sont en base64url.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PasskeyRegistrationBody(String attestationObject,
                                      String clientDataJSON,
                                      String clientExtensionResults,
                                      List<String> transports,
                                      String label) {
}
