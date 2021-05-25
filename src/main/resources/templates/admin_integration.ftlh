<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-integration" action="<@spring.url '/admin/integration' />" method="POST" class="border rounded p-4">
             <h5>MapBox</h5>
             <div class="alert alert-info" role="alert">
               Si une clé mapbox est renseignée, elle sera utilisée pour les fonds de carte au lieu du fond OpenStreeMap par défaut.
             </div>
            <div class="mb-3">
              <label for="form-integration-mapbox" class="form-label">Mapbox API Key</label>
              <input autocomplete="off" name="mapBoxAPIKey" value="${formdata.mapBoxAPIKey}" type="test" class="form-control" id="form-integration-mapBox">
            </div>

            <hr/>

            <h5>Facebook</h5>
            <div class="alert alert-info" role="alert">
               <p>L'intégration facebook permet de publier les rides et les publications sur une page Facebook.</p>
               <ul class="list-unstyled">
                   <#if facebookConfigurationStep gt 1>
                       <li><i class="bi bi-check-circle"></i> Etape 1 : <span class="badge bg-primary">App-ID : ${formdata.facebookAppId}</span> <span class="badge bg-primary">App-Secret : *****</span></li>
                   <#else>
                       <li>Etape 1 : App-ID et App-Secret</li>
                   </#if>
                   <#if facebookConfigurationStep gt 2>
                       <li><i class="bi bi-check-circle"></i> Etape 2 : <span class="badge bg-primary">Connecté à Facebook</span></li>
                   <#else>
                       <li>Etape 2 : Connexion avec Facebook</li>
                   </#if>
                   <#if facebookConfigurationStep gt 3>
                       <li><i class="bi bi-check-circle"></i> Etape 3 : <span class="badge bg-primary">Page-ID : ${formdata.facebookPageId}</span></li>
                       <li><span class="badge bg-success"><i class="bi bi-check-circle"></i> Configuration terminée</span></li>
                   <#else>
                       <li>Etape 3 : Page ID</li>
                   </#if>
               </ul>
               <#if facebookConfigurationStep gt 1>
                  <a class="btn btn-secondary btn-sm" href="<@spring.url '/admin/integration/facebook/backward' />" role="button">Etape précédente</a>
               </#if>
             </div>

            <#if facebookConfigurationStep == 1>

                <div class="mb-3">
                  <label for="form-integration-facebookAppId" class="form-label">App ID</label>
                  <input autocomplete="off" name="facebookAppId" value="${formdata.facebookAppId}" type="test" class="form-control" id="form-integration-facebookAppId">
                </div>
                <div class="mb-3">
                  <label for="form-integration-facebookAppSecret" class="form-label">App Secret</label>
                  <input autocomplete="off" name="facebookAppSecret" value="${formdata.facebookAppSecret}" type="test" class="form-control" id="form-integration-facebookAppSecret">
                </div>
                <input name="facebookPageId" value="" type="hidden">

            <#elseif facebookConfigurationStep == 2>

                <input name="facebookAppSecret" value="${formdata.facebookAppSecret}" type="hidden">
                <input name="facebookAppId" value="${formdata.facebookAppId}" type="hidden">
                <input name="facebookPageId" value="" type="hidden">

                <div class="mb-3">
                    <a class="btn btn-primary btn-sm" href="${facebookUrl}" role="button"><i class="bi bi-facebook"></i> Connecter Facebook</a>
                </div>

            <#elseif facebookConfigurationStep == 3>

                <input name="facebookAppSecret" value="${formdata.facebookAppSecret}" type="hidden">
                <input name="facebookAppId" value="${formdata.facebookAppId}" type="hidden">

                <div class="mb-3">
                  <label for="form-integration-facebookPageId" class="form-label">Page ID</label>
                  <input autocomplete="off" name="facebookPageId" value="${formdata.facebookPageId}" type="test" class="form-control" id="form-integration-facebookPageId">
                </div>

            <#elseif facebookConfigurationStep == 4>

                   <input name="facebookAppSecret" value="${formdata.facebookAppSecret}" type="hidden">
                   <input name="facebookAppId" value="${formdata.facebookAppId}" type="hidden">
                   <input name="facebookPageId" value="${formdata.facebookPageId}" type="hidden">

            </#if>

            <hr/>

            <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">