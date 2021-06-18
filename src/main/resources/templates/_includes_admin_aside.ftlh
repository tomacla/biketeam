<ul class="col-12 col-md-4 list-group mb-4">
  <a href="<@spring.url '/admin/users' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/users'> list-group-item-secondary</#if>">Utilisateurs</a>
  <a href="<@spring.url '/admin/archives' />" class="list-group-item list-group-item-action<#if springMacroRequestContext.requestUri == '/admin/archives'> list-group-item-secondary</#if>">Archives</a>
</ul>