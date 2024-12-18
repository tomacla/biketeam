<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12">

        <div class="row g-2 pb-4">

            <#if _user.mapFavorites?size == 0>
                 <div class="alert alert-warning" role="alert">
                   Vous n'avez ajouté aucune map dans vos favoris.
                 </div>
            </#if>
            <#list _user.mapFavorites as map>

                <div class="modal fade" id="modal-fav-map-${map.id}" tabindex="-1">
                      <div class="modal-dialog">
                        <div class="modal-content">
                          <div class="modal-header">
                            <h5 class="modal-title">${map.name}</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                          </div>
                          <div class="modal-body">
                            <div class="d-grid gap-2 col-6 mx-auto">
                                <a download="${map.name}.gpx" href="<@common.teamUrl map.teamId '/maps/${map.id}/gpx' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> GPX</a>
                                <a download="${map.name}.fit" href="<@common.teamUrl map.teamId '/maps/${map.id}/fit' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-file-earmark-arrow-down"></i> FIT</a>
                                <a href="<@common.teamUrl map.teamId '/maps/${map.id}/garmin' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-triangle-fill"></i> Garmin</a>
                            </div>
                          </div>
                          <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fermer</button>
                          </div>
                        </div>
                      </div>
                    </div>

                  <div class="col-12 col-md-4">
                    <div class="card">
                        <a href="<@common.teamUrl map.teamId '/maps/${map.permalink!map.id}' />"><img src="<@common.teamUrl map.teamId '/maps/${map.id}/image' />" style="max-width:450px;width:100%" class="card-img-top" alt="${map.id} image"></a>
                        <div class="card-body">
                          <h5 class="card-title"><a class="link-dark" href="<@common.teamUrl map.teamId '/maps/${map.permalink!map.id}' />">${map.name}</a></h5>
                          <h6 class="card-subtitle mb-2 text-muted">${map.postedAt.format(_date_formatter)}</h6>
                          <p class="card-text">
                            <i class="bi bi-arrow-left-right"></i> ${map.length}km
                            <i class="bi bi-arrow-up"></i> ${map.positiveElevation}m
                            <i class="bi bi-arrow-down"></i> ${map.negativeElevation}m
                          </p>
                        </div>
                        <div class="card-footer d-flex justify-content-between align-items-center">
                            <div>
                                <img src="<@common.teamUrl map.teamId '/image' />" height="30" alt="Team image">
                            </div>
                            <div class="btn-group" role="group">
                                <button class="btn btn-outline-secondary btn-sm" type="button" data-bs-toggle="modal" data-bs-target="#modal-fav-map-${map.id}"><i class="bi bi-download"></i> Télécharger</button>
                                <a href="<@common.teamUrl map.teamId '/maps/${map.permalink!map.id}' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-map"></i> Voir</a>
                            </div>
                        </div>
                    </div>
                  </div>
            </#list>
        </div>

      </div>

</div>

<#include "_includes_tail.ftlh">