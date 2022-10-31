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
                <input type="submit" class="btn btn-secondary" value="Ajouter">
            </div>
        </form>
        <hr/>

        <#if roles?size gt 0>
            <ul class="list-group">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Identité<span class="text-muted"> - ID Strava</span></div>
                    <#if roles?size gt 20>
                        <div class="btn-group btn-group-sm" role="group">
                            <button onclick="previousPage()" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-left-fill"></i></button>
                            <button onclick="nextPage()" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-right-fill"></i></button>
                        </div>
                    </#if>
                    <div>Actions</div>
                </li>
                <#list roles as role>
                    <li class="d-none list-group-item list-group-item-role d-flex justify-content-between align-items-center">
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

            <script type="text/javascript">

                var currentPage = 0;

                function previousPage() {
                    currentPage = Math.max(0, currentPage - 20);
                    displayItems();
                }

                function nextPage() {
                    currentPage = Math.min(${roles?size} - 20, currentPage + 20);
                    displayItems();
                }

                function displayItems() {
                     const mapsElements = document.getElementsByClassName('list-group-item-role');
                     for(var i = 0; i < mapsElements.length; i++) {
                        if(i >= currentPage && i < currentPage + 20) {
                            mapsElements[i].classList.remove("d-none");
                            mapsElements[i].classList.add("d-block");
                        } else {
                            mapsElements[i].classList.remove("d-block");
                            mapsElements[i].classList.add("d-none");
                        }
                     }
                }

                displayItems();

            </script>

        <#else>
            <div class="alert alert-warning" role="alert">
              Aucun utilisateur.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">