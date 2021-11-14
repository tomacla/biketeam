<div class="col-12 col-md-4 list-group mb-4">

    <h6>Contenu</h6>

    <ul class="list-group mb-4">
      <a href="<@common.teamUrl team.id '/admin/publications' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/publications')> list-group-item-secondary</#if>">Publications</a>
      <a href="<@common.teamUrl team.id '/admin/rides' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/rides')> list-group-item-secondary</#if>">Rides</a>
      <a href="<@common.teamUrl team.id '/admin/trips' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/trips')> list-group-item-secondary</#if>">Trips</a>
      <a href="<@common.teamUrl team.id '/admin/maps' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/maps')> list-group-item-secondary</#if>">Maps</a>
    </ul>

    <h6>Administration</h6>

    <ul class="list-group mb-4">
      <a href="<@common.teamUrl team.id '/admin' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin')> list-group-item-secondary</#if>">Général</a>
      <a href="<@common.teamUrl team.id '/admin/description' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/description')> list-group-item-secondary</#if>">Description</a>
      <a href="<@common.teamUrl team.id '/admin/configuration' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/configuration')> list-group-item-secondary</#if>">Configuration</a>
      <a href="<@common.teamUrl team.id '/admin/page' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/page')> list-group-item-secondary</#if>">FAQ</a>
      <a href="<@common.teamUrl team.id '/admin/integration' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/integration')> list-group-item-secondary</#if>">Intégrations</a>
      <a href="<@common.teamUrl team.id '/admin/logo' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/logo')> list-group-item-secondary</#if>">Logo</a>
      <a href="<@common.teamUrl team.id '/admin/users' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/users')> list-group-item-secondary</#if>">Membres</a>
      <a href="<@common.teamUrl team.id '/admin/templates' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri?ends_with('/admin/templates')> list-group-item-secondary</#if>">Modèles de ride</a>
    </ul>

</div>