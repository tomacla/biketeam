<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <script type="text/javascript">

        function confirmDeleteTeam(teamId, targetUrl) {
            var confirmedDeletion = confirm('Confirmer la suppression définitive du groupe ' + teamId + ' et de tous les éléments associés (rides, maps, ...) ?');
            if(confirmedDeletion) {
                window.location=targetUrl;
            }
        }

    </script>

    <div class="col-12 col-md-8">

        <form id="teams-filter" action="<@common.teamUrl '' '/admin/teams' />" method="GET" class="mb-2">

            <input type="hidden" id="teams-filter-page" name="page" value="${page}">
            <input type="hidden" id="teams-filter-pageSize" name="pageSize" value="${pageSize}">

            <div class="row gy-2">

                  <div class="col-8 col-md-6">
                      <div class="input-group">
                          <label class="input-group-text" for="teams-filter-name">Nom</label>
                          <input autocomplete="off" type="text" value="${name}" name="name" class="form-control" id="teams-filter-name">
                       </div>
                    </div>

                <div class="col-4 col-md-2 text-center">
                    <button type="submit" class="btn btn-secondary">Filtrer</button>
                </div>

            </div>
         </form>

        <#if teams?size gt 0>
            <ul class="list-group">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Groupe<span class="text-muted"> - ID</span></div>
                    <div>
                        <@common.pagination page pages 'teams-filter-page' 'teams-filter' />
                    </div>
                    <div>Actions</div>
                </li>
                <#list teams as team>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${team.name}<span class="text-muted"> - ${team.id} - ${team.visibility}</span></div>
                        <div class="btn-group" role="group">
                            <a href="<@common.teamUrl team.id '/' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye"></i></a>
                            <a href="#" onclick="confirmDeleteTeam('${team.id}', '<@common.teamUrl '' '/admin/teams/delete/${team.id}' />')" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
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