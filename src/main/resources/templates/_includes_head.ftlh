<#import "/spring.ftl" as spring/>
<#import "_includes_macro.ftl" as common>

<!doctype html>
<html lang="fr">
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <#if og??>
        <#list og?keys as prop>
            <#if prop?starts_with("og:")>
                <meta property="${prop}" content="${og[prop]}">
            <#else>
                <meta name="${prop}" content="${og[prop]}">
            </#if>
        </#list>
    </#if>

    <#if team??>
        <link rel="shortcut icon" href="<@common.teamUrl team.id '/image' />">
        <meta name="application-name" content="${team.name}">
        <meta name="description" content="${team.description.description}">
    <#else>
        <link rel="shortcut icon" href="<@common.teamUrl '' '/misc/favicon.png' />">
        <meta name="application-name" content="${_sitename}">
        <meta name="description" content="Un site pour les groupes de vélo">
    </#if>

    <title>${_pagetitle}</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/github-fork-ribbon-css/0.2.3/gh-fork-ribbon.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet.locatecontrol@0.81.1/dist/L.Control.Locate.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet-easybutton@2.4.0/src/easy-button.css">
    <link rel="stylesheet" href="https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/leaflet.fullscreen.css"  />
    <link rel="stylesheet" href="<@common.teamUrl '' '/css/sizing.css' />" />
    <link rel="stylesheet" href="<@common.teamUrl '' '/css/navbar.css' />" />
    <link rel="stylesheet" href="<@common.teamUrl '' '/css/done-ribbon.css' />" />
    <link rel="stylesheet" href="<@common.teamUrl '' '/css/main.css' />" />
    <link rel="stylesheet" href="<@common.teamUrl '' '/css/star-rating.css' />" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-geWF76RCwLtnZ8qwWowPQNguL3RmwHVBC9FhGdlKrxdiJJigb/j/68SIy3Te4Bkz" crossorigin="anonymous"></script>
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
    <script src="https://unpkg.com/marked@3.0.4/lib/marked.js" type="text/javascript"></script>
    <script src="https://unpkg.com/dompurify@2.3.3/dist/purify.js" type="text/javascript"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4" type="text/javascript"></script>
    <script src="https://cdn.jsdelivr.net/npm/hammerjs@2.0.8"></script>
    <script src="<@common.teamUrl '' '/js/chartjs-plugin-zoom.min.js' />"></script>
    <script src="<@common.teamUrl '' '/js/chartjs-plugin-corshair.js' />"></script>
    <script src="https://cdn.jsdelivr.net/npm/leaflet.locatecontrol@0.81.1/dist/L.Control.Locate.min.js" charset="utf-8"></script>
    <script src="https://cdn.jsdelivr.net/npm/leaflet-easybutton@2.4.0/src/easy-button.js"></script>
    <script src="https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/Leaflet.fullscreen.min.js"></script>
    <script src="https://unpkg.com/leaflet.vectorgrid@1.3.0/dist/Leaflet.VectorGrid.bundled.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/leaflet-relief@1.0.0/src/L.GridLayer.Relief.js"></script>

    <script src="<@common.teamUrl '' '/js/geoindex/kdbush.js' />"></script>
    <script src="<@common.teamUrl '' '/js/geoindex/tinyqueue.js' />"></script>
    <script src="<@common.teamUrl '' '/js/geoindex/geokdbush.js' />"></script>

    <script src="<@common.teamUrl '' '/jsf/leaflet-layers.js' />"></script>
    <script src="<@common.teamUrl '' '/js/tags.js' />"></script>
    <script src="<@common.teamUrl '' '/js/autocomplete.js' />"></script>
    <script src="<@common.teamUrl '' '/js/leaflet-custom.js' />"></script>
    <script src="<@common.teamUrl '' '/js/single-map.js' />"></script>
    <script src="<@common.teamUrl '' '/js/multiple-maps.js' />"></script>
    <script src="<@common.teamUrl '' '/js/main.js' />"></script>


</head>
<body<#if _fullSize> class="h-100 w-100"</#if>>

    <#if !_embed>
        <#include "_includes_navbar.ftlh">
    </#if>

    <div class="modal fade" id="modal-filetoobig" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Erreur</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <p>Le fichier sélectionné est trop gros. La limite est de 5MB par fichier.</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Accepter</button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="modal-choose-geocode" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Précisez l'adresse</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <ul class="list-group" id="modal-choose-geocode-list">

            </ul>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Accepter</button>
          </div>
        </div>
      </div>
    </div>

    <#if _fullSize>
        <div class="container-fluid container-fluid-responsive w-100 p-0 m-0">
    <#else>
        <div class="container">
    </#if>

    <#include "_includes_errors.ftlh">