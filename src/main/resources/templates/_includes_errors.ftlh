<#if errors?? && errors?size gt 0>
    <div class="alert alert-danger my-2 p-2" role="alert">
      <ul class="m-0 list-unstyled">
        <#list errors as error>
            <li>${error}</li>
        </#list>
      </ul>
    </div>
</#if>

<#if infos?? && infos?size gt 0>
    <div class="alert alert-info my-2 p-2" role="alert">
      <ul class="m-0 list-unstyled">
        <#list infos as info>
            <li>${info}</li>
        </#list>
      </ul>
    </div>
</#if>