<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form name="form-integration" action="<@spring.url '/' + team.id + '/admin/integration' />" method="POST" class="border rounded p-4">

            <h5>Facebook</h5>
            <div class="alert alert-info" role="alert">
               <p>L'intégration facebook permet de publier les rides et les publications sur une page Facebook.</p>
               <ul class="list-unstyled">
                   <#if facebookConfigurationStep gt 1>
                       <li><i class="bi bi-check-circle"></i> Etape 1 : <span class="badge bg-primary">Connecté à Facebook</span></li>
                   <#else>
                       <li>Etape 1 : Connexion avec Facebook</li>
                   </#if>
                   <#if facebookConfigurationStep gt 2>
                       <li><i class="bi bi-check-circle"></i> Etape 2 : <span class="badge bg-primary">Page-ID : ${formdata.facebookPageId}</span></li>
                       <li><span class="badge bg-success"><i class="bi bi-check-circle"></i> Configuration terminée</span></li>
                   <#else>
                       <li>Etape 2 : Page ID</li>
                   </#if>
               </ul>
               <#if facebookConfigurationStep gt 1>
                  <a class="btn btn-secondary btn-sm" href="<@spring.url '/' + team.id + '/admin/integration/facebook/backward' />" role="button">Etape précédente</a>
               </#if>
             </div>

            <#if facebookConfigurationStep == 1>

                <input name="facebookPageId" value="" type="hidden">

                <div class="mb-3">
                    <a class="d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#1877F2; border:1px solid #1877F2; border-radius:5px; width:250px;" href="${facebookUrl}" role="button">
                        <img src="<@spring.url '/css/facebooklogo.png' />" width="25" alt="Facebook logo">
                        <span>Connexion avec Facebook</span>
                    </a>
                </div>

            <#elseif facebookConfigurationStep == 2>

                <div class="mb-3">
                  <label for="form-integration-facebookPageId" class="form-label">Page ID</label>
                  <input autocomplete="off" name="facebookPageId" value="${formdata.facebookPageId}" type="text" class="form-control" id="form-integration-facebookPageId">
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