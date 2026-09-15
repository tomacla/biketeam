<#include "_includes_head.ftlh">

<script type="text/javascript">

    function confirmDeleteUser() {
        return confirm('Votre compte sera définitivement supprimé ainsi que toutes les données associées. Confirmez vous ?');
    }

</script>

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-lg-10">

        <div class="alert alert-warning" role="alert">
          La connexion avec Strava sera supprimée prochainement. Pour continuer à utiliser ${_sitename},
          associez une adresse email et un mot de passe à votre compte, ou liez-le à Google ou Facebook.
        </div>

        <div class="row">

            <div class="col-12 col-md-6 mb-4">

                <div class="card h-100">
                    <h5 class="card-header">Email et mot de passe</h5>
                    <div class="card-body">

                        <h6>1. Vérifier mon adresse email</h6>

                        <#if pendingEmail??>

                            <div class="alert alert-info" role="alert">
                                En attente de vérification de ${pendingEmail}
                            </div>

                            <form name="form-complete-resend" method="POST" action="<@common.teamUrl '' '/account/complete/resend' />" class="d-inline">
                                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                                <button type="submit" class="btn btn-secondary btn-sm">Renvoyer le lien</button>
                            </form>

                            <button type="button" class="btn btn-outline-secondary btn-sm" data-bs-toggle="collapse" data-bs-target="#form-complete-email-block">Changer d'adresse</button>

                        <#elseif user.email?? && user.emailVerified>

                            <p>${user.email} <span class="badge bg-success">Vérifiée</span></p>

                        </#if>

                        <div class="<#if pendingEmail??>collapse mt-3<#else>mt-2</#if>" id="form-complete-email-block">

                            <form name="form-complete-email" method="POST" action="<@common.teamUrl '' '/account/complete/email' />">

                                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                                <div class="mb-3">
                                    <label for="form-complete-email-input" class="form-label">Adresse email</label>
                                    <input autocomplete="email" name="email" value="${formdata.email}" type="email" class="form-control" id="form-complete-email-input" required>
                                </div>

                                <button type="submit" class="btn btn-secondary">Envoyer le lien de vérification</button>

                            </form>

                        </div>

                        <hr/>

                        <h6>2. Choisir un mot de passe</h6>

                        <#if user.emailVerified>

                            <form name="form-complete-password" method="POST" action="<@common.teamUrl '' '/account/complete/password' />">

                                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                                <div class="mb-3">
                                    <label for="form-complete-password-input" class="form-label">Mot de passe</label>
                                    <input autocomplete="new-password" name="password" type="password" class="form-control" id="form-complete-password-input" required>
                                    <div class="form-text">Le mot de passe doit contenir au moins 10 caractères.</div>
                                </div>

                                <div class="mb-3">
                                    <label for="form-complete-password-confirm" class="form-label">Confirmation du mot de passe</label>
                                    <input autocomplete="new-password" name="passwordConfirm" type="password" class="form-control" id="form-complete-password-confirm" required>
                                </div>

                                <button type="submit" class="btn btn-primary">Enregistrer le mot de passe</button>

                            </form>

                        <#else>

                            <p class="text-muted">Vous devez d'abord vérifier votre adresse email.</p>

                        </#if>

                    </div>
                </div>

            </div>

            <div class="col-12 col-md-6 mb-4">

                <div class="card h-100">
                    <h5 class="card-header">Comptes externes</h5>
                    <div class="card-body">

                        <p class="text-muted">Vous pouvez également lier votre compte à Google ou Facebook.</p>

                        <div class="d-grid gap-2">
                            <a class="btn btn-outline-secondary" href="<@common.teamUrl '' '/account/link/google' />" role="button"><i class="bi bi-google"></i> Lier mon compte Google</a>
                            <a class="btn btn-outline-secondary" href="<@common.teamUrl '' '/account/link/facebook' />" role="button"><i class="bi bi-facebook"></i> Lier mon compte Facebook</a>
                        </div>

                    </div>
                </div>

            </div>

        </div>

        <div class="text-center">
            <a class="small link-secondary" href="<@common.teamUrl '' '/logout' />">Se déconnecter</a>
            <span class="text-muted px-2">·</span>
            <form name="form-account-delete" action="<@common.teamUrl '' '/users/me/delete' />" method="POST" onsubmit="return confirmDeleteUser();" class="d-inline">
                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                <button type="submit" class="btn btn-link btn-sm small link-danger p-0 align-baseline">Supprimer mon compte</button>
            </form>
        </div>

    </div>

</div>

<#include "_includes_tail.ftlh">
