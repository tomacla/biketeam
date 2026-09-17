<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

     <script type="text/javascript">

            function confirmDeleteUser() {
                return confirm('Votre compte sera définitivement supprimé ainsi que toutes les données associées. Confirmez vous ?');
            }

        </script>

    <div class="col-12 col-md-4 mb-4">

        <div class="border rounded p-2">

            <h5>${(user.firstName)!'inconnu'} ${(user.lastName)!'inconnu'}</h5>
            <div class="mb-3">
               <img class="rounded" src="<@common.teamUrl '' '/users/${user.id}/image' />" alt="profile">
            </div>
            <dl class="mb-3">
                <dt>Ville</dt>
                <#if user.city??>
                    <dd>${user.city}</dd>
                <#else>
                    <dd class="text-muted">Non renseignée</dd>
                </#if>
                <hr/>
                <form name="form-user-delete" action="<@common.teamUrl '' '/users/me/delete' />" method="POST" onsubmit="return confirmDeleteUser();" class="d-inline">
                    <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                    <button type="submit" class="btn btn-danger btn-sm"><i class="bi bi-trash"></i> Supprimer mon compte</button>
                </form>
                <hr/>
                <dt><i class="bi bi-strava"></i> Strava</dt>
                <#if user.stravaId??>
                        <dd><a class="link-dark" href="https://www.strava.com/athletes/${user.stravaId?c}" target="_blank">${(user.stravaId?c)}</a></dd>
                <#else>
                    <dd class="text-muted">Non connecté</dd>
                </#if>
                <dd><small class="text-muted">Connexion de transition, bientôt supprimée.</small></dd>
                <dt><i class="bi bi-google"></i> Google</dt>
                <#if user.googleId??>
                    <dd>Connecté</dd>
                <#else>
                    <dd class="text-muted">Non connecté</dd>
                </#if>
                <dt><i class="bi bi-facebook"></i> Facebook</dt>
                <#if user.facebookId??>
                        <dd>Connecté</dd>
                <#else>
                    <dd class="text-muted">Non connecté</dd>
                </#if>

            </dl>

        </div>

    </div>

    <div class="col-12 col-md-8">

        <form name="form-user" action="<@common.teamUrl '' '/users/me' />" method="POST" class="border rounded p-4">

               <h4>Préférences</h4>

             <h5>Notifications par email</h5>

             <div class="alert alert-info" role="alert">
                 Les notifications sont envoyées à l'adresse email de votre compte.
               </div>

               <div class="row mb-3">

                  <div class="col-4">
                       <div class="form-check form-switch">
                           <input class="form-check-input" name="emailPublishRides" type="checkbox" id="form-configuration-emailrides"<#if formdata.emailPublishRides?? && formdata.emailPublishRides == 'on'> checked</#if>>
                           <label class="form-check-label" for="form-configuration-emailrides">Recevoir les rides</label>
                         </div>
                  </div>

                  <div class="col-4">
                       <div class="form-check form-switch">
                           <input class="form-check-input" name="emailPublishTrips" type="checkbox" id="form-configuration-emailtrips"<#if formdata.emailPublishTrips?? && formdata.emailPublishTrips == 'on'> checked</#if>>
                           <label class="form-check-label" for="form-configuration-emailtrips">Recevoir les trips</label>
                         </div>
                  </div>

                  <div class="col-4">
                       <div class="form-check form-switch">
                           <input class="form-check-input" name="emailPublishPublications" type="checkbox" id="form-configuration-emailpubs"<#if formdata.emailPublishPublications?? && formdata.emailPublishPublications == 'on'> checked</#if>>
                           <label class="form-check-label" for="form-configuration-emailpubs">Recevoir les publications</label>
                         </div>
                  </div>

              </div>

              <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

        <div class="border rounded p-4 mt-4">

            <h4>Sécurité</h4>

            <h5>Adresse email</h5>

            <#if user.email??>
                <p>
                    ${user.email}
                    <#if user.emailVerified>
                        <span class="badge bg-success">Vérifiée</span>
                    <#else>
                        <span class="badge bg-warning text-dark">Non vérifiée</span>
                    </#if>
                </p>
            <#else>
                <p class="text-muted">Aucune adresse email enregistrée.</p>
            </#if>

            <#if pendingEmail??>
                <div class="alert alert-info" role="alert">
                    En attente de vérification de ${pendingEmail}.
                </div>
            </#if>

            <#if pendingEmail?? || (user.email?? && !user.emailVerified)>
            <form name="form-user-email-resend" action="<@common.teamUrl '' '/users/me/email/resend' />" method="POST" class="mb-3">
                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                <button type="submit" class="btn btn-secondary btn-sm">Renvoyer le lien de vérification</button>
            </form>
            </#if>

            <form name="form-user-email" action="<@common.teamUrl '' '/users/me/email' />" method="POST" class="mb-3">

                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                <div class="mb-3">
                    <label for="form-user-new-email" class="form-label">Modifier mon adresse</label>
                    <input autocomplete="email" name="email" type="email" class="form-control" id="form-user-new-email" required>
                    <div class="form-text">L'adresse actuelle reste votre identifiant de connexion jusqu'à la confirmation de la nouvelle.</div>
                </div>

                <#if user.passwordHash??>
                    <div class="mb-3">
                        <label for="form-user-email-current-password" class="form-label">Mot de passe actuel</label>
                        <input autocomplete="current-password" name="currentPassword" type="password" class="form-control" id="form-user-email-current-password" required>
                        <div class="form-text">L'adresse email est un identifiant de connexion : sa modification demande votre mot de passe.</div>
                    </div>
                </#if>

                <button type="submit" class="btn btn-secondary">Envoyer le lien de vérification</button>

            </form>

            <hr/>

            <#if user.passwordHash??>
                <h5>Modifier mon mot de passe</h5>
            <#else>
                <h5>Définir un mot de passe</h5>
                <#if !user.emailVerified>
                    <div class="alert alert-warning" role="alert">Vous devez d'abord vérifier votre adresse email.</div>
                </#if>
            </#if>

            <form name="form-user-password" action="<@common.teamUrl '' '/users/me/password' />" method="POST">

                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                <#if user.passwordHash??>
                    <div class="mb-3">
                        <label for="form-user-current-password" class="form-label">Mot de passe actuel</label>
                        <input autocomplete="current-password" name="currentPassword" type="password" class="form-control" id="form-user-current-password" required>
                    </div>
                </#if>

                <div class="mb-3">
                    <label for="form-user-password" class="form-label">Nouveau mot de passe</label>
                    <input autocomplete="new-password" name="password" type="password" class="form-control" id="form-user-password" required>
                    <div class="form-text">Le mot de passe doit contenir au moins 10 caractères.</div>
                </div>

                <div class="mb-3">
                    <label for="form-user-password-confirm" class="form-label">Confirmation du mot de passe</label>
                    <input autocomplete="new-password" name="passwordConfirm" type="password" class="form-control" id="form-user-password-confirm" required>
                </div>

                <button type="submit" class="btn btn-secondary"<#if !user.passwordHash?? && !user.emailVerified> disabled</#if>>Enregistrer le mot de passe</button>

            </form>

            <hr/>

            <h5>Passkeys</h5>

            <p class="text-muted small">
                Une passkey vous identifie avec vos appareils, sans mot de passe à saisir.
                Votre mot de passe et vos comptes liés restent actifs : une passkey s'ajoute
                à vos moyens de connexion, elle n'en remplace aucun.
            </p>

            <#if passkeys?has_content>
                <ul class="list-group mb-3">
                    <#list passkeys as passkey>
                        <li class="list-group-item d-flex flex-row justify-content-between align-items-center">
                            <div>
                                <div>${passkey.label}</div>
                                <div class="small text-muted">
                                    Ajoutée le ${passkey.createdAtUtc.format(_date_formatter)}
                                    <#if passkey.lastUsedAtUtc??>
                                        &middot; utilisée le ${passkey.lastUsedAtUtc.format(_date_formatter)}
                                    <#else>
                                        &middot; jamais utilisée
                                    </#if>
                                    <#if passkey.syncedAcrossDevices>
                                        &middot; synchronisée sur vos appareils
                                    </#if>
                                </div>
                            </div>
                            <form name="form-user-passkey-delete-${passkey?index}" class="d-inline"
                                  action="<@common.teamUrl '' '/users/me/passkeys/${passkey.id}/delete' />" method="POST">
                                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                                <button type="submit" class="btn btn-outline-secondary btn-sm">Supprimer</button>
                            </form>
                        </li>
                    </#list>
                </ul>
            <#else>
                <p class="text-muted">Aucune passkey enregistrée.</p>
            </#if>

            <div data-passkey-supported-only hidden>
                <div class="mb-3">
                    <label for="passkey-label" class="form-label">Nom de cette passkey</label>
                    <input type="text" class="form-control" id="passkey-label" maxlength="80"
                           placeholder="Mon téléphone">
                    <div class="form-text">Sert uniquement à la reconnaître dans cette liste.</div>
                </div>
                <button type="button" class="btn btn-secondary" id="passkey-register-button"
                        data-passkey-register
                        data-options-url="<@common.teamUrl '' '/users/me/passkeys/options' />"
                        data-register-url="<@common.teamUrl '' '/users/me/passkeys' />"
                        data-label-id="passkey-label"
                        data-status-id="passkey-register-status">
                    Ajouter une passkey
                </button>
                <div class="form-text mt-1 text-muted" id="passkey-register-status" hidden></div>
            </div>

            <div class="alert alert-secondary" data-passkey-unsupported-only hidden>
                Ce navigateur ne gère pas les passkeys.
            </div>

            <hr/>

            <h5>Comptes liés</h5>

            <dl class="mb-0">

                <dt><i class="bi bi-google"></i> Google</dt>
                <#if user.googleId??>
                    <dd>
                        Lié
                        <form name="form-user-unlink-google" action="<@common.teamUrl '' '/users/me/unlink/google' />" method="POST" class="d-inline">
                            <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                            <button type="submit" class="btn btn-outline-secondary btn-sm">Délier</button>
                        </form>
                    </dd>
                <#else>
                    <dd>
                        <span class="text-muted">Non lié</span>
                        <a class="btn btn-outline-secondary btn-sm" href="<@common.teamUrl '' '/account/link/google' />" role="button">Lier mon compte Google</a>
                    </dd>
                </#if>

                <dt><i class="bi bi-facebook"></i> Facebook</dt>
                <#if user.facebookId??>
                    <dd>
                        Lié
                        <form name="form-user-unlink-facebook" action="<@common.teamUrl '' '/users/me/unlink/facebook' />" method="POST" class="d-inline">
                            <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                            <button type="submit" class="btn btn-outline-secondary btn-sm">Délier</button>
                        </form>
                    </dd>
                <#else>
                    <dd>
                        <span class="text-muted">Non lié</span>
                        <a class="btn btn-outline-secondary btn-sm" href="<@common.teamUrl '' '/account/link/facebook' />" role="button">Lier mon compte Facebook</a>
                    </dd>
                </#if>

                <dt><i class="bi bi-strava"></i> Strava</dt>
                <#if user.stravaId??>
                    <dd>Lié <small class="text-muted">Connexion de transition, bientôt supprimée.</small></dd>
                <#else>
                    <dd class="text-muted">Non lié</dd>
                </#if>

            </dl>

        </div>

    </div>

</div>

<#include "_includes_tail.ftlh">
