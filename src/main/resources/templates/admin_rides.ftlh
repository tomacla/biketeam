<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <a href="<@spring.url '/admin/rides/new' />" class="btn btn-secondary" role="button"><i class="bi bi-plus"></i> Nouveau ride</a>
        <hr/>

        <#if rides?size gt 0>
            <ul class="list-group mt-4">
                <#list rides as ride>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${ride.title}<span class="text-muted"> - ${ride.date.format(_date_formatter)}</span></div>
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