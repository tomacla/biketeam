<#import "_includes_macro.ftl" as common>
<#assign unusedReaction=_reactions?filter(r -> element.reactions?filter(rr -> rr.content == r.unicode())?size == 0)>
<#assign usedReaction=_reactions?filter(r -> element.reactions?filter(rr -> rr.content == r.unicode())?size gt 0)>
<#assign userReaction=element.reactions?filter(r -> _authenticated && r.user.id == _user.id)?size gt 0>
<div class="d-flex flex-row align-items-center<#if _authenticated && !userReaction && unusedReaction?size gt 0 && usedReaction?size gt 0> border-end</#if>">
    <#list _reactions?filter(r -> element.reactions?filter(rr -> rr.content == r.unicode())?size gt 0) as reaction>
        <div <#if _authenticated && !userReaction>role="button" onclick="addReaction('reaction-holder-${element.id}', '<@common.teamUrl element.teamId '/${urlPartPrefix}/${element.id}/add-reaction' />', '${reaction.unicode()}'<#if _session??>, '${_session}'</#if>)"</#if> <#if userReaction && element.reactions?filter(r -> _authenticated && r.user.id == _user.id)[0].content == reaction.unicode()>role="button" onclick="removeReaction('reaction-holder-${element.id}', '<@common.teamUrl element.teamId '/${urlPartPrefix}/${element.id}/remove-reaction' />'<#if _session??>, '${_session}'</#if>)"</#if> class="me-1 <#if userReaction && element.reactions?filter(r -> r.user.id == _user.id)[0].content == reaction.unicode()>text-success fw-bolder<#else>text-muted</#if>">&#x${reaction.unicode()};<#if element.reactions?filter(r -> r.content == reaction.unicode())?size gt 0> <span class="small">${element.reactions?filter(r -> r.content == reaction.unicode())?size}</span></#if></div>
    </#list>
</div>
<#if _authenticated && !userReaction && unusedReaction?size gt 0>
<div class="ms-1 d-flex flex-row align-items-center">
    <div class="reaction-choice-opener position-relative" role="button" class="me-0">&#x1F603;<div class="reaction-choices px-1 py-0 flex-row align-items-center" style="width:${10 + (unusedReaction?size * 20)}px">
            <#list unusedReaction as reaction>
                <div class="reaction-choice" role="button" onclick="addReaction('reaction-holder-${element.id}', '<@common.teamUrl element.teamId '/${urlPartPrefix}/${element.id}/add-reaction' />', '${reaction.unicode()}'<#if _session??>, '${_session}'</#if>)" class="me-0">&#x${reaction.unicode()};</div>
            </#list>
        </div>
    </div>

</div>
</#if>