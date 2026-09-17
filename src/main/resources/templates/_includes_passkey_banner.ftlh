<#--
    Bandeau d'incitation a enregistrer une passkey, en second rang derriere le bandeau de
    completion de compte (voir _includes_head.ftlh : les deux ne s'affichent jamais ensemble).

    Masquable avec report : le choix est garde en localStorage avec une date de peremption, le
    bandeau revient donc apres le delai. Le localStorage - et non le serveur - est ici le bon
    stockage : une passkey est liee a l'appareil, un refus sur l'ordinateur ne doit pas priver
    l'utilisateur de la proposition sur son telephone.

    Le bandeau est rendu "hidden" : c'est passkey.js qui le revele, et seulement si le navigateur
    gere WebAuthn et si la page est servie par le domaine portant les endpoints (data-passkey-supported-only).
-->
<div id="passkey-banner" class="alert alert-info alert-dismissible border-0 rounded-0 mb-0 py-2"
     role="alert" data-passkey-supported-only hidden>
    <div class="container d-flex flex-wrap align-items-center justify-content-between gap-2">
        <div>
            <i class="bi bi-key-fill"></i>
            Connectez-vous plus vite et plus sûrement : activez une passkey pour vous identifier
            avec vos appareils.
            <span class="d-block small" id="passkey-banner-status" hidden></span>
        </div>
        <button type="button" class="btn btn-sm btn-primary flex-shrink-0"
                data-passkey-register
                data-options-url="<@common.teamUrl '' '/users/me/passkeys/options' />"
                data-register-url="<@common.teamUrl '' '/users/me/passkeys' />"
                data-status-id="passkey-banner-status">
            Activer une passkey
        </button>
    </div>
    <button type="button" class="btn-close p-2" id="passkey-banner-close" aria-label="Plus tard"></button>
</div>

<script type="text/javascript">
    (function () {
        var KEY = 'biketeam.passkeyBannerSnoozedUntil';
        var SNOOZE_DAYS = 60;
        var banner = document.getElementById('passkey-banner');
        if (!banner) {
            return;
        }
        var snoozedUntil = 0;
        try {
            snoozedUntil = parseInt(window.localStorage.getItem(KEY), 10) || 0;
        } catch (e) {
            // localStorage indisponible (navigation privee stricte) : on propose la passkey
        }
        // le bandeau est retire du DOM, et non seulement masque : passkey.js ne doit pas lui
        // rendre sa visibilite au DOMContentLoaded, et son bouton ne doit pas etre branche
        if (snoozedUntil > Date.now()) {
            banner.parentNode.removeChild(banner);
            return;
        }
        document.getElementById('passkey-banner-close').addEventListener('click', function () {
            try {
                window.localStorage.setItem(KEY, String(Date.now() + SNOOZE_DAYS * 24 * 60 * 60 * 1000));
            } catch (e) {
                // rien a memoriser : la proposition reviendra au prochain chargement
            }
            banner.parentNode.removeChild(banner);
        });
    })();
</script>
