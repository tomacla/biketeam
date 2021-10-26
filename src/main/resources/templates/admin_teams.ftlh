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

        <#if teams?size gt 0>
            <ul class="list-group">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Groupe<span class="text-muted"> - ID</span></div>
                    <div>Actions</div>
                </li>
                <#list teams as team>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${team.name}<span class="text-muted"> - ${team.id}</span></div>
                        <div class="btn-group" role="group">
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