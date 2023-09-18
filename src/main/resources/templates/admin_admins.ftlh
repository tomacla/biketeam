<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl '' '/admin/admins' />" class="mb-4">
            <div class="row">
                <div class="col-auto p-2">
                  <label for="form-user-stravaid" class="form-label">Strava ID</label>
                </div>
                <div class="col-auto">
                    <input autocomplete="off" name="stravaId" class="form-control" type="text" id="form-user-stravaid">
                </div>
                <div class="col-auto p-2">
                    <label for="form-user-email" class="form-label">ou Email</label>
                </div>
                <div class="col-auto">
                    <input autocomplete="off" name="email" class="form-control" type="email" id="form-user-email">
                </div>
            </div>
            <div class="row">
                <div class="col-auto">
                    <input type="submit" class="btn btn-sm btn-secondary" value="Ajouter comme administrateur">
                </div>
             </div>
        </form>

        <#if admins?size gt 0>
            <ul class="list-group">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Identité<span class="text-muted"> - ID Strava</span></div>
                    <div>Administrateur</div>
                </li>
                <#list admins as user>
                    <li class="small list-group-item d-flex justify-content-between align-items-center">
                        <div><img src="<@common.teamUrl '' '/users/${user.id}/image' />" alt="User photo" width="25" height="25" class="rounded"> ${user.firstName!'inconnu'} ${user.lastName!'inconnu'}<#if user.stravaId??><span class="text-muted"> - <a class="link-dark" href="https://www.strava.com/athletes/${user.stravaId?c}" target="_blank">${(user.stravaId?c)}</a></span></#if><#if user.email??><span class="text-muted"> - ${user.email}</span></#if><#if user.admin> <i class="bi bi-shield-fill"></i></#if><small> - ${user.id}</small></div>
                        <#if user.id == _user.id>
                            <button role="button" disabled="true" class="btn btn-sm btn-success"><i class="bi bi-shield-fill"></i></button>
                        <#else>
                            <div class="btn-group" role="group">
                               <a href="<@common.teamUrl '' '/admin/admins/relegate/${user.id}' />" class="btn btn-success btn-sm" role="button"><i class="bi bi-shield-fill"></i></a>
                            </div>
                        </#if>
                    </li>
                </#list>
            </ul>
        <#else>
            <div class="alert alert-warning" role="alert">
              Aucun administrateur.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">