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
        <link rel="shortcut icon" href="<@common.teamUrl '' '/css/biketeam-logo.png' />">
        <meta name="application-name" content="Biketeam">
        <meta name="description" content="Un site pour les groupes de vélo">
    </#if>

    <title>${_pagetitle} - Biketeam</title>

    <link rel="stylesheet" href="<@common.teamUrl '' '/css/main.css' />" >
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta1/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-giJF6kkoqNQ00vy+HMDP7azOuL0xtbfIcaT9wjKHr8RbDVddVHyTfAAsrekwKmP1" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.0/font/bootstrap-icons.css">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" integrity="sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A==" crossorigin=""/>
    <link rel="stylesheet" href="https://unpkg.com/@raruto/leaflet-elevation@1.9.3/dist/leaflet-elevation.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/github-fork-ribbon-css/0.2.3/gh-fork-ribbon.min.css" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta1/dist/js/bootstrap.bundle.min.js" integrity="sha384-ygbV9kiqUc6oa4msXn9868pTtWMgiQaeYH7/t7LECLbyPA2x65Kgf80OJFdroafW" crossorigin="anonymous"></script>
    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js" integrity="sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA==" crossorigin=""></script>
    <script src="https://unpkg.com/@raruto/leaflet-elevation@1.9.3/dist/leaflet-elevation.js" type="text/javascript"></script>
    <script src="https://unpkg.com/marked@3.0.4/lib/marked.js" type="text/javascript"></script>
    <script src="https://unpkg.com/dompurify@2.3.3/dist/purify.js" type="text/javascript"></script>

    <script src="<@common.teamUrl '' '/js/tags.js' />"></script>
    <script src="<@common.teamUrl '' '/js/autocomplete.js' />"></script>
    <script src="<@common.teamUrl '' '/js/main.js' />"></script>

</head>
<body>

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
            <p>Le fichier sélectionné est trop gros. La limite est de 1MB par fichier.</p>
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

    <div class="container">

    <#include "_includes_errors.ftlh">