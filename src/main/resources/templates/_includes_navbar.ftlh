<nav class="navbar navbar-no-expand navbar-light bg-light">
  <div class="container">

    <#if team??>
        <a class="navbar-brand order-0" href="<@common.teamUrl team.id '' />">
            ${team.name}
        </a>
    <#else>
        <a class="navbar-brand order-0" href="<@common.teamUrl '' '/' />">
            <i class="bi bi-bicycle"></i> BIKETEAM
        </a>
    </#if>

    <ul class="navbar-nav order-2 order-md-1">
        <#if team??>
            <li class="nav-item d-list-item d-md-none">
                <a class="nav-link<#if springMacroRequestContext.requestUri == '/' + team.id > active</#if>" href="<@common.teamUrl team.id '/' />"><i class="bi bi-collection"></i> Flux</a>
            </li>
            <#if team.configuration.markdownPageWritten>
                <li class="nav-item">
                    <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/faq')> active</#if>" href="<@common.teamUrl team.id '/faq' />"><i class="bi bi-patch-question"></i> FAQ</a>
                </li>
            </#if>
            <li class="nav-item">
                <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/maps')> active</#if>" href="<@common.teamUrl team.id '/maps' />"><i class="bi bi-map"></i> Maps</a>
            </li>
            <#if _team_admin>
                <li class="nav-item">
                    <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/admin')> active</#if>" href="<@common.teamUrl team.id '/admin' />"><i class="bi bi-shield-lock"></i> Gérer</a>
                </li>
            </#if>
        </#if>
    </ul>

    <ul class="navbar-nav order-1 order-md-2 ms-auto">
        <#if _authenticated>
            <li class="nav-item">
                <a class="nav-link" href="<@common.teamUrl '' '/users/me' />"><img class="rounded" height="24" src="<@common.teamUrl '' '/users/${_user.id}/image' />" alt="profile"></a>
            </li>
            <li class="nav-item">
                <a class="nav-link" href="<@common.teamUrl '' '/logout' />"><i class="bi bi-door-closed"></i></a>
            </li>
            <#if _admin>
                <li class="nav-item">
                    <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/admin')> active</#if>" href="<@common.teamUrl '' '/admin/teams' />"><i class="bi bi-gear"></i></a>
                </li>
            </#if>
            <#if team??>
                <li class="nav-item">
                    <a class="nav-link" href="<@common.teamUrl '' '/' />"><i class="bi bi-house-door"></i></a>
                </li>
            </#if>
        <#else>
            <li class="nav-item">
                <a class="nav-link" href="<@common.teamUrl '' '/login?requestUri=${springMacroRequestContext.requestUri}' />">Connexion</a>
            </li>
            <#if team??>
                <li class="nav-item border-start ps-1">
                    <a class="nav-link" href="<@common.teamUrl '' '/' />"><i class="bi bi-house-door"></i></a>
                </li>
            </#if>
        </#if>
    </ul>
  </div>
</nav>