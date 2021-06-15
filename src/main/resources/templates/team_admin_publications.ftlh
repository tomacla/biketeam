<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <a href="<@spring.url '/' + team.id + '/admin/publications/new' />" class="btn btn-secondary" role="button"><i class="bi bi-plus"></i> Nouvelle publication</a>
        <hr/>
        <#if publications?size gt 0>
            <ul class="list-group mt-4">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Publication<span class="text-muted"> - Date de publication</span></div>
                    <div>Actions</div>
                </li>
                <#list publications as publication>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div><span class="text-<#if publication.publishedStatus == 'PUBLISHED'>success<#else>danger</#if>"><i class="bi bi-calendar-<#if publication.publishedStatus == 'PUBLISHED'>check<#else>x</#if>"></i></span> ${publication.title}<span class="text-muted"> - ${publication.publishedAt.format(_date_formatter)}</span></div>
                        <div class="btn-group" role="group">
                            <a href="<@spring.url '/' + team.id + '/admin/publications/${publication.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@spring.url '/' + team.id + '/admin/publications/delete/${publication.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>
        <#else>
            <div class="alert alert-warning" role="alert">
              Aucune publication.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">