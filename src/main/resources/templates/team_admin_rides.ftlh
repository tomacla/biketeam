<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="GET" action="<@common.teamUrl team.id '/admin/rides/new' />" class="row mb-0">

            <div class="col-auto">
                <select class="form-select" autocomplete="off" required name="templateId" id="form-ride-template">
                    <option selected value="empty-1">Saisie libre</option>
                    <#if templates?size != 0>
                        <optgroup label="Templates">
                            <#list templates as template>
                                <option value="${template.id}">${template.name}</option>
                            </#list>
                        </optgroup>
                    </#if>
                </select>
            </div>
            <div class="col-auto">
                <input type="submit" class="btn btn-secondary" value="Nouveau ride">
            </div>
        </form>

        <hr/>

        <form id="rides-filter" action="<@common.teamUrl team.id '/admin/rides' />" method="GET" class="mb-2">

            <input type="hidden" id="rides-filter-page" name="page" value="${page}">
            <input type="hidden" id="rides-filter-pageSize" name="pageSize" value="${pageSize}">

            <div class="row gy-2">

                  <div class="col-8 col-md-6">
                      <div class="input-group">
                          <label class="input-group-text" for="teams-filter-title">Nom</label>
                          <input autocomplete="off" type="text" value="${title}" name="title" class="form-control" id="teams-filter-title">
                       </div>
                    </div>

                <div class="col-4 col-md-2 text-center">
                    <button type="submit" class="btn btn-secondary">Filtrer</button>
                </div>

            </div>
         </form>

        <#if rides?size gt 0>
            <ul class="list-group mt-4">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Ride<span class="text-muted"> - Date de publication</span></div>
                    <div>
                        <@common.pagination page pages 'rides-filter-page' 'rides-filter' />
                    </div>
                    <div>Actions</div>
                </li>
                <#list rides as ride>
                    <li class="d-flex list-group-item list-group-item-ride justify-content-between align-items-center">
                        <div><span class="text-<#if ride.publishedStatus == 'PUBLISHED'>success<#else>danger</#if>"><i class="bi bi-calendar-<#if ride.publishedStatus == 'PUBLISHED'>check<#else>x</#if>"></i></span> ${ride.title} (${ride.date.format(_date_formatter)})<span class="text-muted"> - ${ride.publishedAt.format(_date_formatter)}</span></div>
                        <div class="btn-group" role="group">
                            <a href="<@common.teamUrl team.id '/rides/${ride.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/rides/${ride.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/rides/delete/${ride.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>

        <#else>
            <div class="alert alert-warning" role="alert">
              Aucun ride.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">