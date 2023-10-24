<div class="col-12 col-md-4 list-group mb-4">

    <h6>Contenu</h6>

    <ul class="list-group mb-4">
      <a href="<@common.teamUrl team.id '/admin/rides' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?contains('/admin/rides')> list-group-item-secondary</#if>">Rides</a>
      <a href="<@common.teamUrl team.id '/admin/publications' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?contains('/admin/publications')> list-group-item-secondary</#if>">Publications</a>
      <a href="<@common.teamUrl team.id '/admin/trips' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?contains('/admin/trips')> list-group-item-secondary</#if>">Trips</a>
      <a href="<@common.teamUrl team.id '/admin/maps' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?contains('/admin/maps')> list-group-item-secondary</#if>">Maps</a>
      <a href="<@common.teamUrl team.id '/admin/templates' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?contains('/admin/templates')> list-group-item-secondary</#if>">Modèles de ride</a>
      <a href="<@common.teamUrl team.id '/admin/places' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?contains('/admin/places')> list-group-item-secondary</#if>">Lieux (départ/arrivée)</a>
    </ul>

    <h6>Utilisateurs</h6>

     <ul class="list-group mb-4">
      <#if team.visibility != 'USER'><a href="<@common.teamUrl team.id '/admin/admins' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?contains('/admin/admins')> list-group-item-secondary</#if>">Administrateurs</a></#if>
      <a href="<@common.teamUrl team.id '/admin/users' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?contains('/admin/users')> list-group-item-secondary</#if>">Utilisateurs</a>
    </ul>

    <h6>Administration</h6>

    <ul class="list-group mb-4">
      <a href="<@common.teamUrl team.id '/admin/general' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/general')> list-group-item-secondary</#if>">Général</a>
      <a href="<@common.teamUrl team.id '/admin/description' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/description')> list-group-item-secondary</#if>">Description</a>
      <a href="<@common.teamUrl team.id '/admin/configuration' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/configuration')> list-group-item-secondary</#if>">Configuration</a>
      <a href="<@common.teamUrl team.id '/admin/faq' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/faq')> list-group-item-secondary</#if>">FAQ</a>
      <a href="<@common.teamUrl team.id '/admin/integration' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/integration')> list-group-item-secondary</#if>">Intégrations</a>
      <a href="<@common.teamUrl team.id '/admin/logo' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/logo')> list-group-item-secondary</#if>">Logo</a>
    </ul>

</div>