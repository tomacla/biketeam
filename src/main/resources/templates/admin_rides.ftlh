<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="GET" action="<@spring.url '/admin/rides/new' />" class="row mb-0">
            <div class="col-auto">
                <select class="form-select" autocomplete="off" required name="templateId" id="form-ride-template">
                    <option value="">---</option>
                    <option value="empty-1">Saisie libre</option>
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

        <#if rides?size gt 0>
            <ul class="list-group mt-4">
                <#list rides as ride>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div><span class="text-<#if ride.publishedStatus == 'PUBLISHED'>success<#else>danger</#if>"><i class="bi bi-calendar-<#if ride.publishedStatus == 'PUBLISHED'>check<#else>x</#if>"></i></span> ${ride.title} (${ride.date.format(_date_formatter)})<span class="text-muted"> - ${ride.publishedAt.format(_date_formatter)}</span></div>
                        <div class="btn-group" role="group">
                            <a href="<@spring.url '/admin/rides/${ride.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@spring.url '/admin/rides/delete/${ride.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
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