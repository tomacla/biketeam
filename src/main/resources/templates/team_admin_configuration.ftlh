<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-configuration" action="<@spring.url '/' + team.id + '/admin/configuration' />" method="POST" class="border rounded p-4">

                <div class="mb-3">
                       <label for="form-configuration-defaultpage" class="form-label">Page par défaut</label>
                         <select name="defaultPage" class="form-select" id="form-configuration-defaultpage">
                             <option<#if formdata.defaultPage == 'FEED'> selected</#if> value="FEED">Accueil</option>
                             <option<#if formdata.defaultPage == 'RIDES'> selected</#if> value="RIDES">Rides</option>
                             <option<#if formdata.defaultPage == 'MAPS'> selected</#if> value="MAPS">Maps</option>
                         </select>
                </div>

                <div class="mb-3">
                      <div class="form-check form-switch">
                        <input class="form-check-input" name="feedVisible" type="checkbox" id="form-configuration-feedvisible"<#if formdata.feedVisible?? && formdata.feedVisible == 'on'> checked</#if>>
                        <label class="form-check-label" for="form-configuration-feedvisible">Activer la page "Accueil"</label>
                      </div>
                      <div class="form-check form-switch">
                        <input class="form-check-input" name="ridesVisible" type="checkbox" id="form-configuration-ridesvisible"<#if formdata.ridesVisible?? && formdata.ridesVisible == 'on'> checked</#if>>
                        <label class="form-check-label" for="form-configuration-ridesvisible">Activer la page "Rides"</label>
                      </div>
                </div>

                <hr/>

                <div class="mb-3">
                  <label for="form-configuration-timezone" class="form-label">Timezone</label>
                    <select required name="timezone" class="form-select" id="form-configuration-timezone">
                        <#list timezones as timezone>
                            <option <#if formdata.timezone == timezone>selected</#if> value="${timezone}">${timezone}</option>
                        </#list>
                    </select>
                </div>

                <hr/>

                <div class="mb-3">
                    <label class="form-label" for="maps-filter-tags" class="form-label">Tags par défaut pour la recherche</label>
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