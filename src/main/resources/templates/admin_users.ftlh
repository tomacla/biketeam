<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="POST" action="<@spring.url '/admin/users' />" class="row mb-0">
            <div class="col-auto p-2">
              <label for="form-user-stravaid" class="form-label">Strava ID</label>
            </div>
            <div class="col-auto">
                <input autocomplete="off" required name="stravaId" class="form-control" type="text" id="form-user-stravaid">
            </div>
            <div class="col-auto">
                <input type="submit" class="btn btn-secondary" value="Nouvel utilisateur">
            </div>
        </form>
        <hr/>

        <#if users?size gt 0>
            <ul class="list-group">
                <#list users as user>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${user.firstName!'inconnu'} ${user.lastName!'inconnu'}<span class="text-muted"> - <a class="link-dark" href="https://www.strava.com/athletes/${user.stravaId?c}" target="_blank">${(user.stravaId?c)}</a></span><#if user.admin> <i class="bi bi-shield-fill"></i></#if></div>
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