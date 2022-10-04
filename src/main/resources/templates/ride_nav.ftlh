<div class="col-12 my-2">

    <ul class="nav nav-pills">
      <li class="nav-item">
        <a href="<@common.teamUrl team.id '/rides/${ride.id}' />" class="me-1 nav-link text-dark border <#if springMacroRequestContext.requestUri?ends_with(ride.id) || springMacroRequestContext.requestUri?ends_with(ride.permalink!ride.id)> bg-secondary text-white</#if>">À propos</a>
      </li>
      <li class="nav-item">
        <a href="<@common.teamUrl team.id '/rides/${ride.id}/groups' />" class="me-1 nav-link text-dark border <#if springMacroRequestContext.requestUri?ends_with('/groups')> bg-secondary text-white</#if>"><i class="bi bi-map"></i><span class="d-none d-md-inline"> Groupes</span></a>
      </li>
      <li class="nav-item">
        <a href="<@common.teamUrl team.id '/rides/${ride.id}/messages' />" class="me-1 nav-link text-dark border <#if springMacroRequestContext.requestUri?ends_with('/messages')> bg-secondary text-white</#if>"><i class="bi bi-chat-left-text"></i><span class="d-none d-md-inline"> Messages</span>
         <span class="badge bg-info text-white d-none d-md-inline">${ride.sortedMessages?size}</span>
        </a>
      </li>
      <#if _team_admin>
      <li class="nav-item">
            <a href="<@common.teamUrl team.id '/admin/rides/${ride.id}' />" class="nav-link text-dark border"><i class="bi-pencil bi"></i><span class="d-none d-md-inline"> Editer</span></a>
      </li>
      </#if>
    </ul>

</div>