<#include "_includes_head.ftlh">

<#macro accountCard title account teams>
    <div class="col-12 col-md-6 mb-3">
        <div class="border rounded p-3 h-100">
            <h6>${title}</h6>
            <dl class="mb-0">
                <dt>Identité</dt>
                <dd>${(account.firstName)!'inconnu'} ${(account.lastName)!'inconnu'}</dd>
                <dt>Email</dt>
                <#if account.email??>
                    <dd>${account.email}
                        <#if account.emailVerified>
                            <span class="badge bg-success">Vérifiée</span>
                        <#else>
                            <span class="badge bg-warning text-dark">Non vérifiée</span>
                        </#if>
                    </dd>
                <#else>
                    <dd class="text-muted">Aucune</dd>
                </#if>
                <dt>Équipes</dt>
                <#if teams?has_content>
                    <dd>
                        <#list teams as t>${t.name}<#sep>, </#sep></#list>
                    </dd>
                <#else>
                    <dd class="text-muted">Aucune</dd>
                </#if>
                <dt>Strava</dt>
                <#if account.stravaId??>
                    <dd>${account.stravaId?c}</dd>
                <#else>
                    <dd class="text-muted">Non lié</dd>
                </#if>
                <dt>Comptes liés</dt>
                <dd>
                    <#if account.googleId??><span class="badge bg-secondary">Google</span> </#if>
                    <#if account.facebookId??><span class="badge bg-secondary">Facebook</span> </#if>
                    <#if !account.googleId?? && !account.facebookId??><span class="text-muted">Aucun</span></#if>
                </dd>
            </dl>
        </div>
    </div>
</#macro>

<div class="mt-4 row justify-content-center">

    <div class="col-12 col-md-10">

        <div class="card">
            <h5 class="card-header text-center">Fusion de comptes</h5>
            <div class="card-body">

                <#if provider??>
                    <p>
                        Un autre compte utilise déjà cette identité
                        <#if provider == 'facebook'>Facebook<#else>Google</#if>.
                        Vous pouvez fusionner les deux comptes.
                    </p>
                <#else>
                    <p>
                        Vous avez demandé à rattacher votre compte actuel à un compte existant, et
                        vous venez de prouver que vous possédez les deux.
                    </p>
                </#if>

                <p>
                    Le compte conservé récupère de l'autre : ses équipes et ses rôles, ses
                    participations aux sorties et aux séjours, ses messages, ses notifications,
                    ses cartes favorites et ses notes de cartes. L'autre compte est ensuite
                    supprimé. Cette opération est définitive.
                </p>

                <#if privateTeamLost?? && privateTeamLost>
                    <div class="alert alert-danger" role="alert">
                        <strong>Attention :</strong> les deux comptes possèdent un espace personnel,
                        et un compte ne peut en avoir qu'un seul. L'espace personnel du compte
                        supprimé sera perdu, avec tout son contenu (cartes, sorties, séjours,
                        publications).
                    </div>
                </#if>

                <div class="row">
                    <@accountCard "Compte conservé" user userTeams />
                    <@accountCard "Compte supprimé" otherUser otherUserTeams />
                </div>

                <form name="form-account-merge" method="POST" action="<@common.teamUrl '' action!'/account/link-conflict' />">

                    <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                    <button type="submit" class="btn btn-primary">Fusionner ces deux comptes</button>
                    <a class="btn btn-secondary" href="<@common.teamUrl '' cancelUrl!'/users/me' />">Annuler</a>

                </form>

            </div>
        </div>

    </div>

</div>

<#include "_includes_tail.ftlh">
