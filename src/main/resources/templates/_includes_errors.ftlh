<#if errors?? && errors?size gt 0>
    <div class="alert alert-danger my-2" role="alert">
     <p>Erreurs</p>
      <ul>
        <#list errors as error>
            <li>${error}</li>
        </#list>
      </ul>
    </div>
</#if>