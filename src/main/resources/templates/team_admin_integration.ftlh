<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-integration" action="<@common.teamUrl team.id '/admin/integration' />" method="POST" class="border rounded p-4">

            <h5>Domaine personnalisé</h5>
            <p>Vous avez un nom de domaine que vous vous utilisez pour votre groupe. Envoyer un mail à contact@biketeam.info pour le configurer ensemble.</p>

            <h5>Facebook</h5>
            <div class="mb-3">
                <div class="form-check form-switch">
                    <input class="form-check-input" name="facebookGroupDetails" type="checkbox" id="form-configuration-fbgroupdetails"<#if formdata.facebookGroupDetails?? && formdata.facebookGroupDetails == 'on'> checked</#if>>
                    <label class="form-check-label" for="form-configuration-fbgroupdetails">Détails des groupes dans les posts facebook</label>
                  </div>
             </div>
            <div class="alert alert-info" role="alert">
               <p>L'intégration facebook permet de publier les rides et les publications sur une page Facebook.</p>
               <ul class="list-unstyled">
                   <#if facebookConfigurationStep gt 1>
                       <li><i class="bi bi-check-circle"></i> Etape 1 : <span class="badge bg-primary">Connecté à Facebook (${facebookAccount})</span></li>
                   <#else>
                       <li>Etape 1 : Connexion avec Facebook</li>
                   </#if>
                   <#if facebookConfigurationStep gt 2>
                       <li><i class="bi bi-check-circle"></i> Etape 2 : <span class="badge bg-primary">Page-ID : ${formdata.facebookPageId}</span></li>
                       <li><span class="badge bg-success"><i class="bi bi-check-circle"></i> Configuration terminée</span></li>
                   <#else>
                       <li>Etape 2 : ID de la page facebook</li>
                   </#if>
               </ul>
               <#if facebookConfigurationStep == 2>
                  <a class="btn btn-warning btn-sm" href="<@common.teamUrl team.id '/admin/integration/facebook/backward' />" role="button">Changer d'utilisateur</a>
               </#if>
               <#if facebookConfigurationStep == 3>
                 <a class="btn btn-warning btn-sm" href="<@common.teamUrl team.id '/admin/integration/facebook/backward' />" role="button">Changer la page</a>
              </#if>
             </div>

            <#if facebookConfigurationStep == 1>

                <input name="facebookPageId" value="" type="hidden">

                <div class="mb-3">
                    <a class="d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#1877F2; border:1px solid #1877F2; border-radius:5px; width:250px;" href="${facebookUrl}" role="button">
                        <img src="<@common.teamUrl '' '/css/facebooklogo.png' />" width="25" alt="Facebook logo">
                        <span>Connexion avec Facebook</span>
                    </a>
                </div>

            <#elseif facebookConfigurationStep == 2>

                <div class="mb-3">
                  <label for="form-integration-facebookPageId" class="form-label">Page Facebook</label>
                   <div id="facebook-help" class="form-text">Vous devez avoir les droits de publication sur la page.</div>
                    <select class="form-select" name="facebookPageId" id="form-integration-facebookPageId">
                        <option value="">---</option>
                        <#list authorizedPages as authorizedPage>
                            <option value="${authorizedPage.id}">${authorizedPage.name}</option>
                        </#list>
                    </select>
                </div>

            <#elseif facebookConfigurationStep == 3>

                   <input name="facebookPageId" value="${formdata.facebookPageId}" type="hidden">

            </#if>



            <hr/>

            <button type="submit" class="btn btn-secondary">Enregistrer</button>
        </form>

    </div>

</div>

<#include "_includes_tail.ftlh">