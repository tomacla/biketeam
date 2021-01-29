<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-configuration" action="<@spring.url '/admin/configuration' />" method="POST" class="border rounded p-4">

                <div class="mb-3">
                  <label for="form-configuration-timezone" class="form-label">Timezone</label>
                    <select required="true" name="timezone" class="form-select" id="form-configuration-timezone">
                        <#list timezones as timezone>
                            <option <#if formdata.timezone == timezone>selected</#if> value="${timezone}">${timezone}</option>
                        </#list>
                    </select>
                </div>

                <div class="mb-3">
                  <label for="form-configuration-sound" class="form-label">Son du dindon</label>
                  <div class="alert alert-info" role="alert">
                       Le son du dindon est joué lorsque l'écran est secoué en version mobile (legacy N-Peloton).
                    </div>
                    <select required="true" name="soundEnabled" class="form-select" id="form-configuration-sound">
                        <option <#if formdata.soundEnabled>selected</#if> value="true">Activé</option>
                        <option <#if !formdata.soundEnabled>selected</#if> value="false">Désactivé</option>
                    </select>
                </div>

                <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">