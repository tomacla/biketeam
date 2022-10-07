<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <a href="<@common.teamUrl team.id '/admin/places/new' />" class="btn btn-secondary" role="button"><i class="bi bi-plus"></i> Nouveau lieu</a>
        <hr/>
        <#if places?size gt 0>
            <ul class="list-group mt-4">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Lieu</div>
                    <div>Actions</div>
                </li>
                <#list places as place>
                    <li class="d-flex list-group-item justify-content-between align-items-center">
                        <div>${place.name}</div>
                        <div class="btn-group" role="group">
                            <a href="<@common.teamUrl team.id '/admin/places/${place.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/places/delete/${place.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>

         <#else>
             <div class="alert alert-warning" role="alert">
                Aucun lieu
             </div>
         </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">