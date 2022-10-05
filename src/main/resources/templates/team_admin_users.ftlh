<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl team.id '/admin/users' />" class="row mb-0">
            <div class="col-auto p-2">
              <label for="form-user-stravaid" class="form-label">Strava ID</label>
            </div>
            <div class="col-auto">
                <input autocomplete="off" required name="stravaId" class="form-control" type="text" id="form-user-stravaid">
            </div>
            <div class="col-auto">
                <input type="submit" class="btn btn-secondary" value="Ajouter le membre">
            </div>
        </form>
        <hr/>

        <#if roles?size gt 0>
            <ul class="list-group">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Identité<span class="text-muted"> - ID Strava</span></div>
                    <div>Actions</div>
                </li>
                <#list roles as role>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div><img src="<@common.teamUrl '' '/users/${role.user.id}/image' />" alt="User photo" width="30" height="30" class="rounded"> ${role.user.firstName!'inconnu'} ${role.user.lastName!'inconnu'}<span class="text-muted"><#if role.user.stravaId??> - <a class="link-dark" href="https://www.strava.com/athletes/${role.user.stravaId?c}" target="_blank">${(role.user.stravaId?c)}</a></#if></span><#if role.role == 'ADMIN'> <i class="bi bi-shield-fill"></i></#if></div>
                        <#if role.user.id != _user.id>
                            <div class="btn-group" role="group">
                                <#if role.role == 'ADMIN'>
                                    <a href="<@common.teamUrl team.id '/admin/users/relegate/${role.user.id}' />" class="btn btn-success btn-sm" role="button"><i class="bi bi-shield-fill"></i></a>
                                <#else>
                                    <a href="<@common.teamUrl team.id '/admin/users/promote/${role.user.id}' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-shield"></i></a>
                                </#if>
                                <a href="<@common.teamUrl team.id '/admin/users/delete/${role.user.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                            </div>
                        </#if>
                    </li>
                </#list>
            </ul>
        <#else>
            <div class="alert alert-warning" role="alert">
              Aucun utilisateur.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">