<#if errors?? && errors?size gt 0>
    <div class="alert alert-danger my-2 p-2" role="alert">
      <ul class="m-0 list-unstyled">
        <#list errors as error>
            <li>${error}</li>
        </#list>
      </ul>
    </div>
</#if>