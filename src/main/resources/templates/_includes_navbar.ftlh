<nav class="navbar navbar-expand-lg navbar-light bg-light">
  <div class="container-fluid">
    <a class="navbar-brand" href="<@spring.url '/' />">
        <img src="<@spring.url '/api/logo' />" alt="Logo" height="24" class="d-inline-block align-center">
        ${_sitename}
    </a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav ms-auto">
        <li class="nav-item">
          <a class="nav-link<#if springMacroRequestContext.requestUri == '/'> active</#if>" href="<@spring.url '/' />">Accueil</a>
        </li>
        <li class="nav-item">
          <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/rides')> active</#if>" href="<@spring.url '/rides' />">Rides</a>
        </li>
        <li class="nav-item">
          <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/maps')> active</#if>" href="<@spring.url '/maps' />">Maps</a>
        </li>
        <#if _authenticated>
            <li class="nav-item border-start">
               <a class="nav-link" href="<@spring.url '/users/me' />"><img class="rounded" height="24" src="${_profile_image}" alt="profile"> ${_identity}</a>
            </li>
            <#if _admin>
                <li class="nav-item">
                  <a class="nav-link<#if springMacroRequestContext.requestUri?starts_with('/admin')> active</#if>" href="<@spring.url '/admin' />"><i class="bi bi-gear"></i> <span class="d-md-none">Administration</span></a>
                </li>
            </#if>
            <li class="nav-item">
                <a class="nav-link" href="<@spring.url '/logout' />"><i class="bi bi-door-closed"></i> <span class="d-md-none">Déconnexion</span></a>
            </li>
        <#else>
            <li class="nav-item">
                <a href="<@spring.url '/oauth2/authorization/strava' />"><img height="40" src="<@spring.url '/css/btn_strava_connectwith_light.png' />" alt="connect with strava"></a>
            </li>
        </#if>
      </ul>
    </div>
  </div>
</nav>