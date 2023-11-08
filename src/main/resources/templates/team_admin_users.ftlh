<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl team.id '/admin/users' />" class="row mb-0">
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
            <div class="col-auto">
                <input type="submit" class="btn btn-sm btn-secondary" value="Ajouter comme utilisateur">
            </div>
        </form>
        <hr/>

      <form id="users-filter" action="<@common.teamUrl team.id '/admin/users' />" method="GET" class="mb-2">

                <input type="hidden" id="users-filter-page" name="page" value="${page}">
                <input type="hidden" id="users-filter-pageSize" name="pageSize" value="${pageSize}">

                <div class="row gy-2">

                      <div class="col-8 col-md-6">
                          <div class="input-group">
                              <label class="input-group-text" for="users-filter-title">Nom</label>
                              <input autocomplete="off" type="text" value="${name}" name="name" class="form-control" id="users-filter-title">
                           </div>
                        </div>

                    <div class="col-4 col-md-2 text-center">
                        <button type="submit" class="btn btn-secondary">Filtrer</button>
                    </div>

                </div>
             </form>

        <#if users?size gt 0>
            <ul class="list-group">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Identité<span class="text-muted"> - ID Strava</span></div>
                    <div>
                        <@common.pagination page pages 'users-filter-page' 'users-filter' />
                    </div>
                    <div>Actions</div>
                </li>
                <#list users as user>
                    <li class="d-flex list-group-item list-group-item-role justify-content-between align-items-center">
                        <div><img src="<@common.teamUrl '' '/users/${user.id}/image' />" alt="User photo" width="30" height="30" class="rounded"> ${user.firstName!'inconnu'} ${user.lastName!'inconnu'}<span class="text-muted"><#if user.stravaId??> - <a class="link-dark" href="https://www.strava.com/athletes/${user.stravaId?c}" target="_blank">${(user.stravaId?c)}</a></#if></span></div>
                        <#if user.id != _user.id>
                            <div class="btn-group" role="group">
                                <a href="<@common.teamUrl team.id '/admin/users/delete/${user.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
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