<#include "_includes_head.ftlh">

<div class="row pt-2">

    <div class="col-12">

        <h1 class="my-4 pb-4 border-bottom text-center">Outils GPX</h1>

    </div>

    <div class="col-12 col-md-4 mb-2">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Visualiser un fichier</h5>
            <p class="card-text">Sélectionnez un fichier GPX et visualisez le sur une carte dédiée.</p>
            <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl '' '/gpxtool' />" class="card-text">
                <div>
                    <input autocomplete="off" required="true" name="file" accept=".gpx" class="form-control form-size-check" type="file" id="form-map-gpx">
                </div>
                <div class="mt-2">
                    <input type="submit" class="btn btn-secondary" value="Voir">
                </div>
            </form>
          </div>
        </div>
    </div>

    <div class="col-12 col-md-4 mb-2">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Visualiser une URL</h5>
            <p class="card-text">Saisissez l'URL d'un GPX valide et visualisez le sur une carte dédiée.</p>
            <form method="GET" action="<@common.teamUrl '' '/gpxtool' />" class="card-text">
               <div>
                   <input autocomplete="off" required="true" name="gpx" class="form-control" type="text">
               </div>
              <div class="mt-2">
                   <input type="submit" class="btn btn-secondary" value="Voir">
               </div>
           </form>
          </div>
        </div>
    </div>

    <div class="col-12 col-md-4 mb-2">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Fusionner deux fichiers</h5>
            <p class="card-text">Sélectionnez deux fichiers GPX dans le bon ordre et obtenez un GPX unique.</p>
            <form enctype="multipart/form-data" method="POST" action="<@common.teamUrl '' '/gpxtool/merge' />" class="card-text">
                <div>
                    <input autocomplete="off" required="true" name="file1" accept=".gpx" class="form-control form-size-check" type="file" id="form-map-gpx1">
                </div>
                <div>
                    <input autocomplete="off" required="true" name="file2" accept=".gpx" class="form-control form-size-check" type="file" id="form-map-gpx2">
                </div>
                <div class="mt-2">
                    <input type="submit" class="btn btn-secondary" value="Fusionner">
                </div>
            </form>
          </div>
        </div>
    </div>

    <div class="col-12 col-md-4 mb-2">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Voir une carte</h5>
            <p class="card-text">Ouvrez une carte sans tracé pour bénéficier des fonctionnalités du site.</p>
            <a href="<@common.teamUrl '' '/gpxtool/raw' />" class="btn btn-secondary">Ouvrir</a>
          </div>
        </div>
    </div>

    <div class="col-12 col-md-4 mb-2">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Import OpenRunner</h5>
            <p class="card-text">Saisissez l'ID OpenRunner pour visualiser le GPX</p>
            <form method="GET" action="<@common.teamUrl '' '/gpxtool' />" class="card-text">
               <div>
                   <input id="openrunner-source-field" autocomplete="off" required="true" name="gpxor" class="form-control" type="text"
                        onkeyup="document.getElementById('openrunner-target-field').value='https://api.openrunner.com/api/v2/routes/'+document.getElementById('openrunner-source-field').value+'/export/gpx-track'">
               </div>
               <input type="hidden" id="openrunner-target-field" name="gpx" value="">
              <div class="mt-2">
                   <input type="submit" class="btn btn-secondary" value="Voir">
               </div>
           </form>
          </div>
        </div>
    </div>

 <div class="row justify-content-center mt-4">

    <div class="col-12 col-md-6 text-center">
        <p>Tous les GPX seront stockés sur le serveur pour une durée de 1 mois. Ils seront ensuite supprimés. Pour stocker un GPX sans limite de temps, <a href="<@common.teamUrl '' '/new' />" class="link-dark">créer un groupe</a> et utiliser la fonctionnalité de téléchargement de map.</p>
    </div>

</div>



<#include "_includes_tail.ftlh">