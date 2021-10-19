<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-integration" action="<@common.teamUrl team.id '/admin/integration' />" method="POST" class="border rounded p-4">

            <#if adminContact??>
                <div class="mb-3">
                    <h5>Domaine personnalisé</h5>
                    <p>Vous avez un nom de domaine que vous vous utilisez pour votre groupe. Envoyer un mail à ${adminContact} pour le configurer ensemble.</p>
                </div>
                <hr/>
            </#if>

            <h5>Facebook</h5>
            <#if adminContact?? && !team.integration.facebookPageId??>
                <p>La connexion avec Facebook permet d'envoyer les publications et les rides directement sur une page Facebook. Pour la configurer prenez contact avec ${adminContact}. La procédure est simple mais nécessite une intervention de votre part.</p>
            </#if>

            <#if team.integration.facebookPageId??>
                <p>La connexion avec Facebook est activée vers la page <a href="https://www.facebook.com/${team.integration.facebookPageId}" target="_blank">${team.integration.facebookPageId}</a>.</p>

                <div class="mb-3">
                    <div class="form-check form-switch">
                        <input class="form-check-input" name="facebookGroupDetails" type="checkbox" id="form-configuration-fbgroupdetails"<#if formdata.facebookGroupDetails?? && formdata.facebookGroupDetails == 'on'> checked</#if>>
                        <label class="form-check-label" for="form-configuration-fbgroupdetails">Détails des groupes dans les posts facebook</label>
                      </div>
                 </div>

            </#if>

            <hr/>

            <div class="mb-3">
                <h5>Mattermost</h5>
                <div class="mb-3">
                  <label for="form-integration-mattermostendpoint" class="form-label">API Endpoint</label>
                  <input autocomplete="off" name="mattermostApiEndpoint" value="${formdata.mattermostApiEndpoint}" type="text" class="form-control" id="form-integration-mattermostendpoint">
                </div>
                <div class="mb-3">
                  <label for="form-integration-mattermosttoken" class="form-label">Token API</label>
                  <input autocomplete="off" name="mattermostApiToken" value="${formdata.mattermostApiToken}" type="text" class="form-control" id="form-integration-mattermosttoken">
                </div>
                <div class="mb-3">
                  <label for="form-integration-mattermostchannelid" class="form-label">ID du channel</label>
                  <input autocomplete="off" name="mattermostChannelID" value="${formdata.mattermostChannelID}" type="text" class="form-control" id="form-integration-mattermostchannelid">
                </div>
            </div>

            <div class="mb-3">
                <h5>Heatmap</h5>
                <div class="alert alert-info" role="alert">Pour activer la heatmap, renseignez les coordonnées du centre à utiliser pour la générer.</div>
                <div class="row mb-3">
                    <div class="col-6">
                      <label for="form-integration-heatmaplat" class="form-label">Latitude</label>
                      <input autocomplete="off" name="heatmapCenterLat" value="${formdata.heatmapCenterLat}" type="number" min="-90" max="90" step="0.0000000000000001" class="form-control" id="form-integration-heatmapcenterlat">
                    </div>
                    <div class="col-6">
                      <label for="form-integration-heatmaplng" class="form-label">Longitude</label>
                      <input autocomplete="off" name="heatmapCenterLng" value="${formdata.heatmapCenterLng}" type="number" min="-180" max="180" step="0.0000000000000001" class="form-control" id="form-integration-heatmapcenterlng">
                    </div>
                </div>
            </div>

            <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">