<nav class="navbar navbar-no-expand navbar-light bg-light">
  <div class="container">

    <#if team??>
        <a class="nav-link link-secondary px-1" href="<@common.teamUrl '' '/' />"><img height="25" src="<@common.teamUrl '' '/misc/home.png' />" alt="prendlaroue" /><i class="bi bi-chevron-right"></i></a>
        <a class="navbar-brand order-0" href="<@common.teamUrl team.id '' />">
            ${team.name}
        </a>
    <#elseif springMacroRequestContext.requestUri?contains('gpxtool')>
        <a class="nav-link link-secondary px-1" href="<@common.teamUrl '' '/' />"><img height="25" src="<@common.teamUrl '' '/misc/home.png' />" alt="prendlaroue" /><i class="bi bi-chevron-right"></i></a>
        <a class="navbar-brand order-0" href="<@common.teamUrl '' '/gpxtool' />">
            GPX Tools
        </a>
    <#else>
        <a class="navbar-brand order-0 p-0" href="<@common.teamUrl '' '/' />">
            <img height="42" src="<@common.teamUrl '' '/misc/logo.png' />" alt="Prendslaroue">
        </a>
    </#if>

    <#if team?? && (team.configuration.feedVisible || _team_admin || _admin || team.configuration.markdownPageWritten)>
        <ul class="navbar-nav order-2 order-md-1">
            <#if team??>
                <#if team.configuration.feedVisible>
                    <li class="nav-item d-list-item d-md-none">
                        <a class="nav-link<#if springMacroRequestContext.requestUri == '/' + team.id > active</#if>" href="<@common.teamUrl team.id '/' />"><i class="bi bi-collection"></i> Flux</a>
                    </li>
                </#if>
                <#if team.configuration.markdownPageWritten>
                    <li class="nav-item">
                        <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/faq')> active</#if>" href="<@common.teamUrl team.id '/faq' />"><i class="bi bi-patch-question"></i> FAQ</a>
                    </li>
                </#if>
                <li class="nav-item">
                    <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/maps')> active</#if>" href="<@common.teamUrl team.id '/maps' />"><i class="bi bi-map"></i> Maps</a>
                </li>
                <#if _team_admin || _admin>
                    <li class="nav-item">
                        <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/admin')> active</#if>" href="<@common.teamUrl team.id '/admin' />"><i class="bi bi-shield-lock"></i> Gérer</a>
                    </li>
                </#if>
            </#if>
        </ul>
    </#if>

    <ul class="navbar-nav order-1 order-md-2 ms-auto">
        <#if _authenticated>
            <li class="nav-item">
                <a class="nav-link" href="<@common.teamUrl '' '/users/me' />"><img class="rounded" height="24" src="<@common.teamUrl '' '/users/${_user.id}/image' />" alt="profile"></a>
            </li>
            <#if _notifications?? && _notifications?size gt 0>
            <li class="nav-item dropdown">
              <a class="nav-link dropdown-toggle" href="#" id="notification-dropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false"><span class="text-danger"><i class="bi bi-bell-fill"></i></span></a>
              <ul class="dropdown-menu p-1" aria-labelledby="notification-dropdown">
                <#list _notifications as notification>
                    <li class="p-1 d-flex flex-row align-items-center justify-content-between">
                        <img class="rounded" src="<@common.teamUrl notification.teamId '/image' />" height="15" alt="Team image">
                        <a class="dropdown-item p-0 ps-1 small link-dark" href="<@common.teamUrl '' '/notifications/${notification.id}' />">
                            <#if notification.type == 'NEW_RIDE_MESSAGE' || notification.type == 'NEW_TRIP_MESSAGE'>
                                Nouveau message
                            <#elseif notification.type == 'RIDE_PUBLISHED'>
                                Nouveau ride publié
                            <#elseif notification.type == 'TRIP_PUBLISHED'>
                                Nouveau trip publié
                            </#if>
                        </a>
                    </li>
                </#list>
                <li class="p-1 d-flex flex-row align-items-center justify-content-between">
                    <a class="dropdown-item p-0 ps-1 small link-secondary" href="<@common.teamUrl '' '/notifications/read-all' />">Tout marquer comme lu</a>
                </li>
              </ul>
            </li>
            </#if>
            <li class="nav-item">
                <a class="nav-link" href="<@common.teamUrl '' '/logout' />"><i class="bi bi-door-closed"></i></a>
            </li>
            <#if _admin>
                <li class="nav-item">
                    <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/admin')> active</#if>" href="<@common.teamUrl '' '/admin/teams' />"><i class="bi bi-gear"></i></a>
                </li>
            </#if>
        <#else>
            <li class="nav-item">
                <a class="nav-link" href="<@common.teamUrl '' '/login' />">Connexion</a>
            </li>
        </#if>
    </ul>
  </div>
</nav>