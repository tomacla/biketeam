<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <script type="text/javascript">

        function confirmDeleteTemplate(templateName, targetUrl) {
            var confirmedDeletion = confirm('Confirmer la suppression définitive du modèle ' + templateName + ' ?');
            if(confirmedDeletion) {
                window.location=targetUrl;
            }
        }

    </script>

    <div class="col-12 col-md-8">

        <a class="btn btn-secondary" href="<@common.teamUrl team.id '/admin/templates/new' />"><i class="bi bi-plus"></i> Nouveau template</a>

        <hr/>

        <#if templates?size gt 0>
            <ul class="list-group mt-4">
                <li class="list-group-item list-group-item-secondary d-flex justify-content-between align-items-center">
                    <div>Template</div>
                    <div>Actions</div>
                </li>
                <#list templates as template>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        <div>${template.name}</div>
                        <div class="btn-group" role="group">
                            <a href="<@common.teamUrl team.id '/admin/templates/${template.id}' />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-pencil"></i></a>
                            <a href="#" onclick="confirmDeleteTemplate('${template.name}', '<@common.teamUrl team.id '/admin/templates/delete/${template.id}' />')" class="btn btn-danger btn-sm" role="button"><i class="bi bi-trash"></i></a>
                        </div>
                    </li>
                </#list>
            </ul>
        <#else>
            <div class="alert alert-warning" role="alert">
              Aucun template.
            </div>
        </#if>

    </div>

</div>

<#include "_includes_tail.ftlh">