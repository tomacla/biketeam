<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <script type="text/javascript">

        function createTag(label) {

            if(label === null || label === '') {
                return;
            }

            var badge = document.createElement("span");
            badge.classList.add('badge');
            badge.classList.add('bg-secondary');
            badge.classList.add('me-1');
            badge.innerHTML = label;

            var button = document.createElement("button");
            button.setAttribute('type', 'button');
            button.classList.add('btn-close');
            button.style.padding = '0';
            button.style.margin = '0 0 0 5px';
            button.style.width = '10px';
            button.style.height = '10px';
            button.addEventListener('click', function(event) {
                badge.parentNode.removeChild(badge);
            });

            badge.appendChild(button);

            document.getElementById('maps-filter-tags-container').appendChild(badge);
        }

        function handleTags() {

            var container = document.getElementById('maps-filter-tags-container');
            var tags = [];
            for (var i = 0; i < container.childNodes.length; i++) {

                var badge = container.childNodes[i];
                for (var j = 0; j < badge.childNodes.length; j++) {
                    if (badge.childNodes[j].nodeType === Node.TEXT_NODE) {
                        tags.push(badge.childNodes[j].nodeValue);
                        break;
                    }
                }

            }

           document.getElementById('form-map-tags-field').value = JSON.stringify(tags);
        }

    </script>

    <div class="col-12 col-md-4 mb-4">

        <form onsubmit="handleTags()" action="<@spring.url '/maps' />" method="POST" class="border rounded p-2">

            <input type="hidden" name="tags" id="form-map-tags-field">

            <div class="row mt-3">
                <div class="col-3 text-end">
                    <label for="maps-filter-distance" class="form-label">Distance</label>
                </div>
                <div class="col-4">
                    <input autocomplete="off" type="number" required="true" step="1" min="1" value="${formdata.lowerDistance?c}" name="lowerDistance" class="form-control" id="maps-filter-distance">
                </div>
                 <div class="col-4">
                    <input autocomplete="off" type="number" required="true" step="1" min="1" value="${formdata.upperDistance?c}" name="upperDistance" class="form-control">
                </div>
           </div>
            <div class="row mt-3">
                 <div class="col-3 text-end">
                    <label for="maps-filter-wind" class="form-label">Vent</label>
                </div>
                <div class="col-9">
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
            <div class="row mt-3">
                 <div class="col-3 text-end">
                        <label for="maps-filter-sort" class="form-label">Tri</label>
                    </div>
                    <div class="col-9">
                        <select name="sort" class="form-select" id="maps-filter-sort">
                          <option <#if formdata.sort == ''>selected</#if> value="">---</option>
                          <option <#if formdata.sort == 'short'>selected</#if> value="short">Plus courtes</option>
                          <option <#if formdata.sort == 'long'>selected</#if> value="long">Plus longues</option>
                          <option <#if formdata.sort == 'flat'>selected</#if> value="flat">Plus plates</option>
                          <option <#if formdata.sort == 'hilly'>selected</#if> value="hilly">Plus valonnées</option>
                        </select>
                    </div>
            </div>
            <div class="row mt-3 justify-content-end">
                 <div class="col-3 text-end">
                    <label for="maps-filter-tags" class="form-label">Tags</label>
                </div>
                <div class="col-9">
                    <input type="text" class="form-control" id="maps-filter-tags">
                    <div id="maps-filter-tags-container"></div>
                </div>
            </div>
            <div class="row mt-3 justify-content-end">
                <div class="col-6 text-end">
                    <button type="submit" class="btn btn-secondary btn-sm">Valider</button>
                </div>
            </div>

        </form>

    </div>

    <div class="col-12 col-md-8">

        <div class="row g-4">
            <#if maps?size == 0>
                 <div class="alert alert-warning" role="alert">
                   Aucune map à afficher.
                 </div>
            </#if>
            <#list maps as map>
                  <div class="col-12 col-md-6">
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
                                <a href="<@spring.url '/maps/${map.id}' />" class="btn btn-outline-secondary btn-sm" role="button"><i class="bi bi-map"></i> Web</a>
                            </div>
                        </div>
                    </div>
                  </div>
            </#list>
        </div>

    </div>


    <script type="text/javascript">

        JSON.parse('${(formdata.tags)?no_esc}').forEach(function(tag) {
            createTag(tag);
        });

        preventElementToSubmitForm('maps-filter-tags');

        AutoComplete({
            EmptyMessage: "Aucun tag trouvé",
            Url : "<@spring.url '/api/autocomplete/tags' />",
            OnSelect: function(input, value) {
                createTag(value);
            },
            ClearOnSelect: true
        }, '#maps-filter-tags');

    </script>

</div>

<#include "_includes_tail.ftlh">