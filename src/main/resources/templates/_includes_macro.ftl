<#macro displayContent content>
  ${(content?replace('\n', '<br>'))}
</#macro>

<#macro pagination page pages pageFieldId formId>
  <div class="btn-group btn-group-sm" role="group">
    <button <#if page == 0>disabled</#if> onclick="setFieldValue('${pageFieldId}', 0); forceSubmitForm('${formId}');" type="button" class="btn btn-outline-secondary"><i class="bi bi-skip-backward-fill"></i></button>
    <button <#if page == 0>disabled</#if> onclick="setFieldValue('${pageFieldId}', ${[page - 1, 0]?max}); forceSubmitForm('${formId}');" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-left-fill"></i></button>
    <button <#if page == pages - 1>disabled</#if> onclick="setFieldValue('${pageFieldId}', ${[page + 1, pages - 1]?min}); forceSubmitForm('${formId}');" type="button" class="btn btn-outline-secondary"><i class="bi bi-caret-right-fill"></i></button>
    <button <#if page == pages - 1>disabled</#if> onclick="setFieldValue('${pageFieldId}', ${pages - 1}); forceSubmitForm('${formId}');" type="button" class="btn btn-outline-secondary"><i class="bi bi-skip-forward-fill"></i></button>
  </div>
</#macro>