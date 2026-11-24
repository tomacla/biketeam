<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "_includes_team_admin_aside.ftlh">

    <div class="col-12 col-md-8">

        <h5 class="mb-3">Exporter les données</h5>

        <div class="border rounded p-4 mb-4">

            <p>
                Téléchargez une archive ZIP du contenu du groupe, pour le conserver ou le reprendre ailleurs.
            </p>

            <h6>Contenu de l'archive</h6>
            <ul>
                <li>la description, la page FAQ<#if summary.faqPage> (présente)<#else> (absente)</#if> et le logo<#if summary.logo> (présent)<#else> (absent)</#if> du groupe ;</li>
                <li>${summary.places} lieu(x) ;</li>
                <li>${summary.routes} parcours, avec leurs traces GPX ;</li>
                <li>${summary.rides} ride(s) ;</li>
                <li>${summary.rideTemplates} modèle(s) de ride ;</li>
                <li>${summary.trips} trip(s) et ${summary.tripStages} étape(s) ;</li>
                <li>${summary.publications} publication(s) ;</li>
                <li>les images des rides, trips et publications.</li>
            </ul>

            <p class="small text-muted">
                Les données sont au format JSON (fichier <code>team.json</code>), accompagnées des fichiers GPX et
                des images ; le fichier <code>README.txt</code> de l'archive en décrit l'organisation.
                L'archive ne contient aucune donnée personnelle (ni membres, ni participants, ni messages)
                ni les éléments supprimés. Sa préparation peut prendre un moment pour un groupe avec beaucoup de parcours.
            </p>

            <a href="<@common.teamUrl team.id '/admin/export/download' />" class="btn btn-secondary" role="button">
                <i class="bi bi-download"></i> Télécharger l'archive
            </a>

        </div>

    </div>

</div>

<#include "_includes_tail.ftlh">
