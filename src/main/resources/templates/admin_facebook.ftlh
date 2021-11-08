<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <#if !accessToken??>
            <div class="mb-3">
                <a class="d-flex flex-row justify-content-between" style="padding:10px; text-decoration:none; font-weight:bold; background-color:#ffffff; color:#1877F2; border:1px solid #1877F2; border-radius:5px; width:250px;" href="${facebookUrl}" role="button">
                    <img src="<@common.teamUrl '' '/css/facebooklogo.png' />" width="25" alt="Facebook logo">
                    <span>Connexion avec Facebook</span>
                </a>
            </div>
        <#else>
            <div class="mb-3">
                <p>Connecté en tant que ${connectedAccount} - <a href="<@common.teamUrl '' '/admin/facebook/disconnect' />" class="btn btn-danger" type="button">Détruire le lien Facebook</a></p>

            </div>
            <#if teams?size gt 0>
                <form name="form-integration" action="<@common.teamUrl '' '/admin/facebook' />" method="POST" class="border rounded p-4">
                    <ul class="list-group mb-3">
                        <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                            <div>Groupe</div>
                            <div>Page Facebook</div>
                        <#list teams as team>
                            <li class="list-group-item d-flex justify-content-between align-items-center">
                                <div>${team.name}</div>
                                <div>
                                    <select class="form-select" name="facebook-${team.id}" id="form-facebook-${team.id}">
                                        <option value="">---</option>
                                        <#list authorizedPages as authorizedPage>
                                            <option<#if team.integration.facebookPageId?? && team.integration.facebookPageId == authorizedPage.id> selected</#if> value="${authorizedPage.id}">${authorizedPage.name}</option>
                                        </#list>
                                     </select>
                                </div>
                            </li>
                        </#list>
                    </ul>
                    <input type="submit" value="Enregistrer" class="btn btn-secondary">
                </form>
            <#else>
                <div class="alert alert-warning" role="alert">
                  Aucun groupe.
                </div>
            </#if>

        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">