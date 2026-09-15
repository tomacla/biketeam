<#--
    Bandeau d'incitation a completer son compte (mode auth.completion.mode = SUGGESTED ou ENFORCED).

    Masquable pour la session seulement : le choix est garde en sessionStorage, le bandeau
    revient donc a la prochaine visite. Le badge de la barre de navigation, lui, reste toujours
    visible : fermer le bandeau ne doit pas faire oublier l'echeance.

    Le bandeau est cache par defaut (style="display:none") et revele par le script : sans cela,
    un utilisateur qui l'a ferme le verrait reapparaitre le temps du chargement de chaque page.
-->
<div id="account-completion-banner" class="alert alert-warning alert-dismissible border-0 rounded-0 mb-0 py-2" role="alert" style="display:none">
    <div class="container d-flex flex-wrap align-items-center justify-content-between gap-2">
        <div>
            <i class="bi bi-exclamation-triangle-fill"></i>
            La connexion avec Strava sera supprimée prochainement.
            Associez une adresse email et un mot de passe à votre compte, ou liez-le à Google ou Facebook.
        </div>
        <a class="btn btn-sm btn-dark flex-shrink-0" href="<@common.teamUrl '' '/account/complete' />">Compléter mon compte</a>
    </div>
    <button type="button" class="btn-close p-2" id="account-completion-banner-close" aria-label="Masquer"></button>
</div>

<script type="text/javascript">
    (function () {
        var KEY = 'biketeam.accountCompletionBannerDismissed';
        var banner = document.getElementById('account-completion-banner');
        if (!banner) {
            return;
        }
        var dismissed = false;
        try {
            dismissed = window.sessionStorage.getItem(KEY) === '1';
        } catch (e) {
            // sessionStorage indisponible (navigation privee stricte) : on affiche le bandeau
        }
        if (dismissed) {
            banner.parentNode.removeChild(banner);
            return;
        }
        banner.style.display = '';
        document.getElementById('account-completion-banner-close').addEventListener('click', function () {
            try {
                window.sessionStorage.setItem(KEY, '1');
            } catch (e) {
                // rien a memoriser : le bandeau reviendra au prochain chargement
            }
            banner.parentNode.removeChild(banner);
        });
    })();
</script>
