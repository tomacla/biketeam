<div class="col-12 my-2">

    <ul class="nav nav-pills">
      <li class="nav-item">
        <a href="<@common.teamUrl team.id '/trips/${trip.id}' />" class="me-1 nav-link text-dark border <#if springMacroRequestContext.requestUri?ends_with(trip.id) || springMacroRequestContext.requestUri?ends_with(trip.permalink!trip.id)> bg-secondary text-white</#if>">À propos</a>
      </li>
      <li class="nav-item">
        <a href="<@common.teamUrl team.id '/trips/${trip.id}/stages' />" class="me-1 nav-link text-dark border <#if springMacroRequestContext.requestUri?ends_with('/stages')> bg-secondary text-white</#if>"><i class="bi bi-map"></i><span class="d-none d-md-inline"> Etapes</span></a>
      </li>
      <li class="nav-item">
        <a href="<@common.teamUrl team.id '/trips/${trip.id}/messages' />" class="me-1 nav-link text-dark border <#if springMacroRequestContext.requestUri?ends_with('/messages')> bg-secondary text-white</#if>"><i class="bi bi-chat-left-text"></i><span class="d-none d-md-inline"> Messages</span>
         <span class="badge bg-info text-white d-none d-md-inline">${trip.sortedMessages?size}</span>
        </a>
      </li>
      <#if _team_admin>
      <li class="nav-item">
            <a href="<@common.teamUrl team.id '/admin/trips/${trip.id}' />" class="nav-link text-dark border"><i class="bi-pencil bi"></i><span class="d-none d-md-inline"> Editer</span></a>
      </li>
      </#if>
    </ul>

</div>