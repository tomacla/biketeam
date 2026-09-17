<#--
    Page d'erreur rendue par BasicErrorController.

    Volontairement autonome : elle ne peut pas inclure _includes_head.ftlh, qui a besoin
    d'attributs de modele (_pagetitle, _sitename, _embed...) qu'AbstractController n'a pas
    eu l'occasion de renseigner. Toutes les variables sont donc defaultees, sans quoi une
    erreur de rendu de la page d'erreur masquerait l'erreur d'origine.
-->
<#assign _status = status!0>
<#assign _knownStatus = _status gte 400 && _status lt 600>

<#if _status == 404>
    <#assign _title = "Page introuvable">
    <#assign _text = "Cette page n'existe pas, ou n'existe plus.">
    <#assign _icon = "bi-signpost-2">
<#elseif _status == 403>
    <#assign _title = "Accès refusé">
    <#assign _text = "Vous n'avez pas les droits nécessaires pour voir cette page.">
    <#assign _icon = "bi-shield-lock">
<#else>
    <#assign _title = "Une erreur est survenue">
    <#assign _text = "Le site a rencontré un problème. Réessayez dans un instant.">
    <#assign _icon = "bi-cone-striped">
</#if>

<!doctype html>
<html lang="fr">
<head>

    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="application-name" content="Error">
    <meta name="description" content="Un site pour les groupes de vélo">
    <meta name="robots" content="noindex">

    <link rel="shortcut icon" href="/misc/favicon.png">
    <title>${_title}</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">

</head>
<body class="d-flex align-items-center min-vh-100 bg-body-tertiary">

    <main class="container py-5">
        <div class="row justify-content-center text-center">
            <div class="col-12 col-md-8 col-lg-6">

                <i class="bi ${_icon} text-secondary" style="font-size: 4rem;"></i>

                <#if _knownStatus>
                    <p class="text-secondary fw-semibold mb-1">Erreur ${_status?c}</p>
                </#if>

                <h1 class="h3 mb-3">${_title}</h1>
                <p class="text-secondary mb-4">${_text}</p>

                <a class="btn btn-primary" href="/"><i class="bi bi-house me-1"></i>Retour à l'accueil</a>

            </div>
        </div>
    </main>

</body>
</html>
