<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl team.id '/admin/maps/new' />" class="row mb-0">
            <div class="col-auto p-2">
              <label for="form-map-gpx" class="form-label">Fichier GPX</label>
            </div>
            <div class="col-auto">
                <input autocomplete="off" required name="file" accept=".gpx" class="form-control form-size-check" type="file" id="form-map-gpx">
            </div>
            <div class="col-auto">
                <input type="submit" class="btn btn-secondary" value="Nouvelle Map">
            </div>
        </form>
        <hr/>

        <#if maps?size gt 0>
            <ul class="list-group mt-4">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Map<span class="text-muted"> - Date d'ajout</span></div>
                    <#if maps?size gt 20>
                        <div class="btn-group btn-group-sm" role="group">
                            <button onclick="previousPage()" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-left-fill"></i></button>
                            <button onclick="nextPage()" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-right-fill"></i></button>
                          </div>
                    </#if>
                    <div>Actions</div>
                </li>
                <#list maps as map>
                    <li class="d-none list-group-item list-group-item-map justify-content-between align-items-center">
                        <div>${map.name}<span class="text-muted"> - ${map.postedAt.format(_date_formatter)}</span><#if !map.visible> - <i class="bi bi-eye-slash"></i></#if></div>
                        <div class="btn-group" role="group">
                            <a href="<@common.teamUrl team.id '/maps/${map.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/maps/${map.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/maps/delete/${map.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>


            <script type="text/javascript">

                var currentPage = 0;

                function previousPage() {
                    currentPage = Math.max(0, currentPage-20);
                    displayItems();
                }

                function nextPage() {
                    currentPage = Math.min(${maps?size} - 20, currentPage+20);
                    displayItems();
                }

                function displayItems() {
                     const mapsElements = document.getElementsByClassName('list-group-item-map');
                     for(var i = 0; i < mapsElements.length; i++) {
                        if(i >= currentPage && i < currentPage + 20) {
                            mapsElements[i].classList.remove("d-none");
                            mapsElements[i].classList.add("d-flex");
                        } else {
                            mapsElements[i].classList.remove("d-flex");
                            mapsElements[i].classList.add("d-none");
                        }
                     }
                }

                displayItems();

            </script>

         <#else>
             <div class="alert alert-warning" role="alert">
               Aucune map.
             </div>
         </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">