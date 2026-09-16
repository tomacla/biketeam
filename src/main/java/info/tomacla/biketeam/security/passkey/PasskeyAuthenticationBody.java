package info.tomacla.biketeam.security.passkey;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Reponse de {@code navigator.credentials.get()}, aplatie par le script client.
 * Les champs binaires sont en base64url.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PasskeyAuthenticationBody(String credentialId,
                                        String userHandle,
                                        String authenticatorData,
                                        String clientDataJSON,
                                        String signature,
                                        String clientExtensionResults) {
}
