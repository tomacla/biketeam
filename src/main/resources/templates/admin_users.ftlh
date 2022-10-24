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

        <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl '' '/admin/users' />" class="row mb-0">
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
                <input type="submit" class="btn btn-secondary" value="Ajouter">
            </div>
        </form>

        <hr/>

        <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl '' '/admin/users/merge' />" class="row mb-0">
            <div class="col-auto p-2">
              <label for="form-user-sourceId" class="form-label">Source ID</label>
            </div>
            <div class="col-auto">
                <input autocomplete="off" required name="sourceId" class="form-control" type="text" id="form-user-sourceId">
            </div>
            <div class="col-auto p-2">
              <label for="form-user-targetId" class="form-label">Target ID</label>
            </div>
            <div class="col-auto">
                <input autocomplete="off" required name="targetId" class="form-control" type="text" id="form-user-targetId">
            </div>
            <div class="col-auto">
                <input type="submit" class="btn btn-secondary" value="Fusionner">
            </div>
        </form>

        <hr/>

        <#if users?size gt 0>
            <ul class="list-group">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Identité<span class="text-muted"> - ID Strava</span></div>
                    <div>Administrateur</div>
                </li>
                <#list users as user>
                    <li class="small list-group-item d-flex justify-content-between align-items-center">
                        <div><img src="<@common.teamUrl '' '/users/${user.id}/image' />" alt="User photo" width="25" height="25" class="rounded"> ${user.firstName!'inconnu'} ${user.lastName!'inconnu'}<#if user.stravaId??><span class="text-muted"> - <a class="link-dark" href="https://www.strava.com/athletes/${user.stravaId?c}" target="_blank">${(user.stravaId?c)}</a></span></#if><#if user.email??><span class="text-muted"> - ${user.email}</span></#if><#if user.admin> <i class="bi bi-shield-fill"></i></#if><small> - ${user.id}</small></div>
                        <#if user.id == _user.id>
                            <button role="button" disabled="true" class="btn btn-sm btn-success"><i class="bi bi-shield-fill"></i></button>
                        <#else>
                            <div class="btn-group" role="group">
                                <a href="#" onclick="confirmDeleteUser('${user.id}', '<@common.teamUrl '' '/admin/users/delete/${user.id}' />')" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                                <#if user.admin>
                                    <a href="<@common.teamUrl '' '/admin/users/relegate/${user.id}' />" class="btn btn-success btn-sm" role="button"><i class="bi bi-shield-fill"></i></a>
                                <#else>
                                    <a href="<@common.teamUrl '' '/admin/users/promote/${user.id}' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-shield"></i></a>
                                </#if>
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