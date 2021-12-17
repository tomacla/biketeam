<nav class="navbar navbar-expand-lg navbar-light bg-light">
  <div class="container">
    <#if team??>
        <a class="navbar-brand" href="<@common.teamUrl team.id '' />">
            ${team.name}
        </a>
    <#else>
        <a class="navbar-brand" href="<@common.teamUrl '' '/' />">
            Biketeam
        </a>
    </#if>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">

      <ul class="navbar-nav ms-auto align-items-center">
        <#if team??>
            <#if _team_admin>
                <li class="nav-item border-end">
                  <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/admin')> active</#if>" href="<@common.teamUrl team.id '/admin' />"><i class="bi bi-shield-lock"></i> Gérer</a>
                </li>
            </#if>
            <#if team.configuration.feedVisible>
                <li class="nav-item">
                  <a class="nav-link<#if springMacroRequestContext.requestUri == '/${team.id}' || springMacroRequestContext.requestUri == '/${team.id}/'> active</#if>" href="<@common.teamUrl team.id '' />">Actualités</a>
                </li>
            </#if>
            <#if team.configuration.markdownPageWritten>
                <li class="nav-item">
                  <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/faq')> active</#if>" href="<@common.teamUrl team.id '/faq' />">FAQ</a>
                </li>
            </#if>
            <#if team.configuration.ridesVisible>
                <li class="nav-item">
                  <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/rides')> active</#if>" href="<@common.teamUrl team.id '/rides' />">Rides</a>
                </li>
            </#if>
            <#if team.configuration.tripsVisible>
                <li class="nav-item">
                  <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/trips')> active</#if>" href="<@common.teamUrl team.id '/trips' />">Trips</a>
                </li>
            </#if>
            <li class="nav-item border-end">
              <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/' + team.id + '/maps')> active</#if>" href="<@common.teamUrl team.id '/maps' />">Maps</a>
            </li>
        </#if>
        <#if team?? && !_authenticated>
            <li class="nav-item border-end">
                <a class="nav-link" href="<@common.teamUrl '' '/' />"><i class="bi bi-house"></i> <span class="d-md-none">Biketeam</span></a>
            </li>
        </#if>
        <#if _authenticated>
            <li class="nav-item">
               <a class="nav-link" href="<@common.teamUrl '' '/users/me' />"><#if _user.profileImage??><img class="rounded" height="24" src="${_user.profileImage}" alt="profile"></#if> ${_user.identity}</a>
            </li>
            <#if _admin>
                <li class="nav-item">
                  <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/admin')> active</#if>" href="<@common.teamUrl '' '/admin/teams' />"><i class="bi bi-gear"></i> <span class="d-md-none">Administration</span></a>
                </li>
            </#if>
            <#if team??>
                <li class="nav-item">
                    <a class="nav-link" href="<@common.teamUrl '' '/' />"><i class="bi bi-house"></i> <span class="d-md-none">Biketeam</span></a>
                </li>
            </#if>
            <li class="nav-item">
                <a class="nav-link" href="<@common.teamUrl '' '/logout' />"><i class="bi bi-door-closed"></i> <span class="d-md-none">Déconnexion</span></a>
            </li>
        <#else>
            <li class="nav-item ps-2">
                <a class="btn btn-sm btn-outline-secondary" href="<@common.teamUrl '' '/login?requestUri=${springMacroRequestContext.requestUri}' />">Connexion</a>
            </li>
        </#if>
      </ul>
    </div>
  </div>
</nav>