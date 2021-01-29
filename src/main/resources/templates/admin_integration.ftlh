<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-integration" action="<@spring.url '/admin/integration' />" method="POST" class="border rounded p-4">
             <div class="alert alert-info" role="alert">
               Si une clé mapbox est renseignée, elle sera utilisée pour les fonds de carte au lieu du fond OpenStreeMap par défaut.
             </div>
            <div class="mb-3">
              <label for="form-integration-mapbox" class="form-label">Mapbox API Key</label>
              <input autocomplete="off" name="mapBoxAPIKey" value="${formdata.mapBoxAPIKey}" type="test" class="form-control" id="form-integration-mapBox">
            </div>
            <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">