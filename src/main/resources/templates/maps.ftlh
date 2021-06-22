<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 mb-4">

        <form id="maps-filter" action="<@spring.url '/' + team.id + '/maps' />" method="GET" class="border rounded p-2">

            <input type="hidden" id="maps-filter-page" name="page" value="${formdata.page}">
            <input type="hidden" id="maps-filter-pageSize" name="pageSize" value="${formdata.pageSize}">

            <div class="row gy-2">
                <div class="col-12 col-md-6">
                    <div class="input-group">
                        <span class="input-group-text">Distance de</span>
                        <input onchange="setFieldValue('maps-filter-page', 0);" autocomplete="off" type="number" required step="1" min="1" value="${formdata.lowerDistance?c}" name="lowerDistance" class="form-control" id="maps-filter-distance">
                        <span class="input-group-text">à</span>
                        <input onchange="setFieldValue('maps-filter-page', 0);" autocomplete="off" type="number" required step="1" min="1" value="${formdata.upperDistance?c}" name="upperDistance" class="form-control">
                        <span class="input-group-text">km</span>
                    </div>
                </div>
                <div class="col-12 col-md-5">
                    <div class="input-group">
                        <label class="input-group-text" for="maps-filter-type">Type</label>
                        <select onchange="setFieldValue('maps-filter-page', 0);" name="type" class="form-select" id="maps-filter-type">
                          <option <#if formdata.type == ''>selected</#if> value="">---</option>
                          <option <#if formdata.type == 'ROAD'>selected</#if> value="ROAD">Route</option>
                          <option <#if formdata.type == 'GRAVEL'>selected</#if> value="GRAVEL">Gravel</option>
                          <option <#if formdata.type == 'MTB'>selected</#if> value="MTB">VTT</option>
                        </select>
                     </div>
                  </div>
                  <div class="col-12 col-md-6">
                      <div class="input-group">
                          <span class="input-group-text">Dénivelé de</span>
                          <input onchange="setFieldValue('maps-filter-page', 0);" autocomplete="off" type="number" required step="1" min="0" value="${formdata.lowerPositiveElevation?c}" name="lowerPositiveElevation" class="form-control" id="maps-filter-elevation">
                          <span class="input-group-text">à</span>
                          <input onchange="setFieldValue('maps-filter-page', 0);" autocomplete="off" type="number" required step="1" min="1" value="${formdata.upperPositiveElevation?c}" name="upperPositiveElevation" class="form-control">
                          <span class="input-group-text">m</span>
                      </div>
                  </div>
                  <div class="col-12 col-md-5">
                      <div class="input-group">
                          <label class="input-group-text" for="maps-filter-wind">Vent</label>
                          <select onchange="setFieldValue('maps-filter-page', 0);" name="windDirection" class="form-select" id="maps-filter-wind">
                            <option <#if formdata.windDirection == ''>selected</#if> value="">---</option>
                            <option <#if formdata.windDirection == 'NORTH'>selected</#if> value="NORTH">Nord</option>
                            <option <#if formdata.windDirection == 'NORTH_EAST'>selected</#if> value="NORTH_EAST">Nord-Est</option>
                            <option <#if formdata.windDirection == 'EAST'>selected</#if> value="EAST">Est</option>
                            <option <#if formdata.windDirection == 'SOUTH_EAST'>selected</#if> value="SOUTH_EAST">Sud-Est</option>
                            <option <#if formdata.windDirection == 'SOUTH'>selected</#if> value="SOUTH">Sud</option>
                            <option <#if formdata.windDirection == 'SOUTH_WEST'>selected</#if> value="SOUTH_WEST">Sud-Ouest</option>
                            <option <#if formdata.windDirection == 'WEST'>selected</#if> value="WEST">Ouest</option>
                            <option <#if formdata.windDirection == 'NORTH_WEST'>selected</#if> value="NORTH_WEST">Nord-Ouest</option>
                          </select>
                       </div>
                    </div>
                  <div class="col-12 col-md-6">
                    <div class="input-group">
                        <label class="input-group-text" for="maps-filter-tags" class="form-label">Tags</label>
                        <select onchange="setFieldValue('maps-filter-page', 0);" name="tags" multiple class="form-select" id="maps-filter-tags-2">
                            <option selected value="">Tag...</option>
                             <#list tags as tag>
                                <option <#if formdata.tags?seq_contains(tag)>selected</#if> value="${tag}">${tag}</option>
                              </#list>
                        </select>
                    </div>
                </div>
                <div class="col-12 col-md-5">
                    <div class="input-group">
                        <label class="input-group-text" for="maps-filter-sort">Tri</label>
                        <select onchange="setFieldValue('maps-filter-page', 0);" name="sort" class="form-select" id="maps-filter-sort">
                          <option <#if formdata.sort == ''>selected</#if> value="">---</option>
                          <option <#if formdata.sort == 'SHORT'>selected</#if> value="SHORT">Plus courtes</option>
                          <option <#if formdata.sort == 'LONG'>selected</#if> value="LONG">Plus longues</option>
                          <option <#if formdata.sort == 'FLAT'>selected</#if> value="FLAT">Plus plates</option>
                          <option <#if formdata.sort == 'HILLY'>selected</#if> value="HILLY">Plus valonnées</option>
                        </select>
                   </div>
                </div>
                <div class="col-12 col-md-1 text-end">
                    <button type="submit" class="btn btn-secondary btn-sm">Valider</button>
                </div>
            </div>

         </form>

    </div>

    <div class="col-12">

        <div class="row g-2 pb-4">
            <#if maps?size == 0>
                 <div class="alert alert-warning" role="alert">
                   Aucune map à afficher.
                 </div>
            </#if>
            <#list maps as map>
                  <div class="col-12 col-md-4">
                    <div class="card">
                        <img src="<@spring.url '/api/' + team.id + '/maps/${map.id}/image' />" class="card-img-top" alt="${map.id} image">
                        <div class="card-body">
                          <h5 class="card-title">${map.name}</h5>
                          <p class="card-text">
                            <i class="bi bi-arrow-left-right"></i> ${map.length}km
                            <i class="bi bi-arrow-up"></i> ${map.positiveElevation}m
                            <i class="bi bi-arrow-down"></i> ${map.negativeElevation}m
                          </p>
                        </div>
                        <div class="card-footer d-flex justify-content-between align-items-center">
                            <span class="small text-muted">${map.postedAt.format(_date_formatter)}</span>
                            <div class="btn-group" role="group">
                                <a download="${map.name}.gpx" href="<@spring.url '/api/' + team.id + '/maps/${map.id}/gpx' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-download"></i> GPX</a>
                                <a download="${map.name}.fit" href="<@spring.url '/api/' + team.id + '/maps/${map.id}/fit' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-download"></i> FIT</a>
                                <a href="<@spring.url '/' + team.id + '/maps/${map.id}' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-map"></i> Voir</a>
                            </div>
                        </div>
                    </div>
                  </div>
            </#list>
        </div>

        <#if pages gt 1>
            <div class="pb-4 d-flex justify-content-center">
                <@common.pagination formdata.page pages 'maps-filter-page' 'maps-filter' />
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">