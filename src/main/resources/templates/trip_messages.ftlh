<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <#include "trip_nav.ftlh">

    <div class="col-12 col-md-4 mb-4">

        <#if _authenticated>
        <form id="trip-message-form" action="<@common.teamUrl team.id '/trips/' + trip.id + '/add-message' />" method="POST" class="border rounded px-2 py-4">

            <div class="mb-3">
                <label  for="trip-message-new" class="form-label">Nouveau message</label>
                <textarea id="trip-message-new" class="form-control" name="content"></textarea>
                <div id="trip-message-helpLabel" class="d-none form-text"><span class="me-1" id="trip-message-helpLabel-content"></span> <a href="#" onclick="cancel()" class="link-dark"><i class="bi bi-x-circle-fill"></i></a></div>
            </div>

            <input id="trip-message-replyto" type="hidden" name="replyToId" value="" />
            <input id="trip-message-origin" type="hidden" name="originId" value="" />

            <button class="btn btn-secondary btn-sm" type="submit">Poster</input>

        </form>
        <#else>
        <p class="small text-muted">Connectez vous pour poster un message.</p>

    </#if>

</div>

<div class="col-12 col-md-8 mb-4">

    <#if messages?size != 0>
    <div class="border rounded p-2 mb-4 overflow-auto scroll-down" style="max-height: 80vh;">

        <#list messages?filter(m -> !m.replyToId??) as message>

        <div class="<#if !message?is_last>border-bottom </#if> border-3 mb-4 p-1">
            <p class="wrap-content p-3 m-0 rounded <#if _user?? && _user.id == message.user.id>bg-success text-white<#else>bg-light border</#if>">${message.content}</p>
            <div class="px-2 small d-flex justify-content-between align-items-center pt-1">
                <div>
                    <#if _team_admin || _admin || (_user?? && _user.id == message.user.id)><a class="link-danger" href="<@common.teamUrl team.id '/trips/' + trip.id + '/remove-message/' + message.id />"><i class="bi bi-trash"></i></a></#if>
                    <#if _authenticated><a href="#" onclick="replyTo('${message.id}', '${message.user.identity?js_string}')" class="link-dark"><i class="bi bi-reply-fill"></i></a></#if>
                    <#if _team_admin || _admin || (_user?? && _user.id == message.user.id)><a href="#" onclick="edit('${message.id}', '${message.content?js_string}')" class="link-dark"><i class="bi bi-pencil-fill"></i></a></#if>
        </div>
        <div class="d-flex flex-row align-items-center mb-2">
            <span class="text-muted fst-italic">${message.publishedAt.format(_date_formatter)}</span>
            <div class="border-start ms-2 ps-2 d-flex flex-row align-items-center">${message.user.identity} <div style="width:25px; height:25px;" class="d-inline-block ms-2"><img class="w-100 h-100 d-block rounded-pill" src="<@common.teamUrl '' '/users/${message.user.id}/image' />" alt="${message.user.identity}"></div></div> </div>
    </div>

    <#list messages?filter(m -> m.replyToId?? && m.replyToId == message.id) as reply>
    <div class="w-75 ms-auto mb-4 p-1">
        <p class="wrap-content p-3 m-0 rounded <#if _user?? && _user.id == reply.user.id>bg-success text-white<#else>bg-light border</#if>">${reply.content}</p>
        <div class="px-2 small d-flex justify-content-between align-items-center pt-1">
            <div>
                <#if _team_admin || _admin || (_user?? && _user.id == reply.user.id)><a class="link-danger" href="<@common.teamUrl team.id '/trips/' + trip.id + '/remove-message/' + reply.id />"><i class="bi bi-trash"></i></a></#if>
                <#if _authenticated><a href="#" onclick="replyTo('${message.id}', '${reply.user.identity?js_string}')" class="link-dark"><i class="bi bi-reply-fill"></i></a></#if>
                <#if _team_admin || _admin || (_user?? && _user.id == reply.user.id)><a href="#" onclick="edit('${reply.id}', '${reply.content?js_string}')" class="link-dark"><i class="bi bi-pencil-fill"></i></a></#if>
    </div>
    <div class="d-flex flex-row align-items-center">
        <span class="text-muted fst-italic">${reply.publishedAt.format(_date_formatter)}</span>
        <div class="border-start ms-2 ps-2 d-flex flex-row align-items-center">${reply.user.identity} <div style="width:25px; height:25px;" class="d-inline-block ms-2"><img class="w-100 h-100 d-block rounded-pill" src="<@common.teamUrl '' '/users/${reply.user.id}/image' />" alt="${reply.user.identity}"></div></div> </div>
</div>
</div>
</#list>

</div>
</#list>

</div>
</#if>

</div>

<script type="text/javascript">

        function cancel() {
            document.getElementById('trip-message-helpLabel').classList.add('d-none');
            document.getElementById('trip-message-replyto').value = '';
            document.getElementById('trip-message-origin').value = '';
            document.getElementById('trip-message-new').value = '';
        }

        function replyTo(id, author) {

            document.getElementById('trip-message-helpLabel-content').innerHTML = 'Répondre à ' + author;
            document.getElementById('trip-message-helpLabel').classList.remove('d-none');
            document.getElementById('trip-message-replyto').value = id;
            document.getElementById('trip-message-new').focus();
            document.getElementById('trip-message-form').classList.add('bg-info');
            setTimeout(function() {
                document.getElementById('trip-message-form').classList.remove('bg-info');
            }, 400);

        }

        function edit(id, content) {

            document.getElementById('trip-message-helpLabel-content').innerHTML = 'Modifier le message';
            document.getElementById('trip-message-helpLabel').classList.remove('d-none');
            document.getElementById('trip-message-origin').value = id;
            document.getElementById('trip-message-new').value = content;
            document.getElementById('trip-message-new').focus();
            document.getElementById('trip-message-form').classList.add('bg-info');
            setTimeout(function() {
                document.getElementById('trip-message-form').classList.remove('bg-info');
            }, 400);

        }

    </script>



</div>

<#include "_includes_tail.ftlh">