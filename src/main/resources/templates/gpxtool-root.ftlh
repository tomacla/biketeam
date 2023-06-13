<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <div class="col-12">

        <h1 class="my-4 pb-4 text-center">Visualiser un GPX</h1>

        <#if errors?? && errors?size gt 0>
            <div class="mt-2 w-100 alert alert-danger p-1" role="alert">
              <ul class="p-0 m-0 list-unstyled">
                <#list errors as error>
                    <li>${error}</li>
                </#list>
              </ul>
            </div>
        </#if>

    </div>

    <div class="col-12 col-md-4 text-center pb-4">
        <h3>Par fichier</h3>

            <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl '' '/gpxtool' />" class="d-flex justify-content-center">
                <div>
                    <input autocomplete="off" required="true" name="file" accept=".gpx" class="form-control form-size-check" type="file" id="form-map-gpx">
                </div>
                <div class="ms-1">
                    <input type="submit" class="btn btn-secondary" value="Envoyer">
                </div>
            </form>
    </div>

    <div class="col-12 col-md-4 text-center pb-4">
           <h3>Par URL</h3>

               <form method="GET" action="<@common.teamUrl '' '/gpxtool' />" class="d-flex justify-content-center">
                   <div>
                       <input autocomplete="off" required="true" name="gpx" class="form-control" type="text">
                   </div>
                   <div class="ms-1">
                       <input type="submit" class="btn btn-secondary" value="Envoyer">
                   </div>
               </form>
        </div>

  </div>

 <div class="row justify-content-center">

    <div class="col-12 col-md-6 text-center">
        <p>Le GPX sera stocké sur le serveur pour une durée de 1 mois. Il sera ensuite supprimé. Pour stocker un GPX sans limite de temps, <a href="<@common.teamUrl '' '/new' />" class="link-dark">créer un groupe</a> et utiliser la fonctionnalité de téléchargement de map.</p>
    </div>

</div>



<#include "_includes_tail.ftlh">