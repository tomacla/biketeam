<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <a href="<@common.teamUrl team.id '/admin/publications/new' />" class="btn btn-secondary" role="button"><i class="bi bi-plus"></i> Nouvelle publication</a>
        <hr/>
        <#if publications?size gt 0>
            <ul class="list-group mt-4">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Publication<span class="text-muted"> - Date de publication</span></div>
                    <#if publications?size gt 20>
                        <div class="btn-group btn-group-sm" role="group">
                            <button onclick="previousPage()" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-left-fill"></i></button>
                            <button onclick="nextPage()" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-right-fill"></i></button>
                          </div>
                    </#if>
                    <div>Actions</div>
                </li>
                <#list publications as publication>
                    <li class="d-none list-group-item list-group-item-publication justify-content-between align-items-center">
                        <div><span class="text-<#if publication.publishedStatus == 'PUBLISHED'>success<#else>danger</#if>"><i class="bi bi-calendar-<#if publication.publishedStatus == 'PUBLISHED'>check<#else>x</#if>"></i></span> ${publication.title}<span class="text-muted"> - ${publication.publishedAt.format(_date_formatter)}</span></div>
                        <div class="btn-group" role="group">
                            <a href="<@common.teamUrl team.id '/admin/publications/${publication.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="<@common.teamUrl team.id '/admin/publications/delete/${publication.id}' />" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
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
                    currentPage = Math.min(${publications?size?c} - 20, currentPage+20);
                    displayItems();
                }

                function displayItems() {
                     const mapsElements = document.getElementsByClassName('list-group-item-publication');
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
              Aucune publication.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">