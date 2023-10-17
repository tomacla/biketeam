<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <script type="text/javascript">

            function confirmDeleteUser(userId, targetUrl) {
                var confirmedDeletion = confirm('Confirmer la suppression définitive de l\'utilisateur ' + userId + ' ?');
                if(confirmedDeletion) {
                    window.location=targetUrl;
                }
            }

        </script>

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl '' '/admin/users/merge' />" class="mb-0">
            <div class="row">
                <div class="col-auto p-2">
                  <label for="form-user-sourceId" class="form-label">Source ID</label>
                </div>
                <div class="col-auto">
                    <input autocomplete="off" required name="sourceId" class="form-control" type="text" id="form-user-sourceId">
                </div>
            </div>
            <div class="row">
                <div class="col-auto p-2">
                  <label for="form-user-targetId" class="form-label">Target ID</label>
                </div>
                <div class="col-auto">
                    <input autocomplete="off" required name="targetId" class="form-control" type="text" id="form-user-targetId">
                </div>
            </div>
            <div class="row">
                <div class="col-auto">
                    <input type="submit" class="btn btn-sm btn-secondary" value="Fusionner 2 utilisateurs">
                </div>
            </div>
        </form>

        <hr/>

        <form id="users-filter" action="<@common.teamUrl '' '/admin/users' />" method="GET" class="mb-2">

            <input type="hidden" id="users-filter-page" name="page" value="${page}">
            <input type="hidden" id="users-filter-pageSize" name="pageSize" value="${pageSize}">

            <div class="row gy-2">

                  <div class="col-8 col-md-6">
                      <div class="input-group">
                          <label class="input-group-text" for="users-filter-name">Nom</label>
                          <input autocomplete="off" type="text" value="${name}" name="name" class="form-control" id="users-filter-name">
                       </div>
                    </div>

                <div class="col-4 col-md-2 text-center">
                    <button type="submit" class="btn btn-sm btn-secondary">Filtrer</button>
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
                    <div>Suppression</div>
                </li>
                <#list users as user>
                    <li class="small list-group-item d-flex justify-content-between align-items-center">
                        <div><img src="<@common.teamUrl '' '/users/${user.id}/image' />" alt="User photo" width="25" height="25" class="rounded"> ${user.firstName!'inconnu'} ${user.lastName!'inconnu'}<#if user.stravaId??><span class="text-muted"> - <a class="link-dark" href="https://www.strava.com/athletes/${user.stravaId?c}" target="_blank">${(user.stravaId?c)}</a></span></#if><#if user.email??><span class="text-muted"> - ${user.email}</span></#if><#if user.admin> <i class="bi bi-shield-fill"></i></#if><small> - ${user.id}</small></div>
                        <#if user.id != _user.id>
                            <div class="btn-group" role="group">
                               <a href="#" onclick="confirmDeleteUser('${user.id}', '<@common.teamUrl '' '/admin/users/delete/${user.id}' />')" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
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