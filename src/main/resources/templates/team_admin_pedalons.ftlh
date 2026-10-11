<#ftl output_format="HTML">
<#-- echappement force : le moteur n'active pas l'auto-echappement des .ftlh (incompatible_improvements
     par defaut), or cette page affiche des textes recus de Pedalons (erreurs, avertissements) -->
<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <h5 class="mb-3">Migrer vers Pédalons</h5>

        <#if pedalonsState == 'MIGRATED'>
            <div class="alert alert-success" role="alert">
                Ce groupe a été migré vers Pédalons<#if pedalonsUrl != ''> : <a href="${pedalonsUrl}" class="alert-link">${pedalonsUrl}</a></#if>.
                Toutes les adresses du groupe redirigent désormais vers Pédalons et le groupe est en lecture seule ici.
                Rien n'a été supprimé de biketeam.
            </div>
        <#elseif pedalonsState == 'READY'>
            <div class="border border-success rounded p-4 mb-4">
                <h6>Migration définitive terminée : le groupe est prêt à basculer</h6>
                <p>
                    Le groupe a été copié sur Pédalons<#if pedalonsUrl != ''> : <a href="${pedalonsUrl}" target="_blank">${pedalonsUrl}</a></#if>.
                    Vérifiez le bilan (compteurs et avertissements, dans l'historique ci-dessous) et le groupe sur Pédalons.
                    Tant que vous n'avez pas basculé, le groupe reste gelé en lecture seule (pour que rien ne manque sur Pédalons) et ses adresses ne sont pas redirigées. Un essai relancé d'ici là le dégèle.
                </p>
                <p class="small text-muted">
                    Une nouvelle migration (essai ou définitive) reste possible avant la bascule ; elle remplace celle-ci,
                    et seule une migration définitive réussie permet de basculer.
                </p>
                <form action="<@common.teamUrl team.id '/admin/pedalons/switch' />" method="POST"
                      onsubmit="return confirm('Basculer vers Pédalons : toutes les adresses du groupe redirigeront vers Pédalons et le groupe restera en lecture seule ici. Continuer ?');">
                    <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                    <button type="submit" class="btn btn-success">Basculer vers Pédalons</button>
                </form>
            </div>
        <#elseif pedalonsState == 'MIGRATING'>
            <div class="alert alert-warning" role="alert">
                Migration définitive en cours : le groupe est en lecture seule jusqu'à la fin de la migration.
            </div>
        </#if>

        <#if !pedalonsEnabled && pedalonsState != 'MIGRATED'>
            <div class="alert alert-secondary" role="alert">
                La migration vers Pédalons n'est pas activée sur ce site.
            </div>
        </#if>

        <#if running>
            <#list migrations as migration>
                <#if migration.active>
                    <div class="border rounded p-4 mb-4">
                        <h6>
                            <#if migration.dryRun>Essai<#else>Migration définitive</#if> en cours
                            <span class="badge bg-info text-dark">${migration.statusLabel}</span>
                        </h6>
                        <#if migration.phase != ''>
                            <p class="mb-1">Étape : <strong>${migration.phase}</strong><#if migration.total gt 0> — ${migration.done} / ${migration.total}</#if><#if migration.attempt gt 1> (tentative ${migration.attempt})</#if></p>
                            <#if migration.total gt 0>
                                <div class="progress mb-2">
                                    <div class="progress-bar" role="progressbar" style="width: ${((migration.done * 100 / migration.total)?round)?c}%"></div>
                                </div>
                            </#if>
                        <#else>
                            <p class="mb-1">En attente de démarrage sur Pédalons…</p>
                        </#if>
                        <#if migration.lastAttemptError != ''>
                            <p class="mb-1 text-warning-emphasis small">Tentative<#if migration.failedAttempt gt 0> ${migration.failedAttempt}</#if> échouée : ${migration.lastAttemptError} — nouvel essai automatique</p>
                        </#if>
                        <p class="small text-muted mb-0">Cette page se met à jour toute seule toutes les 10 secondes. La migration d'un groupe avec beaucoup de parcours peut prendre plusieurs minutes.</p>
                    </div>
                </#if>
            </#list>
        </#if>

        <#if pedalonsEnabled && pedalonsState != 'MIGRATED' && !pedalonsMigratable>
            <div class="alert alert-warning">L'identifiant de ce groupe (${team.id}) contient des caractères que Pédalons n'accepte pas : il ne peut pas être migré automatiquement.</div>
        </#if>

        <#if pedalonsEnabled && pedalonsState != 'MIGRATED' && !running && pedalonsMigratable>

            <form name="form-pedalons" action="<@common.teamUrl team.id '/admin/pedalons/start' />" method="POST" class="border rounded p-4 mb-4"
                  onsubmit="return confirmPedalonsMigration(this);">

                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>

                <p>
                    Le groupe sera copié vers Pédalons. Vous serez redirigé vers Pédalons, où vous devrez être connecté
                    (ou créer un compte) pour confirmer : ce compte Pédalons deviendra administrateur du groupe.
                </p>

                <#if summary??>
                    <h6>Ce qui sera migré</h6>
                    <ul>
                        <li>la description, la page FAQ<#if summary.faqPage> (présente)<#else> (absente)</#if> et le logo<#if summary.logo> (présent)<#else> (absent)</#if> du groupe ;</li>
                        <li>${summary.places} lieu(x) ;</li>
                        <li>${summary.routes} parcours ;</li>
                        <li>${summary.rides} ride(s) ;</li>
                        <li>${summary.rideTemplates} modèle(s) de ride ;</li>
                        <li>${summary.trips} trip(s) et ${summary.tripStages} étape(s) ;</li>
                        <li>${summary.publications} publication(s).</li>
                    </ul>
                </#if>

                <h6>Ce qui ne sera pas migré</h6>
                <p>
                    Les membres, les inscriptions aux rides et aux trips, les inscriptions aux publications,
                    les commentaires, les notes et les favoris de parcours. Les membres rejoindront le groupe
                    sur Pédalons par les moyens habituels (lien d'invitation, adhésion libre).
                </p>

                <div class="form-check mb-2">
                    <input class="form-check-input" type="checkbox" name="dryRun" value="true" id="form-pedalons-dryrun" checked>
                    <label class="form-check-label" for="form-pedalons-dryrun">Essai (recommandé)</label>
                    <div class="form-text">
                        Le groupe est réellement créé sur Pédalons pour vérification, mais biketeam ne redirige rien
                        et le groupe reste modifiable ici. Un essai peut être relancé autant de fois que nécessaire.
                    </div>
                </div>

                <div class="form-check mb-3">
                    <input class="form-check-input" type="checkbox" name="reset" value="true" id="form-pedalons-reset">
                    <label class="form-check-label" for="form-pedalons-reset">Réinitialiser le groupe sur Pédalons avant la migration</label>
                    <div class="form-text text-danger">
                        Le groupe déjà créé sur Pédalons par une migration précédente est mis à la corbeille et recréé
                        à partir de biketeam : tout ce qui y a été ajouté ou modifié depuis sur Pédalons sera perdu.
                    </div>
                </div>

                <div class="alert alert-warning small" role="alert">
                    Sans essai, la migration est <strong>définitive</strong> : le groupe passe en lecture seule pendant la
                    migration, et le reste jusqu'à la bascule. Une fois celle-ci réussie, vous lirez le bilan puis cliquerez sur « Basculer vers Pédalons » :
                    <strong>toutes les adresses du groupe redirigeront alors vers Pédalons</strong> et il restera en
                    lecture seule ici. Les données restent conservées sur biketeam.
                </div>

                <button type="submit" class="btn btn-primary">Migrer vers Pédalons</button>

            </form>

            <script type="text/javascript">
                function confirmPedalonsMigration(form) {
                    if (form.elements['dryRun'].checked) {
                        return true;
                    }
                    return confirm('Migration définitive : le groupe sera en lecture seule pendant la migration, puis jusqu\'à la bascule. La bascule vers Pédalons se fera ensuite, quand vous la déclencherez. Continuer ?');
                }
            </script>

        </#if>

        <#if migrations?size gt 0>
            <h6>Historique</h6>
            <#list migrations as migration>
                <div class="border rounded p-3 mb-3">
                    <div class="d-flex justify-content-between flex-wrap">
                        <div>
                            <strong><#if migration.dryRun>Essai<#else>Définitif</#if></strong><#if migration.reset> avec réinitialisation</#if>
                            — demandé le ${migration.createdAt}
                        </div>
                        <div>
                            <span class="badge <#if migration.status == 'SUCCEEDED'>bg-success<#elseif migration.status == 'FAILED'>bg-danger<#elseif migration.active>bg-info text-dark<#else>bg-secondary</#if>">${migration.statusLabel}</span>
                        </div>
                    </div>

                    <#if migration.finishedAt != ''><div class="small text-muted">Terminé le ${migration.finishedAt}</div></#if>
                    <#if migration.switchedAt != ''><div class="small text-muted">Bascule le ${migration.switchedAt}</div></#if>

                    <#if migration.errorCode != ''>
                        <div class="text-danger small mt-1">Erreur : ${migration.errorCode}<#if migration.errorMessage != ''> — ${migration.errorMessage}</#if></div>
                    </#if>

                    <#if migration.pedalonsTeamUrl != '' && migration.status == 'SUCCEEDED'>
                        <div class="mt-1"><a href="${migration.pedalonsTeamUrl}" target="_blank">Voir le groupe sur Pédalons</a></div>
                    </#if>

                    <#if migration.counts?size gt 0>
                        <table class="table table-sm small mt-2 mb-1">
                            <thead>
                                <tr><th></th><th class="text-end">Total</th><th class="text-end">Migrés</th><th class="text-end">Ignorés</th><th class="text-end">Échecs</th></tr>
                            </thead>
                            <tbody>
                                <#list migration.counts as count>
                                    <tr>
                                        <td>${count.label}</td>
                                        <td class="text-end">${count.total}</td>
                                        <td class="text-end">${count.migrated}</td>
                                        <td class="text-end">${count.skipped}</td>
                                        <td class="text-end<#if count.failed gt 0> text-danger</#if>">${count.failed}</td>
                                    </tr>
                                </#list>
                            </tbody>
                        </table>
                    </#if>

                    <#if migration.warnings?size gt 0>
                        <details class="small">
                            <summary>${migration.warnings?size} avertissement(s)<#if migration.warningsTruncated> (liste tronquée)</#if></summary>
                            <ul class="mb-0">
                                <#list migration.warnings as warning>
                                    <li>${warning.entityType} ${warning.biketeamId} — ${warning.code}<#if warning.message != ''> : ${warning.message}</#if></li>
                                </#list>
                            </ul>
                        </details>
                    </#if>
                </div>
            </#list>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">
