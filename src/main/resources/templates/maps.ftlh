<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12 mb-4">

        <form id="maps-filter" action="<@spring.url '/maps' />" method="GET" class="border rounded p-2">

            <input type="hidden" id="maps-filter-page" name="page" value="${formdata.page}">
            <input type="hidden" id="maps-filter-pageSize" name="pageSize" value="${formdata.pageSize}">

            <div class="row gy-2">
                <div class="col-12 col-md-6">
                    <div class="input-group">
                        <span class="input-group-text">Distance de</span>
                        <input autocomplete="off" type="number" required step="1" min="1" value="${formdata.lowerDistance?c}" name="lowerDistance" class="form-control" id="maps-filter-distance">
                        <span class="input-group-text">à</span>
                        <input autocomplete="off" type="number" required step="1" min="1" value="${formdata.upperDistance?c}" name="upperDistance" class="form-control">
                        <span class="input-group-text">km</span>
                    </div>
                </div>
                <div class="col-12 col-md-5">
                    <div class="input-group">
                        <label class="input-group-text" for="maps-filter-wind">Vent</label>
                        <select name="windDirection" class="form-select" id="maps-filter-wind">
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
                          <select onchange="handleTagChange('maps-filter-tags', 'tags', 'maps-filter-tags-container', 'maps-filter-tags-field-container')" class="form-select" id="maps-filter-tags">
                            <option value="">---</option>
                            <#list tags as tag>
                                <option value="${tag}">${tag}</option>
                            </#list>
                          </select>
                      </div>
                      <div id="maps-filter-tags-container"></div>
                      <div id="maps-filter-tags-field-container">
                        <input type="hidden" name="tags" value="" />
                      </div>
                  </div>
                <div class="col-12 col-md-5">
                    <div class="input-group">
                        <label class="input-group-text" for="maps-filter-sort">Tri</label>
                        <select name="sort" class="form-select" id="maps-filter-sort">
                          <option <#if formdata.sort == ''>selected</#if> value="">---</option>
                          <option <#if formdata.sort == 'SHORT'>selected</#if> value="short">Plus courtes</option>
                          <option <#if formdata.sort == 'LONG'>selected</#if> value="long">Plus longues</option>
                          <option <#if formdata.sort == 'FLAT'>selected</#if> value="flat">Plus plates</option>
                          <option <#if formdata.sort == 'HILLY'>selected</#if> value="hilly">Plus valonnées</option>
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
                        <img src="<@spring.url '/api/maps/${map.id}/image' />" class="card-img-top" alt="${map.id} image">
                        <div class="card-body">
                          <h5 class="card-title">${map.name}</h5>
                          <p class="card-text">
                            <i class="bi bi-arrow-left-right"></i> ${map.length}km
                            <i class="bi bi-arrow-up"></i> ${map.positiveElevation}m
                            <i class="bi bi-arrow-down"></i> ${map.negativeElevation}m
                          </p>
                        </div>
                        <div class="card-footer d-flex justify-content-between align-items-center">
                            <span class="small text-muted">Ajoutée le ${map.postedAt.format(_date_formatter)}</span>
                            <div class="btn-group" role="group">
                                <a download="${map.name}.gpx" href="<@spring.url '/api/maps/${map.id}/gpx' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-download"></i> GPX</a>
                                <a download="${map.name}.fit" href="<@spring.url '/api/maps/${map.id}/fit' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-download"></i> FIT</a>
                                <a href="<@spring.url '/maps/${map.id}' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-map"></i> Voir</a>
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



    <script type="text/javascript">

       <#list formdata.tags as tag>
            createTag('${tag}', 'tags', 'maps-filter-tags-container', 'maps-filter-tags-field-container');
       </#list>

    </script>

</div>

<#include "_includes_tail.ftlh">