<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">


    <#include "trip_nav.ftlh">

    <div class="col-12 col-md-4 mb-4">

        <#if _authenticated>
            <form action="<@common.teamUrl team.id '/trips/' + trip.id + '/add-message' />" method="POST" class="border rounded p-2">

                <div class="mb-3">
                    <label for="trip-message-new" class="form-label">Nouveau message</label>
                    <textarea id="trip-message-new" class="form-control" name="content"></textarea>
                </div>

                <button class="btn btn-secondary btn-sm" type="submit">Poster</input>

            </form>
            <#else>
            <p class="small text-muted">Connectez vous pour poster un message.</p>

       </#if>

    </div>

    <div class="col-12 col-md-8 mb-4">

       <#if trip.messages?size != 0>
           <div class="bg-light border rounded p-2 mb-4 overflow-auto scroll-down" style="max-height: 60vh;">

               <#list trip.sortedMessages as message>
                   <div class="<#if _user?? && _user.id == message.user.id>bg-light border-success<#else>bg-white</#if> small border rounded mb-1 p-1">
                       <p class="p-1 m-0">${message.content}</p>
                       <div class="d-flex justify-content-between align-items-end border-top pt-1">
                           <span class="text-muted">${message.publishedAt.format(_date_formatter)}<#if _team_admin || (_user?? && _user.id == message.user.id)> <a class="link-danger" href="<@common.teamUrl team.id '/trips/' + trip.id + '/remove-message/' + message.id />"><i class="bi bi-trash"></i></a></#if></span>

                               <div style="width:30px; height:30px;" data-bs-toggle="tooltip" data-bs-placement="top" title="${message.user.identity}" class="d-inline-block"><img class="w-100 h-100 d-block rounded" src="<@common.teamUrl '' '/users/${message.user.id}/image' />" alt="${message.user.identity}"></div>

                       </div>
                   </div>
               </#list>

           </div>
       </#if>

    </div>


</div>

<#include "_includes_tail.ftlh">