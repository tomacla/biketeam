<ul class="col-12 col-md-4 list-group mb-4">
  <a href="<@common.teamUrl '' '/admin/teams' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/teams'> list-group-item-secondary</#if>">Groupes</a>
  <a href="<@common.teamUrl '' '/admin/users' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/users'> list-group-item-secondary</#if>">Utilisateurs</a>
  <a href="<@common.teamUrl '' '/admin/archives' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/archives'> list-group-item-secondary</#if>">Archives</a>
  <a href="<@common.teamUrl '' '/admin/legal' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/legal'> list-group-item-secondary</#if>">Mentions légales</a>
  <a href="<@common.teamUrl '' '/admin/images' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/images'> list-group-item-secondary</#if>">Images</a>
</ul>