<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-configuration" action="<@common.teamUrl team.id '/admin/configuration' />" method="POST" class="border rounded p-4">

                <#if adminContact?? && !domain??>
                    <div class="mb-3">
                        <h5>Domaine personnalisé</h5>
                        <p>Vous avez un nom de domaine que vous vous utilisez pour votre groupe. Envoyer un mail à ${adminContact} pour le configurer ensemble.</p>
                    </div>
                    <hr/>
                </#if>

                <#if domain??>
                    <div class="mb-3">
                        <h5>Domaine personnalisé</h5>
                        <p>Ce groupe est accessible sur le domaine ${domain}</p>
                    </div>
                    <hr/>
                </#if>

                <h5>Navigation</h5>

                <div class="mb-3">
                      <div class="form-check form-switch">
                        <input class="form-check-input" name="feedVisible" type="checkbox" id="form-configuration-feedvisible"<#if formdata.feedVisible?? && formdata.feedVisible == 'on'> checked</#if>>
                        <label class="form-check-label" for="form-configuration-feedvisible">Activer le flux d'actualités</label>
                      </div>
                </div>

                <h5>Emojis</h5>

                <div class="mb-3">
                      <div class="form-check form-switch">
                        <input class="form-check-input" name="reactionVisible" type="checkbox" id="form-configuration-reactionVisible"<#if formdata.reactionVisible?? && formdata.reactionVisible == 'on'> checked</#if>>
                        <label class="form-check-label" for="form-configuration-reactionVisible">Activer les emojis dans le flux</label>
                      </div>
                </div>

                <hr/>

                <h5>Localisation</h5>

                <div class="mb-3">
                  <label for="form-configuration-timezone" class="form-label">Timezone</label>
                    <select required name="timezone" class="form-select" id="form-configuration-timezone">
                        <#list timezones as timezone>
                            <option <#if formdata.timezone == timezone>selected</#if> value="${timezone}">${timezone}</option>
                        </#list>
                    </select>
                </div>

                <hr/>

                <h5>Préférences</h5>

                <div class="mb-3">
                    <label class="form-label" for="maps-filter-tags" class="form-label">Tags par défaut (recherche et création de maps)</label>
                    <div class="input-group">
                          <label class="input-group-text" for="maps-filter-tags" class="form-label">Tags</label>
                          <select multiple data-remove-first="true" name="defaultSearchTags" class="form-select" id="maps-filter-tags">
                            <option selected disabled hidden value="">Tag...</option>
                            <#list tags as tag>
                                <option <#if formdata.defaultSearchTags?seq_contains(tag)>selected</#if> value="${tag}">${tag}</option>
                            </#list>
                          </select>
                      </div>
                </div>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">