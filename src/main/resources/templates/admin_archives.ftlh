<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <#if archives?size gt 0>
            <ul class="list-group">
                <#list archives as archive>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${archive}</div>
                        <div class="btn-group" role="group">
                            <a href="<@spring.url '/admin/archives?archive=${archive}' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-arrow-up"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>
        <#else>
            <div class="alert alert-warning" role="alert">
              Aucune archive.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">