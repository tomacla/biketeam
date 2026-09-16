<#include "_includes_head.ftlh">

<@common.csrfMeta />

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-6">
        <div class="card">
          <h5 class="card-header text-center">Connexion</h5>
          <div class="card-body">

            <#if loginLocked?? && loginLocked>
                <div class="alert alert-danger" role="alert">Trop de tentatives de connexion. Réessayez dans quelques minutes.</div>
            <#elseif loginError?? && loginError>
                <div class="alert alert-danger" role="alert">Email ou mot de passe incorrect.</div>
            </#if>

            <form name="form-login" method="POST" action="<@common.teamUrl '' '/login' />">

                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                <div class="mb-3">
                    <label for="form-login-email" class="form-label">Email</label>
                    <#-- "username webauthn" active la suggestion de passkey directement dans la
                         liste d'autocompletion du champ (remplissage conditionnel) -->
                    <input autocomplete="username webauthn" name="email" type="email" class="form-control" id="form-login-email" required>
                </div>

                <div class="mb-3">
                    <label for="form-login-password" class="form-label">Mot de passe</label>
                    <input autocomplete="current-password" name="password" type="password" class="form-control" id="form-login-password" required>
                </div>

                <button type="submit" class="btn btn-primary w-100">Se connecter</button>

            </form>

            <div class="mt-3" data-passkey-supported-only hidden>
                <button type="button"
                        class="btn btn-outline-primary w-100"
                        id="passkey-login-button"
                        data-options-url="<@common.teamUrl '' '/login/webauthn/options' />"
                        data-login-url="<@common.teamUrl '' '/login/webauthn' />"
                        data-status-id="passkey-login-status">
                    <i class="bi bi-fingerprint"></i> Se connecter avec une passkey
                </button>
                <div class="form-text mt-1 text-muted" id="passkey-login-status" hidden></div>
            </div>

            <div class="mt-3 d-flex flex-row justify-content-between">
                <a class="small link-secondary" href="<@common.teamUrl '' '/forgot-password' />">Mot de passe oublié ?</a>
                <a class="small link-secondary" href="<@common.teamUrl '' '/register' />">Créer un compte</a>
            </div>

            <hr/>
            <p class="text-muted text-center">ou</p>

            <a class="mx-auto mb-1 d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#fb5304; border:1px solid #fb5304; border-radius:5px; width:275px;" href="<@common.teamUrl '' '/oauth2/authorization/strava' />" role="button">
                <img src="<@common.teamUrl '' '/css/stravalogo.jpg' />" width="25" alt="Strava logo">
                <span>Connexion avec Strava</span>
            </a>
            <small class="text-muted d-block text-center mb-3">La connexion Strava sera supprimée prochainement.</small>
            <a class="mx-auto mb-3 d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#565656; border:1px solid #565656; border-radius:5px; width:275px;" href="<@common.teamUrl '' '/oauth2/authorization/google' />" role="button">
                <img src="<@common.teamUrl '' '/css/googlelogo.png' />" width="25" alt="Google logo">
                <span>Connexion avec Google</span>
            </a>
            <a class="mx-auto mb-3 d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#1877F2; border:1px solid #1877F2; border-radius:5px; width:275px;" href="<@common.teamUrl '' '/oauth2/authorization/facebook' />" role="button">
                <img src="<@common.teamUrl '' '/css/facebooklogo.png' />" width="25" alt="Facebook logo">
                <span>Connexion avec Facebook</span>
            </a>

          </div>
        </div>
    </div>

</div>

<#include "_includes_tail.ftlh">
