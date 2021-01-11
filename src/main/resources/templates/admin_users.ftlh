<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <#if users?size gt 0>
            <ul class="list-group mt-4">
                <#list users as user>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${user.firstName} ${user.lastName}<#if user.admin> <i class="bi bi-shield-fill"></i></#if></div>
                        <div class="btn-group" role="group">
                            <#if user.admin>
                                <a href="<@spring.url '/admin/users/relegate/${user.id}' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-shield-slash"></i></a>
                            <#else>
                                <a href="<@spring.url '/admin/users/promote/${user.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-shield"></i></a>
                            </#if>
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