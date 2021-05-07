<#include "_includes_head.ftlh">

<div class="row pt-2 justify-content-center">

    <aside class="col-12 col-md-4">

        <section class="border p-2 mb-4 rounded">
            <img src="<@spring.url '/api/logo' />" class="d-block w-50 h-auto mx-auto my-4" alt="Logo image">
            <p class="lead">
              ${_desc.description}
            </p>
            <p class="lead">
                <#if (_desc.facebook)??>
                    <a target="_blank" href="${_desc.facebook}"><i class="bi bi-facebook"></i></a>
                </#if>
                <#if (_desc.twitter)??>
                    <a target="_blank" href="${_desc.twitter}"><i class="bi bi-twitter"></i></a>
                </#if>
            </p>
            <address>
                <#if (_desc.addressStreetLine)?? >${_desc.addressStreetLine}<br></#if>
                <#if (_desc.addressPostalCode)?? >${_desc.addressPostalCode}<br></#if>
                <#if (_desc.addressCity)?? >${_desc.addressCity}<br></#if>
                <#if (_desc.phoneNumber)?? ><i class="bi bi-telephone-fill"></i> ${_desc.phoneNumber}<br></#if>
                <#if (_desc.email)?? ><i class="bi bi-envelope"></i> ${_desc.email}<br></#if>
            </address>
             <#if (_desc.other)?? >
                <p>${_desc.other}</p>
             </#if>
        </section>

    </aside>

    <div class="col-12 col-md-8">

        <div class="row pb-4 g-4">
            <#list feed as feedItem>
                  <div class="col-12">
                    <div class="card">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h5 class="m-0 p-0">
                            <#if feedItem.type == 'RIDE'>
                                <i class="bi bi-bicycle"></i>
                            <#elseif feedItem.type == 'PUBLICATION'>
                                <i class="bi bi-newspaper"></i>
                            </#if>
                            <#if (feedItem.detailsUrl)??>
                                <a class="text-reset text-decoration-none" href="<@spring.url feedItem.detailsUrl />">
                            </#if>
                            <#if feedItem.type == 'RIDE'>
                                Ride
                            <#elseif feedItem.type == 'PUBLICATION'>
                                Publication
                            </#if>
                            <#if (feedItem.detailsUrl)??>
                                </a>
                            </#if>
                            </h5>
                            <small class="text-muted">Ajouté le ${feedItem.publishedAt.format(_date_formatter)}</small>
                        </div>
                        <div class="card-body">
                          <h5 class="card-title">${feedItem.title}</h5>
                          <p class="card-text"><@common.displayContent feedItem.content /></p>
                          <#if (feedItem.imageUrl)??>
                            <img src="<@spring.url feedItem.imageUrl />" class="d-block shadow rounded w-50 h-auto mx-auto" alt="${feedItem.title} image">
                          </#if>
                        </div>
                        <#if (feedItem.detailsUrl)??>
                        <div class="card-footer text-center">
                            <a href="<@spring.url feedItem.detailsUrl />" class="btn btn-secondary btn-sm" role="button"><i class="bi bi-eye"></i> Voir plus</a>
                        </div>
                        </#if>
                    </div>
                  </div>
            </#list>
        </div>

    </div>

</div>

<#include "_includes_tail.ftlh">