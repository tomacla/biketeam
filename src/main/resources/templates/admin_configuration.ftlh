<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-configuration" action="<@spring.url '/admin/configuration' />" method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-configuration-timezone" class="form-label">Timezone</label>
                    <select required name="timezone" class="form-select" id="form-configuration-timezone">
                        <#list timezones as timezone>
                            <option <#if formdata.timezone == timezone>selected</#if> value="${timezone}">${timezone}</option>
                        </#list>
                    </select>
                </div>

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