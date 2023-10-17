<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-integration" action="<@common.teamUrl team.id '/admin/integration' />" method="POST" class="border rounded p-4">

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
                <div class="row mb-3">
                    <div class="col-4">
                         <div class="form-check form-switch">
                             <input class="form-check-input" name="mattermostPublishRides" type="checkbox" id="form-configuration-mtrides"<#if formdata.mattermostPublishRides?? && formdata.mattermostPublishRides == 'on'> checked</#if>>
                             <label class="form-check-label" for="form-configuration-mtrides">Publier les rides</label>
                           </div>
                    </div>
                    <div class="col-4">
                         <div class="form-check form-switch">
                             <input class="form-check-input" name="mattermostPublishTrips" type="checkbox" id="form-configuration-mttrips"<#if formdata.mattermostPublishTrips?? && formdata.mattermostPublishTrips == 'on'> checked</#if>>
                             <label class="form-check-label" for="form-configuration-mttrips">Publier les trips</label>
                           </div>
                    </div>
                   <div class="col-4">
                         <div class="form-check form-switch">
                             <input class="form-check-input" name="mattermostPublishPublications" type="checkbox" id="form-configuration-mtpubs"<#if formdata.mattermostPublishPublications?? && formdata.mattermostPublishPublications == 'on'> checked</#if>>
                             <label class="form-check-label" for="form-configuration-mtpubs">Publier les publications</label>
                           </div>
                    </div>
                 </div>
                 <div class="mb-3">
                   <label for="form-integration-mattermostmessagechannelid" class="form-label">ID du channel (Notification de messages)</label>
                   <input autocomplete="off" name="mattermostMessageChannelID" value="${formdata.mattermostMessageChannelID}" type="text" class="form-control" id="form-integration-mattermostmessagechannelid">
                 </div>
            </div>

            <hr/>

            <h5>Webhook</h5>

            <div class="alert alert-info" role="alert">Si les webhooks sont configurés, les détails des rides/trips/publications seront envoyés en POST sur l'url saisie au moment de la publication.</div>

            <div class="mb-3">

                <div class="mb-3">
                  <label for="form-integration-webhookRide" class="form-label">Ride endpoint</label>
                  <input autocomplete="off" name="webhookRide" value="${formdata.webhookRide}" type="text" class="form-control" id="form-integration-webhookRide">
                </div>

                <div class="mb-3">
                  <label for="form-integration-webhookTrip" class="form-label">Trip endpoint</label>
                  <input autocomplete="off" name="webhookTrip" value="${formdata.webhookTrip}" type="text" class="form-control" id="form-integration-webhookTrip">
                </div>

                <div class="mb-3">
                  <label for="form-integration-webhookPublication" class="form-label">Publication endpoint</label>
                  <input autocomplete="off" name="webhookPublication" value="${formdata.webhookPublication}" type="text" class="form-control" id="form-integration-webhookPublication">
                </div>

            </div>

            <hr/>

            <div class="mb-3">
                <h5>Heatmap</h5>
                <div class="alert alert-info" role="alert">Pour activer la heatmap, renseignez les coordonnées du centre à utiliser pour la générer.</div>
                <div class="mb-3">
                    <div class="form-check form-switch">
                        <input class="form-check-input" name="heatmapDisplay" type="checkbox" id="form-configuration-heatmapDisplay"<#if formdata.heatmapDisplay?? && formdata.heatmapDisplay == 'on'> checked</#if>>
                        <label class="form-check-label" for="form-configuration-heatmapDisplay">Afficher la heatmap sur la page "Actualités"</label>
                      </div>
                 </div>
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