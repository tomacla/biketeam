/*
 * Passkeys (WebAuthn).
 *
 * Deux parcours, tous deux pilotes par des attributs data- poses dans les templates :
 *  - enregistrement depuis /users/me, reserve a un utilisateur deja connecte ;
 *  - connexion depuis /login, sans saisie d'identifiant (credential decouvrable).
 *
 * Les URL viennent des attributs data- et non de chemins relatifs : les pages peuvent etre
 * servies depuis le domaine personnalise d'une team, alors que les endpoints sont toujours
 * sur le domaine principal (celui auquel la passkey est liee).
 */
(function () {

    'use strict';

    function supported() {
        return typeof window.PublicKeyCredential !== 'undefined'
            && typeof navigator.credentials !== 'undefined'
            && typeof navigator.credentials.create === 'function';
    }

    /**
     * Les endpoints sont toujours sur le domaine principal, auquel la passkey est liee. Une page
     * atteinte depuis le domaine personnalise d'une team ferait donc une requete interdomaine :
     * le cookie de session ne partirait pas (credentials same-origin) et le challenge ne serait
     * jamais retrouve. Plutot que d'afficher un bouton qui echouerait, on ne l'affiche pas.
     */
    function sameOriginAsEndpoints(container) {
        const url = container.querySelector('[data-options-url]');
        if (!url) {
            return true;
        }
        return new URL(url.dataset.optionsUrl, window.location.href).origin === window.location.origin;
    }

    function base64UrlToBuffer(value) {
        const padded = value.replace(/-/g, '+').replace(/_/g, '/');
        const binary = window.atob(padded + '==='.slice((padded.length + 3) % 4));
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes.buffer;
    }

    function bufferToBase64Url(buffer) {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return window.btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    }

    function csrfHeaders() {
        const token = document.querySelector('meta[name="_csrf"]');
        const header = document.querySelector('meta[name="_csrf_header"]');
        const headers = {'Content-Type': 'application/json'};
        if (token && header && token.content) {
            headers[header.content] = token.content;
        }
        return headers;
    }

    function postJson(url, body) {
        return fetch(url, {
            method: 'POST',
            credentials: 'same-origin',
            headers: csrfHeaders(),
            body: body === undefined ? '{}' : JSON.stringify(body)
        }).then(function (response) {
            return response.json()
                .catch(function () {
                    return {};
                })
                .then(function (payload) {
                    if (!response.ok) {
                        throw new Error(payload.error || 'Une erreur est survenue.');
                    }
                    return payload;
                });
        });
    }

    function showStatus(element, message, isError) {
        if (!element) {
            return;
        }
        element.textContent = message || '';
        element.classList.toggle('text-danger', !!isError);
        element.classList.toggle('text-muted', !isError);
        element.hidden = !message;
    }

    /* --- enregistrement --------------------------------------------------- */

    function register(button) {

        const status = document.getElementById(button.dataset.statusId);
        const labelInput = document.getElementById(button.dataset.labelId);

        button.disabled = true;
        showStatus(status, 'Suivez les instructions de votre appareil…', false);

        postJson(button.dataset.optionsUrl)
            .then(function (options) {
                const publicKey = Object.assign({}, options);
                publicKey.challenge = base64UrlToBuffer(options.challenge);
                publicKey.user = Object.assign({}, options.user);
                publicKey.user.id = base64UrlToBuffer(options.user.id);
                publicKey.excludeCredentials = (options.excludeCredentials || []).map(function (descriptor) {
                    return Object.assign({}, descriptor, {id: base64UrlToBuffer(descriptor.id)});
                });
                return navigator.credentials.create({publicKey: publicKey});
            })
            .then(function (credential) {
                const response = credential.response;
                return postJson(button.dataset.registerUrl, {
                    attestationObject: bufferToBase64Url(response.attestationObject),
                    clientDataJSON: bufferToBase64Url(response.clientDataJSON),
                    clientExtensionResults: JSON.stringify(credential.getClientExtensionResults()),
                    transports: typeof response.getTransports === 'function' ? response.getTransports() : [],
                    label: labelInput ? labelInput.value : null
                });
            })
            .then(function () {
                // la liste des passkeys est rendue cote serveur : on la recharge plutot que de
                // la reconstruire en JavaScript, pour n'avoir qu'une seule source de verite
                window.location.reload();
            })
            .catch(function (error) {
                button.disabled = false;
                // NotAllowedError : l'utilisateur a ferme la fenetre ou laisse expirer le delai
                showStatus(status, error.name === 'NotAllowedError'
                    ? "L'enregistrement a été annulé."
                    : error.message, true);
            });

    }

    /* --- connexion -------------------------------------------------------- */

    /**
     * Parcours conditionnel en cours (remplissage automatique), ou null. Le navigateur n'accepte
     * qu'un seul navigator.credentials.get a la fois : le bouton doit l'annuler avant de lancer le
     * sien, sous peine de "A request is already pending". Le serveur ne garde de plus qu'un seul
     * challenge par session : on attend que la requete d'options du parcours conditionnel soit
     * terminee, pour qu'elle ne puisse pas ecraser celui demande par le bouton.
     */
    let conditional = null;

    function abortConditionalMediation() {
        if (!conditional) {
            return Promise.resolve();
        }
        const current = conditional;
        conditional = null;
        current.controller.abort();
        return current.done;
    }

    function buildAssertionRequest(options, mediation) {
        const publicKey = Object.assign({}, options);
        publicKey.challenge = base64UrlToBuffer(options.challenge);
        publicKey.allowCredentials = (options.allowCredentials || []).map(function (descriptor) {
            return Object.assign({}, descriptor, {id: base64UrlToBuffer(descriptor.id)});
        });
        const request = {publicKey: publicKey};
        if (mediation) {
            request.mediation = mediation;
        }
        return request;
    }

    function submitAssertion(credential, loginUrl) {
        const response = credential.response;
        return postJson(loginUrl, {
            credentialId: bufferToBase64Url(credential.rawId),
            userHandle: response.userHandle ? bufferToBase64Url(response.userHandle) : null,
            authenticatorData: bufferToBase64Url(response.authenticatorData),
            clientDataJSON: bufferToBase64Url(response.clientDataJSON),
            signature: bufferToBase64Url(response.signature),
            clientExtensionResults: JSON.stringify(credential.getClientExtensionResults())
        }).then(function (payload) {
            window.location.assign(payload.redirect || '/');
        });
    }

    function login(button) {

        const status = document.getElementById(button.dataset.statusId);

        button.disabled = true;
        showStatus(status, 'Suivez les instructions de votre appareil…', false);

        abortConditionalMediation()
            .then(function () {
                return postJson(button.dataset.optionsUrl);
            })
            .then(function (options) {
                return navigator.credentials.get(buildAssertionRequest(options, null));
            })
            .then(function (credential) {
                return submitAssertion(credential, button.dataset.loginUrl);
            })
            .catch(function (error) {
                button.disabled = false;
                showStatus(status, error.name === 'NotAllowedError'
                    ? 'La connexion a été annulée.'
                    : error.message, true);
                // le parcours conditionnel a ete annule par le clic : on le relance
                startConditionalMediation(button);
            });

    }

    /**
     * Remplissage automatique : le navigateur propose la passkey dans la liste de suggestions du
     * champ email (autocomplete="username webauthn"), sans clic prealable. Le parcours est
     * silencieux par construction - aucun message d'erreur n'est affiche - car il tourne en
     * arriere-plan et son abandon est le cas normal : l'utilisateur a simplement tape son mot
     * de passe. Le bouton reste la pour le declencher explicitement.
     */
    function startConditionalMediation(button) {

        if (typeof window.PublicKeyCredential.isConditionalMediationAvailable !== 'function') {
            return;
        }

        const controller = new AbortController();
        const current = {controller: controller, done: null};
        conditional = current;

        current.done = window.PublicKeyCredential.isConditionalMediationAvailable()
            .then(function (available) {
                if (!available || controller.signal.aborted) {
                    return;
                }
                // la requete d'options n'est pas annulable : le serveur enregistrerait quand meme
                // son challenge, et le bouton doit pouvoir attendre qu'elle soit vraiment finie
                return postJson(button.dataset.optionsUrl)
                    .then(function (options) {
                        if (controller.signal.aborted) {
                            return null;
                        }
                        const request = buildAssertionRequest(options, 'conditional');
                        request.signal = controller.signal;
                        return navigator.credentials.get(request);
                    })
                    .then(function (credential) {
                        if (credential) {
                            return submitAssertion(credential, button.dataset.loginUrl);
                        }
                    });
            })
            .catch(function () {
                /* abandon silencieux : voir le commentaire ci-dessus */
            })
            .then(function () {
                if (conditional === current) {
                    conditional = null;
                }
            });

    }

    document.addEventListener('DOMContentLoaded', function () {

        const containers = document.querySelectorAll('[data-passkey-supported-only]');
        if (!supported()) {
            containers.forEach(function (container) {
                container.hidden = true;
            });
            const unsupported = document.querySelectorAll('[data-passkey-unsupported-only]');
            unsupported.forEach(function (element) {
                element.hidden = false;
            });
            return;
        }

        containers.forEach(function (container) {
            container.hidden = !sameOriginAsEndpoints(container);
        });

        const registerButton = document.getElementById('passkey-register-button');
        if (registerButton && !registerButton.closest('[data-passkey-supported-only]').hidden) {
            registerButton.addEventListener('click', function () {
                register(registerButton);
            });
        }

        const loginButton = document.getElementById('passkey-login-button');
        if (loginButton && !loginButton.closest('[data-passkey-supported-only]').hidden) {
            loginButton.addEventListener('click', function () {
                login(loginButton);
            });
            startConditionalMediation(loginButton);
        }

    });

})();
